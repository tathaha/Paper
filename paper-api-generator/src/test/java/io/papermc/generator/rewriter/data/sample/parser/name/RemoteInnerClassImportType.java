package io.papermc.generator.rewriter.data.sample.parser.name;

import io.papermc.generator.rewriter.data.sample.parser.name.one.OneDepthClass.NonStaticClass;
import static io.papermc.generator.rewriter.data.sample.parser.name.one.OneDepthClass.StaticClass;

public class RemoteInnerClassImportType {
    {
        var b = NonStaticClass.class;
        var c = StaticClass.class;
    }
}
