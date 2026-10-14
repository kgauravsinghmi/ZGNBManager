package com.zengge.nbmanager;

import androidx.documentfile.provider.DocumentFile;

public class FileItem {
    public final DocumentFile documentFile;
    public final String name;
    public final boolean isDirectory;
    public final long size;

    public FileItem(DocumentFile file) {
        this.documentFile = file;
        this.name = (file.getName() == null) ? "" : file.getName();
        this.isDirectory = file.isDirectory();
        this.size = file.length();
    }
}
