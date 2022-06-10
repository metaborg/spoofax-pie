package mb.str.config;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.pie.api.OutTransient;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.ModuleIdentifier;
import mb.stratego.build.strincr.Stratego2LibInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.parsetable.IParseTable;

import java.io.Serializable;

public class StrategoAnalyzeConfig implements Serializable {
    public final ResourcePath rootDirectory;
    public final ModuleIdentifier mainModule;
    public final ListView<ResourcePath> includeDirs;
    public final ListView<BuiltinLibraryIdentifier> builtinLibs;
    public final ListView<Supplier<Stratego2LibInfo>> str2libraries;
    public final MapView<String, Supplier<? extends Result<? extends IParseTable, ?>>> concreteSyntaxExtensionParseTables;
    public final MapView<String, Supplier<OutTransient<Result<IParseTable, ?>>>> concreteSyntaxExtensionTransientParseTables;
    public final ListView<STask<?>> sourceFileOrigins;

    public StrategoAnalyzeConfig(
        ResourcePath rootDirectory,
        ModuleIdentifier mainModule,
        ListView<ResourcePath> includeDirs,
        ListView<BuiltinLibraryIdentifier> builtinLibs,
        ListView<Supplier<Stratego2LibInfo>> str2libraries,
        MapView<String, Supplier<? extends Result<? extends IParseTable, ?>>> concreteSyntaxExtensionParseTables,
        MapView<String, Supplier<OutTransient<Result<IParseTable, ?>>>> concreteSyntaxExtensionTransientParseTables,
        ListView<STask<?>> sourceFileOrigins
    ) {
        this.rootDirectory = rootDirectory;
        this.mainModule = mainModule;
        this.includeDirs = includeDirs;
        this.builtinLibs = builtinLibs;
        this.str2libraries = str2libraries;
        this.concreteSyntaxExtensionParseTables = concreteSyntaxExtensionParseTables;
        this.concreteSyntaxExtensionTransientParseTables = concreteSyntaxExtensionTransientParseTables;
        this.sourceFileOrigins = sourceFileOrigins;
    }

    public StrategoAnalyzeConfig(
        ResourcePath rootDirectory,
        ResourcePath mainFile,
        ListView<ResourcePath> includeDirs,
        ListView<BuiltinLibraryIdentifier> builtinLibs,
        ListView<Supplier<Stratego2LibInfo>> str2libraries,
        MapView<String, Supplier<? extends Result<? extends IParseTable, ?>>> concreteSyntaxExtensionParseTables,
        MapView<String, Supplier<OutTransient<Result<IParseTable, ?>>>> concreteSyntaxExtensionTransientParseTables,
        ListView<STask<?>> sourceFileOrigins
    ) {
        this(
            rootDirectory,
            StrategoConfig.fromRootDirectoryAndMainFile(rootDirectory, mainFile),
            includeDirs,
            builtinLibs,
            str2libraries,
            concreteSyntaxExtensionParseTables,
            concreteSyntaxExtensionTransientParseTables,
            sourceFileOrigins
        );
    }

    public static StrategoAnalyzeConfig createDefault(ResourcePath rootDirectory) {
        return new StrategoAnalyzeConfig(
            rootDirectory,
            StrategoConfig.defaultMainModule(rootDirectory),
            ListView.of(),
            StrategoConfig.defaultBuiltinLibs(),
            ListView.of(),
            MapView.of(),
            MapView.of(),
            ListView.of()
        );
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StrategoAnalyzeConfig that = (StrategoAnalyzeConfig)o;
        if(!rootDirectory.equals(that.rootDirectory)) return false;
        if(!mainModule.equals(that.mainModule)) return false;
        if(!includeDirs.equals(that.includeDirs)) return false;
        if(!builtinLibs.equals(that.builtinLibs)) return false;
        if(!str2libraries.equals(that.str2libraries)) return false;
        if(!concreteSyntaxExtensionParseTables.equals(that.concreteSyntaxExtensionParseTables)) return false;
        if(!concreteSyntaxExtensionTransientParseTables.equals(that.concreteSyntaxExtensionTransientParseTables))
            return false;
        return sourceFileOrigins.equals(that.sourceFileOrigins);
    }

    @Override public int hashCode() {
        int result = rootDirectory.hashCode();
        result = 31 * result + mainModule.hashCode();
        result = 31 * result + includeDirs.hashCode();
        result = 31 * result + builtinLibs.hashCode();
        result = 31 * result + str2libraries.hashCode();
        result = 31 * result + concreteSyntaxExtensionParseTables.hashCode();
        result = 31 * result + concreteSyntaxExtensionTransientParseTables.hashCode();
        result = 31 * result + sourceFileOrigins.hashCode();
        return result;
    }

    @Override public String toString() {
        return "StrategoAnalyzeConfig{" +
            "rootDirectory=" + rootDirectory +
            ", mainModule=" + mainModule +
            ", includeDirs=" + includeDirs +
            ", builtinLibs=" + builtinLibs +
            ", str2libraries=" + str2libraries +
            ", concreteSyntaxExtensionParseTables=" + concreteSyntaxExtensionParseTables +
            ", concreteSyntaxExtensionTransientParseTables=" + concreteSyntaxExtensionTransientParseTables +
            ", sourceFileOrigins=" + sourceFileOrigins +
            '}';
    }
}
