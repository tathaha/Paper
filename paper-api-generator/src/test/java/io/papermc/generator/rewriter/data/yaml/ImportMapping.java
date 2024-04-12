package io.papermc.generator.rewriter.data.yaml;

public class ImportMapping {

    public ImportSet imports;
    public ImportSet staticImports;

    public ImportSet getImports() {
        return this.imports;
    }

    public ImportSet getStaticImports() {
        return this.staticImports;
    }
}
