package io.papermc.generator.types;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@DefaultQualifier(NonNull.class)
public abstract class SimpleGenerator implements SourceGenerator {

    protected final String className;
    protected final String packageName;

    protected SimpleGenerator(String className, String packageName) {
        this.className = className;
        this.packageName = packageName;
    }

    protected abstract TypeSpec getTypeSpec();

    protected JavaFile.Builder file(JavaFile.Builder builder) {
        return builder;
    }

    @Override
    public void writeToFile(Path parent) throws IOException {
        JavaFile.Builder builder = JavaFile.builder(this.packageName, this.getTypeSpec());
        this.file(builder)
            .indent(INDENT_UNIT)
            .skipJavaLangImports(true);

        builder.build().writeTo(parent, StandardCharsets.UTF_8);
    }

}
