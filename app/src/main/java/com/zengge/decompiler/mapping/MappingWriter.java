package com.zengge.decompiler.mapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes mapping entries to a ProGuard-style mapping.txt file.
 * Supports class, field, method, resource, and file mappings.
 */
public class MappingWriter {
    public static void write(File outputFile, List<MappingEntry> entries) throws IOException {
        Map<String, List<MappingEntry>> classMap = new LinkedHashMap<>();
        List<MappingEntry> resourceEntries = new ArrayList<>();
        List<MappingEntry> fileEntries = new ArrayList<>();

        for (MappingEntry e : entries) {
            switch (e.getType()) {
                case CLASS:
                    classMap.computeIfAbsent(e.getObfuscatedName(), k -> new ArrayList<>()).add(e);
                    break;
                case FIELD:
                case METHOD:
                    String owner = e.getOwner();
                    if (owner == null || owner.trim().isEmpty()) {
                        owner = e.getObfuscatedName();
                    }
                    classMap.computeIfAbsent(owner, k -> new ArrayList<>()).add(e);
                    break;
                case RESOURCE:
                    resourceEntries.add(e);
                    break;
                case FILE:
                    fileEntries.add(e);
                    break;
                default:
                    break;
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            for (Map.Entry<String, List<MappingEntry>> entry : classMap.entrySet()) {
                MappingEntry classEntry = null;
                for (MappingEntry me : entry.getValue()) {
                    if (me.getType() == MappingEntry.Type.CLASS) {
                        classEntry = me;
                        break;
                    }
                }

                if (classEntry != null) {
                    bw.write(classEntry.getOriginalName() + " -> " + classEntry.getObfuscatedName() + ":");
                    bw.newLine();
                } else {
                    bw.write("# owner: " + entry.getKey());
                    bw.newLine();
                }

                for (MappingEntry me : entry.getValue()) {
                    if (me.getType() == MappingEntry.Type.FIELD) {
                        bw.write("    field " + me.getOriginalName() + " -> " + me.getObfuscatedName());
                        bw.newLine();
                    } else if (me.getType() == MappingEntry.Type.METHOD) {
                        String sig = me.getSignature() == null ? "" : me.getSignature();
                        bw.write("    method " + sig + " " + me.getOriginalName() + " -> " + me.getObfuscatedName());
                        bw.newLine();
                    }
                }
                bw.newLine();
            }

            if (!resourceEntries.isEmpty()) {
                bw.write("# RESOURCES");
                bw.newLine();
                for (MappingEntry re : resourceEntries) {
                    bw.write(re.getOwner() + ": " + re.getOriginalName() + " -> " + re.getObfuscatedName());
                    bw.newLine();
                }
                bw.newLine();
            }

            if (!fileEntries.isEmpty()) {
                bw.write("# FILES");
                bw.newLine();
                for (MappingEntry fe : fileEntries) {
                    bw.write(fe.getOriginalName() + " -> " + fe.getObfuscatedName());
                    bw.newLine();
                }
                bw.newLine();
            }
        }
    }
}
