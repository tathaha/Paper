package io.papermc.generator.rewriter.parser;

import io.papermc.generator.rewriter.context.ImportCollector;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@Tag("parser")
public class ParserTest {

    protected static final Path CONTAINER = Path.of(System.getProperty("user.dir"), "src/test/java");

    protected void parseFile(Path path, ImportCollector importCollector) throws IOException {
        parseFile(path, importCollector, null, null);
    }

    protected void parseFile(Path path, ImportCollector importCollector, @Nullable Consumer<String> enterBodyCallback, @Nullable Runnable eofCallback) throws IOException {
        final LineParser lineParser = new LineParser();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    if (eofCallback != null) {
                        eofCallback.run();
                    }
                    break;
                }

                if (!line.isEmpty() && lineParser.consumeImports(new StringReader(line), importCollector)) {
                    if (enterBodyCallback != null) {
                        enterBodyCallback.accept(line);
                    }
                    break;
                }
            }
        }
    }
}
