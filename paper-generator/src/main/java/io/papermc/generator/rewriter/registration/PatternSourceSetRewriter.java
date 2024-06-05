package io.papermc.generator.rewriter.registration;

import io.papermc.generator.rewriter.ClassNamed;
import io.papermc.generator.rewriter.replace.CompositeRewriter;
import io.papermc.generator.rewriter.replace.SearchReplaceRewriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface PatternSourceSetRewriter extends SourceSetRewriter<PatternSourceSetRewriter> {

    default PatternSourceSetRewriter register(final String pattern, final Class<?> mainClass, final SearchReplaceRewriter rewriter) {
        return register(pattern, new ClassNamed(mainClass), rewriter);
    }

    PatternSourceSetRewriter register(final String pattern, final ClassNamed mainClass, final SearchReplaceRewriter rewriter);

    default PatternSourceSetRewriter register(final Class<?> mainClass, final CompositeRewriter rewriter) {
        return this.register(new ClassNamed(mainClass), rewriter);
    }

    PatternSourceSetRewriter register(final ClassNamed mainClass, final CompositeRewriter rewriter);
}
