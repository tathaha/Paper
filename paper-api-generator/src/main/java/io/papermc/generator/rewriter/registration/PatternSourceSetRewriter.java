package io.papermc.generator.rewriter.registration;

import io.papermc.generator.rewriter.ClassNamed;
import io.papermc.generator.rewriter.SourceFile;
import io.papermc.generator.rewriter.replace.CompositeRewriter;
import io.papermc.generator.rewriter.replace.SearchReplaceRewriter;
import io.papermc.generator.rewriter.types.EnumRewriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface PatternSourceSetRewriter extends SourceSetRewriter<PatternSourceSetRewriter> {

    default PatternSourceSetRewriter register(final String pattern, final Class<?> rewriteClass, final SearchReplaceRewriter rewriter) {
        return register(pattern, new ClassNamed(rewriteClass), rewriter);
    }

    PatternSourceSetRewriter register(final String pattern, final ClassNamed rewriteClass, final SearchReplaceRewriter rewriter);

    // inferred generic
    default <T, A extends Enum<A>> PatternSourceSetRewriter register(final String pattern, final Class<A> rewriteClass, final EnumRewriter<T, A> rewriter) {
        return this.register(pattern, rewriteClass, (SearchReplaceRewriter) rewriter);
    }

    PatternSourceSetRewriter register(final String pattern, final SourceFile source, final SearchReplaceRewriter rewriter);

    default PatternSourceSetRewriter register(final Class<?> mainClass, final CompositeRewriter rewriter) {
        return this.register(new ClassNamed(mainClass), rewriter);
    }

    PatternSourceSetRewriter register(final ClassNamed mainClass, final CompositeRewriter rewriter);
}
