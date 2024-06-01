package io.papermc.generator.rewriter;

import com.google.common.base.Preconditions;
import java.nio.file.Path;
import java.util.Iterator;

public record SourceFile(ClassNamed mainClass, Path path, IndentUnit indentUnit) {

    public SourceFile {
        Preconditions.checkArgument(mainClass.isRoot(), "Main class is not a root class!");
        Preconditions.checkArgument(path.getNameCount() > 0, "Path %s cannot be a root element", path);
        Preconditions.checkArgument(path.getFileName().toString().endsWith(".java"), "Path %s is not a Java source file", path);
        Preconditions.checkArgument(!path.isAbsolute(), "Path %s cannot be absolute", path);
    }

    public static SourceFile of(ClassNamed mainClass, IndentUnit indentUnit) {
        Path path = Path.of(
            mainClass.packageName().replace('.', '/'),
            mainClass.simpleName().concat(".java")
        );
        return new SourceFile(mainClass, path, indentUnit);
    }

    public static SourceFile of(Class<?> mainClass, IndentUnit indentUnit) {
        return of(new ClassNamed(mainClass), indentUnit);
    }

    public static SourceFile of(Path path, IndentUnit indentUnit) {
        if (path.getNameCount() == 0) { // invalid path let the validation occur quickly
            return new SourceFile(ClassNamed.of("", ""), path, indentUnit);
        }

        StringBuilder packageName = new StringBuilder();
        Iterator<Path> parts = path.getParent().iterator();
        while (parts.hasNext()) {
            packageName.append(parts.next().toString());
            if (parts.hasNext()) {
                packageName.append('.');
            }
        }

        String name = path.getFileName().toString();
        int dotIndex = name.indexOf('.');
        return new SourceFile(ClassNamed.of(
            packageName.toString(),
            dotIndex == -1 ? name : name.substring(0, dotIndex)
        ), path, indentUnit);
    }
}
