package com.zengge.decompiler.mapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple mapping entry model used to generate ProGuard-style mapping.txt files.
 */
public class MappingEntry {
    public enum Type { CLASS, FIELD, METHOD, RESOURCE, FILE }

    private final Type type;
    private final String owner; // for FIELD/METHOD - full original owner class name
    private final String originalName; // original readable name (or inferred)
    private final String obfuscatedName; // obfuscated name found in target
    private final String signature; // optional method signature for METHOD entries

    public MappingEntry(Type type, String owner, String originalName, String obfuscatedName, String signature) {
        this.type = type;
        this.owner = owner;
        this.originalName = originalName;
        this.obfuscatedName = obfuscatedName;
        this.signature = signature;
    }

    public Type getType() { return type; }
    public String getOwner() { return owner; }
    public String getOriginalName() { return originalName; }
    public String getObfuscatedName() { return obfuscatedName; }
    public String getSignature() { return signature; }

    public static MappingEntry classEntry(String originalClass, String obfuscatedClass) {
        return new MappingEntry(Type.CLASS, null, originalClass, obfuscatedClass, null);
    }

    public static MappingEntry fieldEntry(String owner, String originalField, String obfuscatedField) {
        return new MappingEntry(Type.FIELD, owner, originalField, obfuscatedField, null);
    }

    public static MappingEntry methodEntry(String owner, String originalMethod, String signature, String obfuscatedMethod) {
        return new MappingEntry(Type.METHOD, owner, originalMethod, obfuscatedMethod, signature);
    }

    public static MappingEntry resourceEntry(String typeName, String original, String obfuscated) {
        // use owner to store resource type (e.g., "string", "id")
        return new MappingEntry(Type.RESOURCE, typeName, original, obfuscated, null);
    }

    public static MappingEntry fileEntry(String originalPath, String obfuscatedPath) {
        return new MappingEntry(Type.FILE, null, originalPath, obfuscatedPath, null);
    }
}
