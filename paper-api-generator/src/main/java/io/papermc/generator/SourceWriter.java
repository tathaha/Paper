package io.papermc.generator;

import java.io.IOException;
import java.nio.file.Path;

public interface SourceWriter {

    String INDENT_UNIT = "    ";
    int INDENT_SIZE = INDENT_UNIT.length();
    char INDENT_CHAR = INDENT_UNIT.charAt(0);

    void writeToFile(Path parent) throws IOException;

}
