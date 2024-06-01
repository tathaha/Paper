package io.papermc.generator.rewriter.registration;

import io.papermc.generator.rewriter.SourceFile;
import io.papermc.generator.rewriter.SourceRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SourceSetRewriterImpl<T extends SourceSetRewriter<T>> implements SourceSetRewriter<T> {

    protected final Map<SourceFile, SourceRewriter> rewrites = new HashMap<>();

    @Deprecated
    private final Path alternateOutput;

    @Deprecated
    public SourceSetRewriterImpl(@Nullable Path alternateOutput) {
        this.alternateOutput = alternateOutput;
    }

    @Deprecated
    public Path getAlternateOutput() {
        return this.alternateOutput;
    }

    @Override
    public Map<SourceFile, SourceRewriter> getRewriters() {
        return Collections.unmodifiableMap(this.rewrites);
    }

    @Override
    public T register(final SourceFile source, final SourceRewriter rewriter) {
        if (rewriter.registerFor(source)) {
            this.rewrites.put(source, rewriter);
        }
        return (T) this;
    }

    @Override
    public void apply(final Path output) throws IOException {
        for (Map.Entry<SourceFile, SourceRewriter> rewriter : this.rewrites.entrySet()) {
            if (this.alternateOutput == null) {
                rewriter.getValue().writeToFile(output, rewriter.getKey());
            } else {
                rewriter.getValue().writeToFile(output, this.alternateOutput, rewriter.getKey());
            }
        }
    }
}
