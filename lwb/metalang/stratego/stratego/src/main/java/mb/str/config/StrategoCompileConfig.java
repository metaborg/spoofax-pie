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
import org.metaborg.util.cmd.Arguments;

import java.io.File;
import java.io.Serializable;

public class StrategoCompileConfig implements Serializable {
    public final ResourcePath rootDirectory;
    public final ModuleIdentifier mainModule;
    public final ListView<ResourcePath> includeDirs;
    public final ListView<BuiltinLibraryIdentifier> builtinLibs;
    public final ListView<Supplier<Stratego2LibInfo>> str2libraries;
    public final Arguments extraCompilerArguments;
    public final MapView<String, Supplier<? extends Result<? extends IParseTable, ?>>> concreteSyntaxExtensionParseTables;
    public final MapView<String, Supplier<OutTransient<Result<IParseTable, ?>>>> concreteSyntaxExtensionTransientParseTables;
    public final ListView<STask<?>> sourceFileOrigins;
    public final @Nullable ResourcePath cacheDir;
    public final ResourcePath javaSourceFileOutputDir;
    public final ResourcePath javaClassFileOutputDir;
    public final String outputJavaPackageId;
    public final String outputLibraryName;
    public final ListView<File> javaClassPaths;

    public StrategoCompileConfig(
        ResourcePath rootDirectory,
        ModuleIdentifier mainModule,
        ListView<ResourcePath> includeDirs,
        ListView<BuiltinLibraryIdentifier> builtinLibs,
        ListView<Supplier<Stratego2LibInfo>> str2libraries,
        Arguments extraCompilerArguments,
        MapView<String, Supplier<? extends Result<? extends IParseTable, ?>>> concreteSyntaxExtensionParseTables,
        MapView<String, Supplier<OutTransient<Result<IParseTable, ?>>>> concreteSyntaxExtensionTransientParseTables,
        ListView<STask<?>> sourceFileOrigins,
        @Nullable ResourcePath cacheDir,
        ResourcePath javaSourceFileOutputDir,
        ResourcePath javaClassFileOutputDir,
        String outputJavaPackageId,
        String outputLibraryName,
        ListView<File> javaClassPaths
    ) {
        this.rootDirectory = rootDirectory;
        this.mainModule = mainModule;
        this.includeDirs = includeDirs;
        this.builtinLibs = builtinLibs;
        this.str2libraries = str2libraries;
        this.extraCompilerArguments = extraCompilerArguments;
        this.concreteSyntaxExtensionParseTables = concreteSyntaxExtensionParseTables;
        this.concreteSyntaxExtensionTransientParseTables = concreteSyntaxExtensionTransientParseTables;
        this.sourceFileOrigins = sourceFileOrigins;
        this.cacheDir = cacheDir;
        this.javaSourceFileOutputDir = javaSourceFileOutputDir;
        this.javaClassFileOutputDir = javaClassFileOutputDir;
        this.outputJavaPackageId = outputJavaPackageId;
        this.outputLibraryName = outputLibraryName;
        this.javaClassPaths = javaClassPaths;
    }

    public StrategoCompileConfig(
        ResourcePath rootDirectory,
        ResourcePath mainFile,
        ListView<ResourcePath> includeDirs,
        ListView<BuiltinLibraryIdentifier> builtinLibs,
        ListView<Supplier<Stratego2LibInfo>> str2libraries,
        Arguments extraCompilerArguments,
        MapView<String, Supplier<? extends Result<? extends IParseTable, ?>>> concreteSyntaxExtensionParseTables,
        MapView<String, Supplier<OutTransient<Result<IParseTable, ?>>>> concreteSyntaxExtensionTransientParseTables,
        ListView<STask<?>> sourceFileOrigins,
        @Nullable ResourcePath cacheDir,
        ResourcePath javaSourceFileOutputDir,
        ResourcePath javaClassFileOutputDir,
        String outputJavaPackageId,
        String outputLibraryName,
        ListView<File> javaClassPaths
    ) {
        this(
            rootDirectory,
            StrategoConfig.fromRootDirectoryAndMainFile(rootDirectory, mainFile),
            includeDirs,
            builtinLibs,
            str2libraries,
            extraCompilerArguments,
            concreteSyntaxExtensionParseTables,
            concreteSyntaxExtensionTransientParseTables,
            sourceFileOrigins,
            cacheDir,
            javaSourceFileOutputDir,
            javaClassFileOutputDir,
            outputJavaPackageId,
            outputLibraryName,
            javaClassPaths
        );
    }

    public static StrategoCompileConfig createDefault(
        ResourcePath rootDirectory,
        ResourcePath javaSourceFileOutputDir,
        ResourcePath javaClassFileOutputDir,
        String outputJavaPackageId,
        String outputLibraryName
    ) {
        return new StrategoCompileConfig(
            rootDirectory,
            StrategoConfig.defaultMainModule(rootDirectory),
            ListView.of(),
            StrategoConfig.defaultBuiltinLibs(),
            ListView.of(),
            new Arguments(),
            MapView.of(),
            MapView.of(),
            ListView.of(),
            null,
            javaSourceFileOutputDir,
            javaClassFileOutputDir,
            outputJavaPackageId,
            outputLibraryName,
            ListView.of()
        );
    }

    public StrategoAnalyzeConfig toAnalyzeConfig() {
        return new StrategoAnalyzeConfig(
            rootDirectory,
            mainModule,
            includeDirs,
            builtinLibs,
            str2libraries,
            concreteSyntaxExtensionParseTables,
            concreteSyntaxExtensionTransientParseTables,
            sourceFileOrigins
        );
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StrategoCompileConfig that = (StrategoCompileConfig)o;
        if(!rootDirectory.equals(that.rootDirectory)) return false;
        if(!mainModule.equals(that.mainModule)) return false;
        if(!includeDirs.equals(that.includeDirs)) return false;
        if(!builtinLibs.equals(that.builtinLibs)) return false;
        if(!str2libraries.equals(that.str2libraries)) return false;
        if(!extraCompilerArguments.equals(that.extraCompilerArguments)) return false;
        if(!concreteSyntaxExtensionParseTables.equals(that.concreteSyntaxExtensionParseTables)) return false;
        if(!concreteSyntaxExtensionTransientParseTables.equals(that.concreteSyntaxExtensionTransientParseTables))
            return false;
        if(!sourceFileOrigins.equals(that.sourceFileOrigins)) return false;
        if(cacheDir != null ? !cacheDir.equals(that.cacheDir) : that.cacheDir != null) return false;
        if(!javaSourceFileOutputDir.equals(that.javaSourceFileOutputDir)) return false;
        if(!javaClassFileOutputDir.equals(that.javaClassFileOutputDir)) return false;
        if(!outputJavaPackageId.equals(that.outputJavaPackageId)) return false;
        if(!outputLibraryName.equals(that.outputLibraryName)) return false;
        return javaClassPaths.equals(that.javaClassPaths);
    }

    @Override public int hashCode() {
        int result = rootDirectory.hashCode();
        result = 31 * result + mainModule.hashCode();
        result = 31 * result + includeDirs.hashCode();
        result = 31 * result + builtinLibs.hashCode();
        result = 31 * result + str2libraries.hashCode();
        result = 31 * result + extraCompilerArguments.hashCode();
        result = 31 * result + concreteSyntaxExtensionParseTables.hashCode();
        result = 31 * result + concreteSyntaxExtensionTransientParseTables.hashCode();
        result = 31 * result + sourceFileOrigins.hashCode();
        result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
        result = 31 * result + javaSourceFileOutputDir.hashCode();
        result = 31 * result + javaClassFileOutputDir.hashCode();
        result = 31 * result + outputJavaPackageId.hashCode();
        result = 31 * result + outputLibraryName.hashCode();
        result = 31 * result + javaClassPaths.hashCode();
        return result;
    }

    @Override public String toString() {
        return "StrategoCompileConfig{" +
            "rootDirectory=" + rootDirectory +
            ", mainModule=" + mainModule +
            ", includeDirs=" + includeDirs +
            ", builtinLibs=" + builtinLibs +
            ", str2libraries=" + str2libraries +
            ", extraCompilerArguments=" + extraCompilerArguments +
            ", concreteSyntaxExtensionParseTables=" + concreteSyntaxExtensionParseTables +
            ", concreteSyntaxExtensionTransientParseTables=" + concreteSyntaxExtensionTransientParseTables +
            ", sourceFileOrigins=" + sourceFileOrigins +
            ", cacheDir=" + cacheDir +
            ", javaSourceFileOutputDir=" + javaSourceFileOutputDir +
            ", javaClassFileOutputDir=" + javaClassFileOutputDir +
            ", outputJavaPackageId='" + outputJavaPackageId + '\'' +
            ", outputLibraryName='" + outputLibraryName + '\'' +
            ", javaClassPaths=" + javaClassPaths +
            '}';
    }
}
