package mb.str.config;

import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.ModuleIdentifier;

interface StrategoConfig {
    static ResourcePath defaultMainFile(ResourcePath rootDirectory) {
        return rootDirectory.appendRelativePath("src/main.str");
    }

    static ModuleIdentifier defaultMainModule(ResourcePath rootDirectory) {
        return new ModuleIdentifier(true, false, "main", defaultMainFile(rootDirectory));
    }

    static ModuleIdentifier fromRootDirectoryAndMainFile(ResourcePath rootDirectory, ResourcePath mainFile) {
        return new ModuleIdentifier(true, false, rootDirectory.relativize(mainFile.removeLeafExtension()), mainFile);
    }

    static ListView<BuiltinLibraryIdentifier> defaultBuiltinLibs() {
        return ListView.of(BuiltinLibraryIdentifier.StrategoLib, BuiltinLibraryIdentifier.StrategoGpp, BuiltinLibraryIdentifier.StrategoSglr);
    }
}
