package io.papermc.generator.rewriter;

import java.io.IOException;
import java.nio.file.Path;

public interface SourceRewriter {

    default boolean registerFor(SourceFile file) {
        return true;
    }

    void dump(SourceFile file, StringBuilder into);

    default void writeToFile(Path parent, SourceFile file) throws IOException {
        writeToFile(parent, parent, file);
    }

    @Deprecated // used only for test
    void writeToFile(Path readFolder, Path writeFolder, SourceFile file) throws IOException;
}
