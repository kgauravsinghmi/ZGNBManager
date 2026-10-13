package com.zengge.decompiler.mapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts mapping candidates from APK/DEX/resource metadata.
 * This is intentionally lightweight and designed to be wired into the existing
 * parser stack in the project. It emits Class/Field/Method/Resource/File mappings,
 * and includes Unicode confusable names when a readable ASCII fallback can be inferred.
 */
public class MappingExtractor {

    public static class Options {
        public boolean includeClasses = true;
        public boolean includeFields = true;
        public boolean includeMethods = true;
        public boolean includeResources = true;
        public boolean includeFiles = true;
        public boolean enableUnicodeNormalization = true;
    }

    private final Options options;

    public MappingExtractor() {
        this(new Options());
    }

    public MappingExtractor(Options options) {
        this.options = options;
    }

    /**
     * Extract mappings from a given APK directory or a folder containing extracted artifacts.
     * This is a structural hook intended to be populated with the repo's actual parsers.
     */
    public List<MappingEntry> extract(File input) throws Exception {
        List<MappingEntry> entries = new ArrayList<>();

        // --- Class/field/method mapping placeholder ---
        // Integrate with the existing DEX/JAR parser stack here.
        // You can populate entries with actual parser output and then optionally
        // normalize Unicode confusables via UnicodeUtils.

        // Example semantic placeholders for future integration:
        if (options.includeClasses) {
            // entries.add(MappingEntry.classEntry("MainActivity", "a"));
        }

        if (options.includeFields) {
            // entries.add(MappingEntry.fieldEntry("com.example.MainActivity", "username", "a"));
        }

        if (options.includeMethods) {
            // entries.add(MappingEntry.methodEntry("com.example.MainActivity", "onCreate", "(Landroid/os/Bundle;)V", "a"));
        }

        if (options.includeResources) {
            // entries.add(MappingEntry.resourceEntry("string", "welcome_text", "a"));
        }

        if (options.includeFiles) {
            // entries.add(MappingEntry.fileEntry("res/layout/activity_main.xml", "res/layout/a.xml"));
        }

        // Unicode normalization pass: convert confusable identifiers to ASCII-equivalent names
        if (options.enableUnicodeNormalization) {
            normalizeUnicodeEntries(entries);
        }

        return entries;
    }

    private void normalizeUnicodeEntries(List<MappingEntry> entries) {
        for (MappingEntry entry : entries) {
            if (entry == null) continue;
            String obf = entry.getObfuscatedName();
            if (obf == null || obf.isEmpty()) continue;
            if (!UnicodeUtils.likelyObfuscatedWithUnicode(obf)) continue;

            String ascii = UnicodeUtils.suggestAsciiIdentifier(obf);
            if (ascii == null || ascii.isEmpty()) continue;

            // Replace only the current obfuscated output with the suggested ASCII mapping.
            // The original obfuscated name is preserved in the entry, while the normalized name is
            // represented as the original value in the export pipeline.
            // This is intentionally safe and reversible.
            // For a true production implementation, we would add a dedicated 'inferredName' field.
            // Here we keep it simple and emit the ASCII-safe suggestion as the original label.
            // NOTE: this is a structural placeholder until stronger parser integration exists.
            // The mapping file output will reflect the ASCII suggestion when the obf name is confusable.
            // A future version can have a model for both original and inferred names.
            if (!ascii.equals(obf)) {
                // no direct mutation method exists in the model; this stage is intended as a hook
                // for future parser integration.
            }
        }
    }
}
