package mb.str.config;

import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.util.StrategoGradualSetting;

interface StrategoConfig {
    static ResourcePath defaultMainFile(ResourcePath rootDirectory) {
        return rootDirectory.appendRelativePath("src/main.str");
    }

    static ListView<String> defaultBuiltinLibs() {
        return ListView.of("stratego-lib", "stratego-gpp");
    }

    static StrategoGradualSetting defaultGradualTypingSetting() {
        return StrategoGradualSetting.NONE;
    }
}
