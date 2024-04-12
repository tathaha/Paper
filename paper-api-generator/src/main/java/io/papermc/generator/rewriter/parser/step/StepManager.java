package io.papermc.generator.rewriter.parser.step;

import java.util.ArrayDeque;
import java.util.Deque;

public class StepManager {

    private final Deque<IterativeStep> steps = new ArrayDeque<>(10);

    public Deque<IterativeStep> getSteps() {
        return this.steps;
    }

    public void enqueue(StepHolder holder) {
        for (IterativeStep step : holder.initialSteps()) {
            if (!this.steps.offerLast(step)) {
                throw new IllegalStateException("Cannot add a step into the queue!");
            }
        }
    }

    public void addPriority(IterativeStep step) {
        if (!this.steps.offerFirst(step)) {
            throw new IllegalStateException("Cannot add a priority step into the queue!");
        }
    }

    public void clearRemaining() {
        this.steps.clear();
    }
}
