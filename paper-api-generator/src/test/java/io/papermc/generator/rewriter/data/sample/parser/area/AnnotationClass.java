package io.papermc.generator.rewriter.data.sample.parser.area;

import org.jetbrains.annotations.ApiStatus;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({
    ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE
})
@ApiStatus.Experimental@ApiStatus.Internal
public @interface AnnotationClass { // << 34
    // @interface should be invalidated and not detected as an annotation
    // via its keyword: interface
}
