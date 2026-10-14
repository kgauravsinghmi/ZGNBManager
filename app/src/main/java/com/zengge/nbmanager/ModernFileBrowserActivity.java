package com.zengge.nbmanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ModernFileBrowserActivity extends AppCompatActivity {

    private static final String PREF_TREE_URI = "modern_tree_uri";

    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private TextView pathText;
    private DocumentFile currentDir;

    private final ActivityResultLauncher<Intent> folderPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri == null) return;

                    int flags = result.getData().getFlags();
                    int grantFlags = flags & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    if (grantFlags == 0) {
                        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        getContentResolver().takePersistableUriPermission(uri, grantFlags);
                    } catch (SecurityException se) {
                        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentDir = DocumentFile.fromTreeUri(this, uri);
                    if (currentDir != null && currentDir.exists()) {
                        saveTreeUri(uri.toString());
                        loadCurrentDir();
                    } else {
                        Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modern_file_browser);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pathText = findViewById(R.id.pathText);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter();
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FileItem item = adapter.getItemAt(position);
                if (item == null) return;
                if (item.isDirectory) {
                    openDirectory(item.documentFile);
                } else {
                    openFile(item.documentFile);
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {
                FileItem item = adapter.getItemAt(position);
                if (item == null) return;
                showOptionsDialog(item);
            }
        }));

        FloatingActionButton fab = findViewById(R.id.fabPickFolder);
        fab.setOnClickListener(v -> openFolderPicker());

        String lastUri = getSavedTreeUri();
        if (lastUri != null) {
            Uri uri = Uri.parse(lastUri);
            currentDir = DocumentFile.fromTreeUri(this, uri);
            if (currentDir != null && currentDir.exists()) {
                loadCurrentDir();
            } else {
                openFolderPicker();
            }
        } else {
            openFolderPicker();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_browser_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_open_root) {
            openFolderPicker();
            return true;
        } else if (id == R.id.action_refresh) {
            if (currentDir != null) loadCurrentDir();
            return true;
        } else if (id == R.id.action_create_folder) {
            createFolderDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        folderPicker.launch(intent);
    }

    private void saveTreeUri(String uriString) {
        getSharedPreferences("file_browser", MODE_PRIVATE)
                .edit()
                .putString(PREF_TREE_URI, uriString)
                .apply();
    }

    private String getSavedTreeUri() {
        return getSharedPreferences("file_browser", MODE_PRIVATE)
                .getString(PREF_TREE_URI, null);
    }

    private void loadCurrentDir() {
        if (currentDir == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DocumentFile[] children = currentDir.listFiles();
            final List<FileItem> items = new ArrayList<>();
            if (children != null) {
                for (DocumentFile child : children) {
                    items.add(new FileItem(child));
                }
            }

            runOnUiThread(() -> {
                adapter.submitList(items);
                pathText.setText(currentDir.getName() == null ? currentDir.getUri().toString() : currentDir.getName());
            });
        });
    }

    private void openDirectory(DocumentFile dir) {
        if (dir == null || !dir.isDirectory()) return;
        currentDir = dir;
        loadCurrentDir();
    }

    private void openFile(DocumentFile file) {
        if (file == null || !file.exists()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String mime = getContentResolver().getType(file.getUri());
        intent.setDataAndType(file.getUri(), mime == null ? "*/*" : mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    private void createFolderDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this)
                .setTitle(R.string.create_folder)
                .setView(input)
                .setPositiveButton(R.string.btn_ok, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.folder_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (currentDir == null) {
                        Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentFile newDir = currentDir.createDirectory(name);
                    if (newDir != null) {
                        Toast.makeText(this, R.string.create_success, Toast.LENGTH_SHORT).show();
                        loadCurrentDir();
                    } else {
                        Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showOptionsDialog(FileItem item) {
        if (item == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.name);
        builder.setItems(new CharSequence[]{
                getString(R.string.open),
                getString(R.string.rename),
                getString(R.string.delete)
        }, (dialog, which) -> {
            switch (which) {
                case 0:
                    if (item.isDirectory) openDirectory(item.documentFile);
                    else openFile(item.documentFile);
                    break;
                case 1:
                    renameDialog(item);
                    break;
                case 2:
                    deleteItem(item);
                    break;
            }
        });
        builder.show();
    }

    private void renameDialog(FileItem item) {
        if (item == null) return;

        final EditText input = new EditText(this);
        input.setText(item.name);

        new AlertDialog.Builder(this)
                .setTitle(R.string.rename)
                .setView(input)
                .setPositiveButton(R.string.btn_ok, (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) return;

                    boolean ok = item.documentFile.renameTo(newName);
                    if (ok) {
                        Toast.makeText(this, R.string.rename_success, Toast.LENGTH_SHORT).show();
                        loadCurrentDir();
                    } else {
                        Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void deleteItem(FileItem item) {
        if (item == null) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.is_delete, item.name))
                .setPositiveButton(R.string.btn_ok, (dialog, which) -> {
                    boolean ok = item.documentFile.delete();
                    if (ok) {
                        Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
                        loadCurrentDir();
                    } else {
                        Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (currentDir != null && currentDir.getParentFile() != null) {
            currentDir = currentDir.getParentFile();
            loadCurrentDir();
        } else {
            super.onBackPressed();
        }
    }
}
