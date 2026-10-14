package com.zengge.nbmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FileAdapter extends ListAdapter<FileItem, FileAdapter.FileViewHolder> {

    public FileAdapter() {
        super(DIFF_CALLBACK);
    }

    public FileAdapter(List<FileItem> list) {
        this();
        submitList(list);
    }

    private static final DiffUtil.ItemCallback<FileItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<FileItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull FileItem oldItem, @NonNull FileItem newItem) {
                    return oldItem.documentFile.getUri().equals(newItem.documentFile.getUri());
                }

                @Override
                public boolean areContentsTheSame(@NonNull FileItem oldItem, @NonNull FileItem newItem) {
                    return oldItem.name.equals(newItem.name)
                            && oldItem.isDirectory == newItem.isDirectory
                            && oldItem.size == newItem.size;
                }
            };

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_browser, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem item = getCurrentList().get(position);
        holder.name.setText(item.name);

        if (item.isDirectory) {
            holder.icon.setImageResource(android.R.drawable.ic_menu_sort_by_size);
            holder.meta.setText("Folder");
        } else {
            holder.icon.setImageResource(android.R.drawable.ic_menu_report_image);
            holder.meta.setText(formatFileSize(item.size));
        }
    }

    public FileItem getItemAt(int position) {
        if (position < 0 || position >= getCurrentList().size()) return null;
        return getCurrentList().get(position);
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        double s = size;
        int unit = 0;
        while (s > 1024 && unit < units.length - 1) {
            s = s / 1024.0;
            unit++;
        }
        return String.format("%.2f %s", s, units[unit]);
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView meta;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.itemIcon);
            name = itemView.findViewById(R.id.itemName);
            meta = itemView.findViewById(R.id.itemMeta);
        }
    }
}
