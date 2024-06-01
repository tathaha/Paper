package io.papermc.generator.rewriter.replace;

import com.google.common.base.Preconditions;
import io.papermc.generator.rewriter.ClassNamed;
import io.papermc.generator.rewriter.IndentUnit;
import io.papermc.generator.rewriter.SourceFile;
import io.papermc.generator.rewriter.SourceRewriter;
import io.papermc.generator.rewriter.parser.LineParser;
import io.papermc.generator.rewriter.context.ImportCollector;
import io.papermc.generator.rewriter.context.ImportTypeCollector;
import io.papermc.generator.rewriter.parser.StringReader;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static io.papermc.generator.rewriter.replace.CommentMarker.EMPTY_MARKER;

@DefaultQualifier(NonNull.class)
public class SearchReplaceRewriter implements SourceRewriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchReplaceRewriter.class);

    protected @MonotonicNonNull ClassNamed rewriteClass;
    protected @MonotonicNonNull String name;
    protected @MonotonicNonNull ReplaceOptions options;

    public SearchReplaceRewriter withOptions(ReplaceOptionsLike options) {
        this.options = options.asOption();
        return this;
    }

    public SearchReplaceRewriter rewriteClass(Class<?> rewriteClass) {
        return this.rewriteClass(new ClassNamed(rewriteClass));
    }

    public SearchReplaceRewriter rewriteClass(ClassNamed rewriteClass) {
        this.rewriteClass = rewriteClass;
        if (this.name == null) {
            this.name = rewriteClass.simpleName();
        }
        return this;
    }

    public SearchReplaceRewriter customName(String name) {
        this.name = name;
        return this;
    }

    public @NonNull String getName() {
        return this.name;
    }

    public @NonNull ReplaceOptions getOptions() {
        return this.options;
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

    @VisibleForTesting
    public Set<SearchReplaceRewriter> getRewriters() {
        return Set.of(this);
    }

    private static void searchAndReplace(SourceFile source, Set<SearchReplaceRewriter> rewriters, BufferedReader reader, StringBuilder content) throws IOException {
        Preconditions.checkState(!rewriters.isEmpty());

        Set<SearchReplaceRewriter> remainingRewriters = new HashSet<>(rewriters);
        Set<SearchReplaceRewriter> unusedRewriters = new HashSet<>(rewriters);
        @Nullable StringBuilder strippedContent = null;

        ClassNamed root = source.mainClass().root();
        final ImportCollector importCollector = new ImportTypeCollector(root);
        final LineParser lineParser = new LineParser();

        @Nullable String indent = null;
        @Nullable SearchReplaceRewriter foundRewriter = null;
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
                marker = searchMarker(
                    lineIterator,
                    foundRewriter == null ? null : indent,
                    source.indentUnit(),
                    remainingRewriters,
                    foundRewriter == null
                );
            }

            if (marker != EMPTY_MARKER) {
                if (foundRewriter != null) {
                    if (!marker.owner().equals(foundRewriter)) {
                        throw new IllegalStateException("Generated end comment doesn't match for rewriter " + foundRewriter.getName() + " in " + foundRewriter.getRewrittenClass().canonicalName() + " at line " + (i + 1));
                    }

                    if (!foundRewriter.options.exactReplacement()) {
                        // append generated comment
                        if (foundRewriter.options.generatedComment().isPresent()) {
                            content.append(indent).append("// ").append(foundRewriter.options.generatedComment().get());
                            content.append('\n');
                        }

                        foundRewriter.insert(new SearchMetadata(source, importCollector, indent, strippedContent.toString(), i), content);
                        strippedContent = null;
                    }
                    if (!foundRewriter.options.multipleOperation()) {
                        remainingRewriters.remove(foundRewriter);
                    }
                    unusedRewriters.remove(foundRewriter);
                    foundRewriter = null;
                } else {
                    if (marker.indent() % source.indentUnit().size() != 0) {
                        throw new IllegalStateException("Generated start comment is not properly indented at line " + (i + 1) + " for rewriter " + marker.owner().getName() + " in " + marker.owner().rewriteClass.canonicalName());
                    }
                    indent = " ".repeat(marker.indent()); // update indent based on the comments for flexibility

                    foundRewriter = marker.owner();
                    if (!foundRewriter.options.exactReplacement()) {
                        strippedContent = new StringBuilder();
                    }
                }
            }

            @Nullable StringBuilder usedBuilder = null;
            if (marker == EMPTY_MARKER && foundRewriter != null) {
                if (foundRewriter.options.exactReplacement()) {
                    // there's no generated comment here since when the size is equals the replaced content doesn't depend on the game content
                    // if it does that means the replaced content might not be equals during MC update because of adding/removed content
                    foundRewriter.replaceLine(new SearchMetadata(source, importCollector, indent, line, i), content);
                } else {
                    usedBuilder = strippedContent;
                }
            } else {
                usedBuilder = content;
            }
            if (usedBuilder != null) {
                usedBuilder.append(line);
                usedBuilder.append('\n');
            }
            i++;
        }

        if (foundRewriter != null) {
            throw new IllegalStateException("Generated end comment is missing for rewriter " + foundRewriter.getName() + " in " + foundRewriter.getRewrittenClass().canonicalName());
        }

        if (!unusedRewriters.isEmpty()) {
            throw new IllegalStateException("SRT didn't found some expected generated comment for the following rewriters: " + unusedRewriters.stream().map(SearchReplaceRewriter::getName).toList());
        }
    }

    @VisibleForTesting
    public boolean hasGeneratedComment() {
        return !this.options.exactReplacement() && this.options.generatedComment().isPresent();
    }

    public ClassNamed getRewrittenClass() {
        return this.rewriteClass;
    }

    @Override
    public boolean registerFor(SourceFile file) {
        if (this.rewriteClass == null) {
            this.rewriteClass(file.mainClass());
        }

        if (this.options == null) {
            LOGGER.error("Replace options are not defined, skipping the rewriter: {}", this.getName());
            return false;
        }
        return true;
    }

    @Override
    public void writeToFile(Path parent, Path writeFolder, SourceFile file) throws IOException {
        Path filePath = file.path();

        Path path = parent.resolve(filePath);
        StringBuilder content = new StringBuilder();

        if (Files.isRegularFile(path)) {
            try (BufferedReader buffer = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                searchAndReplace(file, this.getRewriters(), buffer, content);
            }
        } else {
            LOGGER.warn("Target source file '{}' doesn't exists, dumping rewriters data instead...", filePath);
            dumpAll(this, file, content);
            filePath = filePath.resolveSibling(filePath.getFileName() + ".dump");
            path = parent.resolve(filePath);
        }

        if (!writeFolder.equals(parent)) { // todo remove
            path = writeFolder.resolve(filePath);
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    @Override
    public void dump(SourceFile file, StringBuilder content) {
        content.append("Name : ").append(this.name);

        content.append('\n');
        content.append("Start comment marker : ").append(this.options.startCommentMarker());
        content.append('\n');
        content.append("End comment marker : ").append(this.options.endCommentMarker());
        content.append('\n');
        content.append('\n');

        if (this.options.exactReplacement()) {
            content.append("[This rewriter only works for exact replacement and cannot be dump safely]");
        } else {
            content.append(">".repeat(30));
            content.append('\n');

            this.insert(new SearchMetadata(file, ImportCollector.NO_OP, file.indentUnit().content(), "", -1), content);

            content.append("<".repeat(30));
            content.append('\n');
        }
    }

    public static void dumpAll(SearchReplaceRewriter rewriter, SourceFile file, StringBuilder content) {
        content.append("Dump of the rewriters that apply to the file : ").append(file.path());
        content.append('\n');
        content.append('\n');

        content.append("Configuration :");
        content.append('\n');
        content.append("Indent unit : \"").append(file.indentUnit().content()).append("\" (").append(file.indentUnit().size()).append(" char)");
        content.append('\n');
        content.append("Indent char : '").append(file.indentUnit().character()).append("' (").append(file.indentUnit().content().codePointAt(0)).append(")");
        content.append('\n');
        content.append('\n');

        for (SearchReplaceRewriter subRewriter : rewriter.getRewriters()) {
            subRewriter.dump(file, content);
            content.append('\n');
        }
    }

    public static CommentMarker searchMarker(StringReader lineIterator, @Nullable String indent, IndentUnit indentUnit, Set<SearchReplaceRewriter> remainingRewriters, boolean searchStart) {
        boolean strict = indent != null;
        final int indentSize;
        if (strict) {
            if (!indent.isEmpty() && !lineIterator.trySkipChars(indent.length(), indent.charAt(0))) {
                return EMPTY_MARKER;
            }
            indentSize = indent.length();
        } else {
            indentSize = lineIterator.skipChars(indentUnit.character());
        }

        if (!lineIterator.trySkipString("// ")) {
            return EMPTY_MARKER;
        }

        @Nullable SearchReplaceRewriter result = null;
        for (SearchReplaceRewriter rewriter : remainingRewriters) {
            boolean found = lineIterator.trySkipString(searchStart ? rewriter.options.startCommentMarker() : rewriter.options.endCommentMarker());
            if (found) {
                result = rewriter;
                break;
            }
        }

        if (lineIterator.canRead() || result == null) {
            return EMPTY_MARKER;
        }

        return new CommentMarker(result, indentSize);
    }
}
