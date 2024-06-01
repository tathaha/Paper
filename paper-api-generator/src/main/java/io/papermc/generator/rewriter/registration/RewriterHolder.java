package io.papermc.generator.rewriter.registration;

import io.papermc.generator.rewriter.replace.ReplaceOptionsLike;
import io.papermc.generator.rewriter.replace.SearchReplaceRewriter;
import io.papermc.generator.rewriter.types.EnumRewriter;
import java.util.function.Function;

public record RewriterHolder(String pattern, SearchReplaceRewriter rewriter) {

    public static RewriterHolder holder(String pattern, Class<?> rewriteClass, SearchReplaceRewriter rewriter) {
        return holder(pattern, rewriter.rewriteClass(rewriteClass));
    }

    // inferred generic
    public static <T, A extends Enum<A>> RewriterHolder holder(String pattern, Class<A> rewriteClass, EnumRewriter<T, A> rewriter) {
        return holder(pattern, rewriteClass, (SearchReplaceRewriter) rewriter);
    }

    public static RewriterHolder holder(String pattern, SearchReplaceRewriter rewriter) {
        return new RewriterHolder(pattern, rewriter);
    }

    public SearchReplaceRewriter transform(Function<String, ReplaceOptionsLike> patternMapper) {
        return this.rewriter.withOptions(patternMapper.apply(this.pattern)).customName(this.pattern);
    }
}
