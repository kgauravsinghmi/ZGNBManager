package com.zengge.decompiler.mapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Background export task for generating a deobfuscation mapping.txt file.
 * This integrates with the MappingExtractor + MappingWriter classes.
 */
public class ExportMappingTask implements Callable<List<MappingEntry>> {

    public static class Options {
        public boolean includeClasses = true;
        public boolean includeFields = true;
        public boolean includeMethods = true;
        public boolean includeResources = true;
        public boolean includeFiles = true;
        public boolean enableUnicodeNormalization = true;
    }

    private final File input;
    private final Options options;

    public ExportMappingTask(File input, Options options) {
        this.input = input;
        this.options = options;
    }

    @Override
    public List<MappingEntry> call() throws Exception {
        if (input == null || !input.exists()) {
            return new ArrayList<>();
        }

        MappingExtractor extractor = new MappingExtractor(new MappingExtractor.Options());
        extractor = new MappingExtractor() {
            @Override
            public List<MappingEntry> extract(File in) throws Exception {
                // This is a structural placeholder that can be wired to the real parser stack.
                // For now it delegates to the default skeleton implementation.
                return super.extract(in);
            }
        };

        // Map user options into extractor defaults in a lightweight way.
        MappingExtractor.Options exOptions = new MappingExtractor.Options();
        exOptions.includeClasses = options.includeClasses;
        exOptions.includeFields = options.includeFields;
        exOptions.includeMethods = options.includeMethods;
        exOptions.includeResources = options.includeResources;
        exOptions.includeFiles = options.includeFiles;
        exOptions.enableUnicodeNormalization = options.enableUnicodeNormalization;

        MappingExtractor realExtractor = new MappingExtractor(exOptions);
        return realExtractor.extract(input);
    }
}
