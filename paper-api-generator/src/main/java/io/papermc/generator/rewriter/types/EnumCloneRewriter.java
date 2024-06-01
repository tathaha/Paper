package io.papermc.generator.rewriter.types;

import java.util.Arrays;

public class EnumCloneRewriter<T extends Enum<T>, A extends Enum<A>> extends EnumRewriter<T, A> { // not really a clone anymore

    private final Class<T> basedOn;

    public EnumCloneRewriter(final Class<T> basedOn) {
        this.basedOn = basedOn;
    }

    @Override
    protected Iterable<T> getValues() {
        return Arrays.asList(this.basedOn.getEnumConstants());
    }

    @Override
    protected String rewriteEnumName(final T item) {
        return item.name();
    }
}
