package io.papermc.generator.rewriter.parser.step;

public interface StepHolder {

    IterativeStep[] initialSteps();

    default IterativeStep create(IterativeStep.Runner runner) {
        return new IterativeStep.Once(runner);
    }

    default IterativeStep createUntil(IterativeStep.RepeatRunner runner) {
        return new IterativeStep.Repeat(runner);
    }
}
