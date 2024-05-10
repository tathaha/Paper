package io.papermc.generator.rewriter;

import io.papermc.generator.SourceWriter;

public interface SourceRewriter extends SourceWriter {

    String PAPER_START_FORMAT = "Paper start";
    String PAPER_END_FORMAT = "Paper end";
    String SEARCH_COMMENT_MARKER_FORMAT = "// %s - Generated/%s"; // {0} = PAPER_START_FORMAT|PAPER_END_FORMAT {1} = pattern

    void dump(StringBuilder into);
}
