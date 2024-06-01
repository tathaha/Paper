package io.papermc.generator.rewriter.yaml;

public class ImportMapping {

    public ImportSetBean imports;
    public ImportSetBean staticImports;

    public ImportSetBean getImports() {
        return this.imports;
    }

    public ImportSetBean getStaticImports() {
        return this.staticImports;
    }
}
