package io.papermc.generator.rewriter;

import java.io.IOException;
import java.nio.file.Path;

public interface SourceRewriter {

    default boolean registerFor(SourceFile file) {
        return true;
    }

    default void writeToFile(Path parent, SourceFile file) throws IOException {
        writeToFile(parent, parent, file);
    }

    @Deprecated // used only for paper test
    void writeToFile(Path readFolder, Path writeFolder, SourceFile file) throws IOException;
}
