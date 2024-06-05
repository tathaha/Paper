package io.papermc.generator.rewriter.registration;

import io.papermc.generator.rewriter.SourceFile;
import io.papermc.generator.rewriter.SourceRewriter;
import org.jetbrains.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public interface SourceSetRewriter<T extends SourceSetRewriter<T>> {

    @VisibleForTesting
    Map<SourceFile, SourceRewriter> getRewriters();

    T register(SourceFile source, SourceRewriter rewriter);

    void apply(Path output) throws IOException;
}
