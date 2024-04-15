package io.papermc.generator.rewriter.data.sample.parser.name;

import io.papermc.generator.rewriter.data.sample.parser.name.one.OneDepthClass.NonStaticClass;
import static io.papermc.generator.rewriter.data.sample.parser.name.one.OneDepthClass.StaticClass;

public class RemoteInnerClassImportType {
    {
        var a = NonStaticClass.class;
        var b = StaticClass.class;
    }
}
