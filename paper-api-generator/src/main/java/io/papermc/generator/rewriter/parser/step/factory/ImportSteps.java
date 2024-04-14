package io.papermc.generator.rewriter.parser.step.factory;

import io.papermc.generator.rewriter.context.ImportCollector;
import io.papermc.generator.rewriter.parser.LineParser;
import io.papermc.generator.rewriter.parser.ProtoTypeName;
import io.papermc.generator.rewriter.parser.step.IterativeStep;
import io.papermc.generator.rewriter.parser.step.StepHolder;
import io.papermc.generator.rewriter.parser.StringReader;
import io.papermc.generator.utils.NamingManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

// start once "import" is detected unless commented
// order is: enforceSpace -> checkStatic (-> enforceSpace) -> getPartName -> collectImport
public final class ImportSteps implements StepHolder {

    public static boolean canStart(StringReader line) {
        return line.trySkipString("import");
    }

    private final IterativeStep enforceSpaceStep = IterativeStep.create(this::enforceSpace);
    private final IterativeStep skipUntilSemicolonStep = IterativeStep.createUntil(this::skipUntilSemicolon);

    private final ImportCollector collector;
    private boolean isStatic;
    private @MonotonicNonNull ProtoTypeName name;

    public ImportSteps(ImportCollector collector) {
        this.collector = collector;
    }

    public void enforceSpace(StringReader line, LineParser parser) {
        if (line.canRead() && parser.nextSingleLineComment(line)) {
            // ignore single line comment at the end of import/static
            line.setCursor(line.getTotalLength());
            return;
        }

        if (!parser.skipComment(line)) {
            if (line.skipWhitespace() == 0) { // expect at least one space between import, static and type name unless a multi comment is here to fill the gap
                parser.getSteps().clearRemaining();
            }
        }
    }

    public boolean checkStatic(StringReader line, LineParser parser) {
        parser.skipCommentOrWhitespace(line);
        if (!line.canRead()) {
            return true;
        }

        if (line.trySkipString("static")) {
            parser.getSteps().addPriority(this.enforceSpaceStep);
            this.isStatic = true;
        }
        return false;
    }

    public void collectImport(StringReader line, LineParser parser) {
        String name = this.name.getFinalName();
        if (name.isEmpty() || NamingManager.hasIllegalKeyword(name)) { // keyword are checked after to simplify things
            return;
        }

        if (this.isStatic) {
            this.collector.addStaticImport(name);
        } else {
            this.collector.addImport(name);
        }
    }

    public boolean skipUntilSemicolon(StringReader line, LineParser parser) {
        // this is only executed once for star imports!
        parser.skipCommentOrWhitespace(line);
        if (!line.canRead()) {
            return true;
        }

        if (parser.nextSingleLineComment(line)) {
            line.setCursor(line.getTotalLength());
        } else {
            line.skipStringUntil(';');
        }

        if (line.canRead() && line.peek() == ';') {
            this.pushToImportName("*");
            line.skip();
            return false;
        }

        if (line.canRead()) {
            throw new IllegalStateException("Invalid java source, found a '*' char in the middle of import type name!");
        }

        return true;
    }

    public boolean getPartName(StringReader line, LineParser parser) {
        parser.skipCommentOrWhitespace(line);
        if (!line.canRead()) {
            return true;
        }

        String name = line.getPartNameUntil(';', parser::skipCommentOrWhitespace, this.name);
        boolean isGlobal = false;
        if (line.canRead()) {
            if (line.peek() == '*') {
                isGlobal = true;
                line.skip();
            } else if (parser.nextSingleLineComment(line)) {
                // ignore single line comment at the end of the name
                line.setCursor(line.getTotalLength());
            }
        }

        this.pushToImportName(name);

        if (isGlobal) {
            parser.getSteps().addPriority(this.skipUntilSemicolonStep);
            return false;
        }

        return !line.canRead();
    }

    private void pushToImportName(String name) {
        if (this.name == null) {
            this.name = ProtoTypeName.create(name);
        } else {
            this.name.append(name);
        }
    }

    @Override
    public IterativeStep[] initialSteps() {
        return new IterativeStep[] {
            this.enforceSpaceStep,
            IterativeStep.createUntil(this::checkStatic),
            IterativeStep.createUntil(this::getPartName),
            IterativeStep.create(this::collectImport)
        };
    }
}
