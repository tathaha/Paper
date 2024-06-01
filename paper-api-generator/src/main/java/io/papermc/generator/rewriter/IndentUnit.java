package io.papermc.generator.rewriter;

import com.google.common.base.Preconditions;
import io.papermc.generator.utils.Formatting;
import org.checkerframework.checker.nullness.qual.NonNull;

public record IndentUnit(String content, int size, char character) implements CharSequence {

    public static IndentUnit parseUnsafe(@NonNull String indentUnit) {
        return new IndentUnit(indentUnit, indentUnit.length(), indentUnit.charAt(0));
    }

    /**
     * Creates an indent unit compatible with a source file.
     * <br>
     * The indent unit characters must follow these conditions:
     *
     * <ul>
     *   <li>all the characters in the string must use the same character repeated if needed.
     *   <li>the characters used are valid whitespace as defined by {@link Character#isWhitespace(char)}
     *   (exception is the newline characters (CR/LF).
     *   <li>supplementary whitespace characters are not allowed nor supported.
     * </ul>
     *
     * @param indentUnit the indent unit content
     * @return the new object
     * @see io.papermc.generator.rewriter.SourceFile
     */
    public static IndentUnit parse(@NonNull String indentUnit) {
        Preconditions.checkArgument(!indentUnit.isEmpty(), "Indent unit cannot be empty!");
        char c0 = indentUnit.charAt(0);
        int size = indentUnit.length();

        for (int i = 0; i < size; i++) {
            char c = indentUnit.charAt(i);
            Preconditions.checkArgument(Character.isWhitespace(c) && c != '\r' && c != '\n', "Character '%s' in the indent unit is not a whitespace!", c);
            Preconditions.checkArgument(c0 == c, "Indent unit contains mixed whitespace (%s != %s)", c, c0);
        }

        return new IndentUnit(indentUnit, size, c0);
    }

    @Override
    public int length() {
        return this.size;
    }

    @Override
    public char charAt(final int index) {
        Preconditions.checkElementIndex(index, this.size);
        return this.character; // mixed whitespace are not allowed
    }

    @Override
    public @NonNull CharSequence subSequence(final int beginIndex, final int endIndex) {
        return this.content.subSequence(beginIndex, endIndex);
    }

    public String adjustContentFor(ClassNamed classNamed) {
        if (classNamed.knownClass() == null) {
            return this.content.repeat(Formatting.countOccurrences(classNamed.dottedNestedName(), '.') + 1);
        }

        Class<?> parent = classNamed.knownClass().getEnclosingClass();
        StringBuilder indentBuilder = new StringBuilder(this.content);
        while (parent != null) {
            indentBuilder.append(this.content);
            parent = parent.getEnclosingClass();
        }
        return indentBuilder.toString();
    }
}
