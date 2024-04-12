package io.papermc.generator.rewriter.parser;

public class ProtoTypeName {

    private final String initialName;
    private StringBuilder currentName;

    private ProtoTypeName(String initialName) {
        this.initialName = initialName;
    }

    public void append(String part) {
        if (this.currentName == null) {
            this.currentName = new StringBuilder(this.initialName);
        }
        this.currentName.append(part);
    }

    public String getFinalName() {
        return this.currentName != null ? this.currentName.toString() : this.initialName;
    }

    public boolean shouldCheckStartIdentifier() {
        if (this.currentName != null) {
            return this.currentName.isEmpty() || this.currentName.lastIndexOf(".") == this.currentName.length() - 1;
        }
        return this.initialName.isEmpty() || this.initialName.lastIndexOf('.') == this.initialName.length() - 1;
    }

    public static ProtoTypeName create(String initialName) {
        return new ProtoTypeName(initialName);
    }

}
