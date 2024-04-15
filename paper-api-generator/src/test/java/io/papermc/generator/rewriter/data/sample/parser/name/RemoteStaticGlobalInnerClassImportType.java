package io.papermc.generator.rewriter.data.sample.parser.name;

import static io.papermc.generator.rewriter.data.sample.parser.name.one.OneDepthClass.*;

public class RemoteStaticGlobalInnerClassImportType {
    {
        var a = io.papermc.generator.rewriter.data.sample.parser.name.one.OneDepthClass.NonStaticClass.class;
        var b = StaticClass.class;
    }
}
