package mb.esv.task;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.Supplier;
import mb.pie.api.ValueSupplier;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;

/**
 * Configuration for ESV in the context of ESV itself.
 */
public class EsvConfig implements Serializable {
    public final ResourcePath rootDirectory;
    public final ResourceKey mainFile;
    public final ListView<? extends Supplier<?>> sourceFileOrigins;
    public final ListView<Supplier<Result<ResourcePath, ?>>> includeDirectorySuppliers;
    public final ListView<Supplier<Result<IStrategoTerm, ?>>> includeAstSuppliers;

    public EsvConfig(
        ResourcePath rootDirectory,
        ResourcePath mainFile,
        ListView<? extends Supplier<?>> sourceFileOrigins,
        ListView<Supplier<Result<ResourcePath, ?>>> includeDirectorySuppliers,
        ListView<Supplier<Result<IStrategoTerm, ?>>> includeAstSuppliers
    ) {
        this.rootDirectory = rootDirectory;
        this.mainFile = mainFile;
        this.sourceFileOrigins = sourceFileOrigins;
        this.includeDirectorySuppliers = includeDirectorySuppliers;
        this.includeAstSuppliers = includeAstSuppliers;
    }

    public static EsvConfig createDefault(ResourcePath rootDirectory) {
        final ResourcePath mainSourceDirectory = rootDirectory.appendRelativePath("src");
        final ResourcePath mainFile = mainSourceDirectory.appendRelativePath("main.esv");
        final ListView<Supplier<Result<ResourcePath, ?>>> includeDirectorySuppliers = ListView.of(new ValueSupplier<>(Result.ofOk(mainSourceDirectory)));
        return new EsvConfig(rootDirectory, mainFile, ListView.of(), includeDirectorySuppliers, ListView.of());
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final EsvConfig esvConfig = (EsvConfig)o;
        if(!rootDirectory.equals(esvConfig.rootDirectory)) return false;
        if(!mainFile.equals(esvConfig.mainFile)) return false;
        if(!sourceFileOrigins.equals(esvConfig.sourceFileOrigins)) return false;
        if(!includeDirectorySuppliers.equals(esvConfig.includeDirectorySuppliers)) return false;
        return includeAstSuppliers.equals(esvConfig.includeAstSuppliers);
    }

    @Override public int hashCode() {
        int result = rootDirectory.hashCode();
        result = 31 * result + mainFile.hashCode();
        result = 31 * result + sourceFileOrigins.hashCode();
        result = 31 * result + includeDirectorySuppliers.hashCode();
        result = 31 * result + includeAstSuppliers.hashCode();
        return result;
    }

    @Override public String toString() {
        return "EsvConfig{" +
            "rootDirectory=" + rootDirectory +
            ", mainFile=" + mainFile +
            ", sourceFileOrigins=" + sourceFileOrigins +
            ", includeDirectorySuppliers=" + includeDirectorySuppliers +
            ", includeAstSuppliers=" + includeAstSuppliers +
            '}';
    }
}
