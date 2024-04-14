package io.papermc.generator.rewriter.data.sample.parser.area;

public @interface AnnotationClass { // << 34
    // @interface should be invalidated and not detected as an annotation
    // via its keyword: interface
}
