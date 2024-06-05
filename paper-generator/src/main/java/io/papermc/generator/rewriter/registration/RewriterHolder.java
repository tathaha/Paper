package io.papermc.generator.rewriter.registration;

import io.papermc.generator.rewriter.replace.ReplaceOptionsLike;
import io.papermc.generator.rewriter.replace.SearchReplaceRewriter;
import org.jetbrains.annotations.Contract;
import java.util.function.Function;

public record RewriterHolder(String pattern, SearchReplaceRewriter rewriter) {

    @Contract(value = "_, _ -> new", pure = true)
    public static RewriterHolder holder(String pattern, SearchReplaceRewriter rewriter) {
        return new RewriterHolder(pattern, rewriter);
    }

    public SearchReplaceRewriter transform(Function<String, ReplaceOptionsLike> patternMapper) {
        return this.rewriter.withOptions(patternMapper.apply(this.pattern)).customName(this.pattern);
    }
}
