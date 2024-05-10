package io.papermc.generator.rewriter.replace;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.papermc.generator.Main;
import io.papermc.generator.rewriter.ClassNamed;
import io.papermc.generator.rewriter.SourceRewriter;
import io.papermc.generator.rewriter.parser.LineParser;
import io.papermc.generator.rewriter.utils.Annotations;
import io.papermc.generator.rewriter.context.ImportCollector;
import io.papermc.generator.rewriter.context.ImportTypeCollector;
import io.papermc.generator.rewriter.parser.StringReader;
import io.papermc.generator.utils.Formatting;
import io.papermc.paper.generated.GeneratedFrom;
import net.minecraft.SharedConstants;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static io.papermc.generator.rewriter.replace.CommentMarker.EMPTY_MARKER;

public class SearchReplaceRewriter implements SourceRewriter {

    private static final Logger LOGGER = LogUtils.getLogger();

    protected final ClassNamed rewriteClass;
    protected final String pattern;
    private final boolean exactReplacement;

    public SearchReplaceRewriter(Class<?> rewriteClass, String pattern, boolean exactReplacement) {
        this(new ClassNamed(rewriteClass), pattern, exactReplacement);
    }

    public SearchReplaceRewriter(ClassNamed rewriteClass, String pattern, boolean exactReplacement) {
        this.rewriteClass = rewriteClass;
        this.pattern = pattern;
        this.exactReplacement = exactReplacement;
    }

    // only when exactReplacement = false
    @ApiStatus.OverrideOnly
    protected void insert(SearchMetadata metadata, StringBuilder builder) {
        throw new UnsupportedOperationException("This rewriter (" + this.getClass().getCanonicalName() + ") doesn't support removal and insertion!");
    }

    // only when exactReplacement = true
    @ApiStatus.OverrideOnly
    protected void replaceLine(SearchMetadata metadata, StringBuilder builder) {
        throw new UnsupportedOperationException("This rewriter (" + this.getClass().getCanonicalName() + ") doesn't support exact replacement!");
    }

    protected void beginSearch() {}

    protected SearchReplaceRewriter getRewriterFor(String pattern) {
        return this;
    }

    protected Set<String> getPatterns() {
        return Set.of(this.pattern);
    }

    private void searchAndReplace(BufferedReader reader, StringBuilder content) throws IOException {
        Set<String> patterns = this.getPatterns();
        Preconditions.checkState(!patterns.isEmpty());
        this.beginSearch();

        Set<String> remainingPatterns = new HashSet<>(patterns);
        StringBuilder strippedContent = null;

        ClassNamed root = this.rewriteClass.root();
        final ImportCollector importCollector = new ImportTypeCollector(root);
        final LineParser lineParser = new LineParser();

        String indent = Formatting.incrementalIndent(INDENT_UNIT, this.rewriteClass);
        SearchReplaceRewriter foundRewriter = null;
        boolean inBody = false;
        int i = 0;
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            StringReader lineIterator = new StringReader(line);
            // collect import to avoid fqn when not needed
            int previousCursor = lineIterator.getCursor();
            if (importCollector != ImportCollector.NO_OP && !inBody && foundRewriter == null && !line.isEmpty()) {
                if (lineParser.consumeImports(lineIterator, importCollector)) {
                    inBody = true;
                }
            }
            lineIterator.setCursor(previousCursor);

            CommentMarker marker = EMPTY_MARKER;
            if (!line.isEmpty()) {
                marker = this.searchMarker(lineIterator, foundRewriter == null ? null : indent, remainingPatterns); // change this to patterns if needed to allow two rewrite of the same pattern in the same file
            }

            if (marker != EMPTY_MARKER) {
                if (!marker.start()) {
                    if (foundRewriter == null) {
                        throw new IllegalStateException("Generated start comment is missing for pattern " + marker.pattern() + " in " + this.rewriteClass.canonicalName() + " at line " + (i + 1));
                    }
                    if (foundRewriter.pattern.equals(marker.pattern())) {
                        if (!foundRewriter.exactReplacement) {
                            appendGeneratedComment(content, indent);

                            foundRewriter.insert(new SearchMetadata(importCollector, indent, strippedContent.toString(), i), content);
                            strippedContent = null;
                        }
                        remainingPatterns.remove(foundRewriter.pattern);
                        foundRewriter = null;
                        // regular line
                        content.append(line);
                        content.append('\n');
                    } else {
                        throw new IllegalStateException("Generated end comment doesn't match for pattern " + foundRewriter.pattern + " in " + this.rewriteClass.canonicalName() + " at line " + (i + 1));
                    }
                } else {
                    if (foundRewriter != null) {
                        throw new IllegalStateException("Nested generated comments are not allowed for " + this.rewriteClass.canonicalName() + " at line " + (i + 1));
                    }
                    if (marker.indent() % INDENT_SIZE != 0) {
                        throw new IllegalStateException("Generated start comment is not properly indented at line " + (i + 1) + " for pattern " + marker.pattern() + " in " + this.rewriteClass.canonicalName());
                    }
                    indent = " ".repeat(marker.indent()); // update indent based on the comments for flexibility

                    foundRewriter = this.getRewriterFor(marker.pattern());
                    if (!foundRewriter.exactReplacement) {
                        strippedContent = new StringBuilder();
                    }
                    // regular line
                    content.append(line);
                    content.append('\n');
                }
            } else {
                if (foundRewriter == null) {
                    content.append(line);
                    content.append('\n');
                } else {
                    if (foundRewriter.exactReplacement) {
                        // there's no generated comment here since when the size is equals the replaced content doesn't depend on the game content
                        // if it does that means the replaced content might not be equals during MC update because of adding/removed content
                        foundRewriter.replaceLine(new SearchMetadata(importCollector, indent, line, i), content);
                    } else {
                        strippedContent.append(line);
                        strippedContent.append('\n');
                    }
                }
            }
            i++;
        }

        if (foundRewriter != null) {
            throw new IllegalStateException("Generated end comment is missing for pattern " + foundRewriter.pattern + " in " + this.rewriteClass.canonicalName());
        }

        if (!remainingPatterns.isEmpty()) {
            throw new IllegalStateException("SRT didn't found some expected generated comment for the following patterns: " + remainingPatterns.toString());
        }
    }

    public String getRelativeFilePath() {
        return "%s/%s.java".formatted(
            this.rewriteClass.packageName().replace('.', '/'),
            this.rewriteClass.root().simpleName()
        );
    }

    public boolean isVersionDependant() {
        return !this.exactReplacement;
    }

    public ClassNamed getRewrittenClass() {
        return this.rewriteClass;
    }

    @Override
    public void writeToFile(Path parent) throws IOException {
        String filePath = this.getRelativeFilePath();

        Path path = parent.resolve(filePath);
        final Path createdPath;
        StringBuilder content = new StringBuilder();

        if (Files.isRegularFile(path)) {
            try (BufferedReader buffer = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                this.searchAndReplace(buffer, content);
            }
        } else if (!this.exactReplacement) {
            LOGGER.warn("Target source file '{}' doesn't exists, dumping rewriters data instead...", filePath);
            this.dumpAll(content);
            filePath += ".dump";
            path = parent.resolve(filePath);
        }

        // Files.writeString(path, content.toString(), StandardCharsets.UTF_8); // todo
        if (path.toString().contains("Paper/Paper-API/src/")) {
            createdPath = Main.generatedPath.resolve(filePath);
        } else {
            createdPath = Main.generatedServerPath.resolve(filePath);
        }

        Files.createDirectories(createdPath.getParent());
        Files.writeString(createdPath, content, StandardCharsets.UTF_8);
    }

    @Override
    public void dump(StringBuilder content) {
        content.append('\n');
        content.append(this.pattern);
        content.append('\n');
        content.append("Start comment marker : ").append(SEARCH_COMMENT_MARKER_FORMAT.formatted(PAPER_START_FORMAT, this.pattern));
        content.append('\n');
        content.append("End comment marker : ").append(SEARCH_COMMENT_MARKER_FORMAT.formatted(PAPER_END_FORMAT, this.pattern));
        content.append('\n');
        content.append('\n');

        content.append(">".repeat(30));
        content.append('\n');

        this.insert(new SearchMetadata(ImportCollector.NO_OP, INDENT_UNIT, "", -1), content);

        content.append("<".repeat(30));
        content.append('\n');
    }

    public void dumpAll(StringBuilder content) {
        content.append("Dump of the rewriters that apply to the file : ").append(this.getRelativeFilePath());
        content.append('\n');
        content.append('\n');

        content.append("Configuration :");
        content.append('\n');
        content.append("Indent unit : \"").append(INDENT_UNIT).append("\" (").append(INDENT_SIZE).append(" char)");
        content.append('\n');
        content.append("Indent char : '").append(INDENT_CHAR).append("' (").append(INDENT_UNIT.codePointAt(0)).append(")");
        content.append('\n');

        this.dump(content);
    }

    private void appendGeneratedComment(StringBuilder builder, String indent) {
        builder.append(indent).append("// %s %s".formatted(
            Annotations.annotationStyle(GeneratedFrom.class),
            SharedConstants.getCurrentVersion().getName()
        ));
        builder.append('\n');
    }

    public CommentMarker searchMarker(StringReader lineIterator, @Nullable String indent, @Nullable Set<String> patterns) {
        boolean strict = indent != null;
        final int indentSize;
        if (strict) {
            if (!indent.isEmpty() && !lineIterator.trySkipChars(indent.length(), indent.charAt(0))) {
                return EMPTY_MARKER;
            }
            indentSize = indent.length();
        } else {
            indentSize = lineIterator.skipChars(INDENT_CHAR);
        }

        boolean foundStart = lineIterator.trySkipString(SEARCH_COMMENT_MARKER_FORMAT.formatted(PAPER_START_FORMAT, ""));
        boolean foundEnd = !foundStart && lineIterator.trySkipString(SEARCH_COMMENT_MARKER_FORMAT.formatted(PAPER_END_FORMAT, ""));
        if (!foundStart && !foundEnd) {
            return EMPTY_MARKER;
        }

        String pattern = lineIterator.getRemaining();
        if (patterns == null || patterns.contains(pattern)) { // patterns will be null only for tests
            return new CommentMarker(pattern, foundStart, indentSize);
        }
        return EMPTY_MARKER;
    }
}
