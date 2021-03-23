package mb.esv.task;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.esv.EsvScope;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.ValueSupplier;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;

public class EsvConfig implements Serializable {
    public final ResourcePath mainFile;
    public final ListView<STask<?>> sourceFileOrigins;
    public final ListView<Supplier<Result<ResourcePath, ?>>> includeDirectorySuppliers;
    public final ListView<Supplier<Result<IStrategoTerm, ?>>> includeAstSuppliers;

    public EsvConfig(
        ResourcePath mainFile,
        ListView<STask<?>> sourceFileOrigins,
        ListView<Supplier<Result<ResourcePath, ?>>> includeDirectorySuppliers,
        ListView<Supplier<Result<IStrategoTerm, ?>>> includeAstSuppliers
    ) {
        this.mainFile = mainFile;
        this.sourceFileOrigins = sourceFileOrigins;
        this.includeDirectorySuppliers = includeDirectorySuppliers;
        this.includeAstSuppliers = includeAstSuppliers;
    }

    public static EsvConfig createDefault(ResourcePath rootDirectory) {
        final ResourcePath mainSourceDirectory = rootDirectory.appendRelativePath("src");
        final ResourcePath mainFile = mainSourceDirectory.appendRelativePath("main.esv");
        final ListView<Supplier<Result<ResourcePath, ?>>> includeDirectorySuppliers = ListView.of(new ValueSupplier<>(Result.ofOk(mainSourceDirectory)));
        return new EsvConfig(mainFile, ListView.of(), includeDirectorySuppliers, ListView.of());
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final EsvConfig esvConfig = (EsvConfig)o;
        if(!mainFile.equals(esvConfig.mainFile)) return false;
        if(!sourceFileOrigins.equals(esvConfig.sourceFileOrigins)) return false;
        if(!includeDirectorySuppliers.equals(esvConfig.includeDirectorySuppliers)) return false;
        return includeAstSuppliers.equals(esvConfig.includeAstSuppliers);
    }

    @Override public int hashCode() {
        int result = mainFile.hashCode();
        result = 31 * result + sourceFileOrigins.hashCode();
        result = 31 * result + includeDirectorySuppliers.hashCode();
        result = 31 * result + includeAstSuppliers.hashCode();
        return result;
    }

    @Override public String toString() {
        return "EsvConfig{" +
            "mainFile=" + mainFile +
            ", sourceFileOrigins=" + sourceFileOrigins +
            ", includeDirectorySuppliers=" + includeDirectorySuppliers +
            ", includeAstSuppliers=" + includeAstSuppliers +
            '}';
    }
}
