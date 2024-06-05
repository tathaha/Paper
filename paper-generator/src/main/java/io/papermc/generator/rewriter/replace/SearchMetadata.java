package io.papermc.generator.rewriter.replace;

import io.papermc.generator.rewriter.SourceFile;
import io.papermc.generator.rewriter.context.ImportCollector;

/**
 * Search metadata bound to a rewriter with the data of the current replacement.
 *
 * @param source the source file where the search happen
 * @param importCollector the import collector with the import statements collected (only if the replacement happens after the imports!)
 * @param indent the indent adjusted by the start comment marker
 * @param replacedContent the replaced content
 * @param line the line number approximately starting from 0 or -1 during a dump
 */
public record SearchMetadata(SourceFile source, ImportCollector importCollector, String indent, String replacedContent, int line) {
}
