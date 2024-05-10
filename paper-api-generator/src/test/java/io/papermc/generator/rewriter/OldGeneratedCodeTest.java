package io.papermc.generator.rewriter;

import io.papermc.generator.Generators;
import io.papermc.generator.rewriter.parser.StringReader;
import io.papermc.generator.rewriter.replace.CommentMarker;
import io.papermc.generator.rewriter.replace.SearchReplaceRewriter;
import io.papermc.generator.rewriter.utils.Annotations;
import io.papermc.paper.generated.GeneratedFrom;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.papermc.generator.rewriter.replace.CommentMarker.EMPTY_MARKER;
import static io.papermc.generator.SourceWriter.INDENT_CHAR;
import static io.papermc.generator.SourceWriter.INDENT_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OldGeneratedCodeTest {

    private static final String CURRENT_VERSION;

    static {
        SharedConstants.tryDetectVersion();
        CURRENT_VERSION = SharedConstants.getCurrentVersion().getName();
    }

    public static void main(String[] args) throws IOException {
        checkOutdated(Path.of(args[0]), Generators.API_REWRITE);
        checkOutdated(Path.of(args[1]), Generators.SERVER_REWRITE);
    }

    private static void checkOutdated(Path container, SourceRewriter[] rewriters) throws IOException {
        for (SourceRewriter rewriter : rewriters) {
            if (!(rewriter instanceof SearchReplaceRewriter srt) || !srt.isVersionDependant()) {
                continue;
            }

            Path path = container.resolve(srt.getRelativeFilePath());
            if (Files.notExists(path)) { // todo (softspoon): remove after
                continue;
            }
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                int lineCount = 0;
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    lineCount++;
                    if (line.isEmpty()) {
                        continue;
                    }

                    StringReader lineIterator = new StringReader(line);
                    CommentMarker marker = srt.searchMarker(lineIterator, null, null);
                    if (marker != EMPTY_MARKER) {
                        if (!marker.start()) {
                            continue;
                        }
                        int startIndentSize = marker.indent();
                        if (startIndentSize % INDENT_SIZE != 0) {
                            continue;
                        }

                        String nextLine = reader.readLine();
                        if (nextLine == null) {
                            break;
                        }
                        lineCount++;
                        if (nextLine.isEmpty()) {
                            continue;
                        }

                        StringReader nextLineIterator = new StringReader(nextLine);
                        int indentSize = nextLineIterator.skipChars(INDENT_CHAR);
                        if (indentSize != startIndentSize) {
                            continue;
                        }

                        String generatedComment = "// %s ".formatted(Annotations.annotationStyle(GeneratedFrom.class));
                        if (nextLineIterator.trySkipString(generatedComment) && nextLineIterator.canRead()) {
                            String generatedVersion = nextLineIterator.getRemaining();
                            assertEquals(CURRENT_VERSION, generatedVersion,
                                "Code at line %s in %s is marked as being generated in version %s when the current version is %s".formatted(
                                    lineCount, srt.getRewrittenClass().canonicalName(),
                                    generatedVersion, CURRENT_VERSION));
                        }
                    }
                }
            }
        }
    }
}
