package io.papermc.generator.rewriter.registration;

import io.papermc.generator.rewriter.ClassNamed;
import io.papermc.generator.rewriter.SourceFile;
import io.papermc.generator.rewriter.replace.CompositeRewriter;
import io.papermc.generator.rewriter.IndentUnit;
import io.papermc.generator.rewriter.replace.ReplaceOptions;
import io.papermc.generator.rewriter.replace.ReplaceOptionsLike;
import io.papermc.generator.rewriter.replace.SearchReplaceRewriter;
import io.papermc.generator.rewriter.utils.Annotations;
import io.papermc.generator.types.SimpleGenerator;
import io.papermc.paper.generated.GeneratedFrom;
import net.minecraft.SharedConstants;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import java.nio.file.Path;
import java.util.Arrays;

@DefaultQualifier(NonNull.class)
public class PaperPatternSourceSetRewriter extends SourceSetRewriterImpl<PatternSourceSetRewriter> implements PatternSourceSetRewriter {

    private static final String PAPER_START_FORMAT = "Paper start";
    private static final String PAPER_END_FORMAT = "Paper end";
    private static final String SEARCH_COMMENT_MARKER_FORMAT = "%s - Generated/%s"; // {0} = PAPER_START_FORMAT|PAPER_END_FORMAT {1} = pattern
    private static final IndentUnit INDENT_UNIT = IndentUnit.parse(SimpleGenerator.INDENT_UNIT);

    public PaperPatternSourceSetRewriter(final Path alternateOutput) {
        super(alternateOutput);
    }

    private static ReplaceOptionsLike getOptions(String pattern) {
        return ReplaceOptions.between(
                SEARCH_COMMENT_MARKER_FORMAT.formatted(PAPER_START_FORMAT, pattern),
                SEARCH_COMMENT_MARKER_FORMAT.formatted(PAPER_END_FORMAT, pattern)
            )
            .generatedComment(Annotations.annotationStyle(GeneratedFrom.class) + " " + SharedConstants.getCurrentVersion().getName());
    }

    @Override
    public PatternSourceSetRewriter register(final String pattern, final ClassNamed rewriteClass, final SearchReplaceRewriter rewriter) {
        if (rewriter.getRewrittenClass() == null) {
            rewriter.rewriteClass(rewriteClass);
        }

        return this.register(pattern, SourceFile.of(rewriteClass.root(), INDENT_UNIT), rewriter);
    }

    @Override
    public PatternSourceSetRewriter register(final String pattern, final SourceFile source, final SearchReplaceRewriter rewriter) {
        return super.register(source, rewriter.withOptions(getOptions(pattern)).customName(pattern));
    }

    @Override
    public PatternSourceSetRewriter register(final ClassNamed mainClass, final CompositeRewriter rewriter) {
        return super.register(SourceFile.of(mainClass, INDENT_UNIT), rewriter);
    }

    public static CompositeRewriter composite(final RewriterHolder... holders) {
        return CompositeRewriter.bind(Arrays.stream(holders)
            .map(holder -> holder.transform(PaperPatternSourceSetRewriter::getOptions))
            .toArray(SearchReplaceRewriter[]::new));
    }
}
