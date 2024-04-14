package io.papermc.generator.rewriter.parser.step.model;

import io.papermc.generator.rewriter.context.ImportCollector;
import io.papermc.generator.rewriter.parser.LineParser;
import io.papermc.generator.rewriter.parser.ProtoTypeName;
import io.papermc.generator.rewriter.parser.step.IterativeStep;
import io.papermc.generator.rewriter.parser.step.StepHolder;
import io.papermc.generator.rewriter.parser.StringReader;
import io.papermc.generator.utils.NamingManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

// start once "import" is detected unless commented
// order is: enforceSpace -> checkStatic (-> enforceSpace) -> getPartName (-> skipUntilSemicolonAfterStar) -> collectImport
public final class ImportSteps implements StepHolder {

    public static boolean canStart(StringReader line) {
        return line.trySkipString("import");
    }

    private final IterativeStep enforceSpaceStep = IterativeStep.create(this::enforceSpace);
    private final IterativeStep skipUntilSemicolonAfterStarStep = IterativeStep.createUntil(this::skipUntilSemicolonAfterStar);

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
        if (name.isEmpty()) {
            throw new IllegalStateException("Invalid java source, import type name is empty!");
        }
        if (!NamingManager.isValidName(name)) { // keyword are checked after to simplify things
            throw new IllegalStateException("Invalid java source, import type name contains a reserved keyword or a syntax error!");
        }

        if (this.isStatic) {
            this.collector.addStaticImport(name);
        } else {
            this.collector.addImport(name);
        }
    }

    public boolean skipUntilSemicolonAfterStar(StringReader line, LineParser parser) {
        parser.skipCommentOrWhitespace(line);
        if (!line.canRead()) {
            return true;
        }

        if (parser.nextSingleLineComment(line)) {
            line.setCursor(line.getTotalLength());
        }

        if (line.canRead() && line.peek() == ';') {
            this.name.append('*');
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

        this.name = line.getPartNameUntil(';', parser::skipCommentOrWhitespace, this.name);

        if (line.canRead()) {
            if (this.name.getLastChar() == '.' && line.peek() == '*') {
                line.skip();
                parser.getSteps().addPriority(this.skipUntilSemicolonAfterStarStep);
                return false;
            } else if (parser.nextSingleLineComment(line)) {
                // ignore single line comment at the end of the name
                line.setCursor(line.getTotalLength());
            }
        }

        return !line.canRead();
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
