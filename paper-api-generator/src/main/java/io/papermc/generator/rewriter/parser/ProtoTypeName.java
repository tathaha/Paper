package io.papermc.generator.rewriter.parser;

import it.unimi.dsi.fastutil.chars.CharSet;

public class ProtoTypeName {

    private final StringBuilder currentName;
    private char lastChar;
    private boolean idTerminatorExpected;

    public ProtoTypeName(char[] initialChars) {
        this.currentName = new StringBuilder(initialChars.length);
        this.append(initialChars);
    }

    public boolean append(char... namePart) {
        if (this.idTerminatorExpected) {
            if (namePart[0] != '.') {
                return false;
            } else {
                this.idTerminatorExpected = false;
            }
        }

        this.currentName.append(namePart);
        this.lastChar = namePart[namePart.length - 1];
        return true;
    }

    public void expectIdTerminator() {
        this.idTerminatorExpected = true;
    }

    public char getLastChar() {
        return this.lastChar;
    }

    public String getFinalName() {
        return this.currentName.toString();
    }

    public boolean shouldCheckStartIdentifier() {
        return this.lastChar == '.';
    }

}
