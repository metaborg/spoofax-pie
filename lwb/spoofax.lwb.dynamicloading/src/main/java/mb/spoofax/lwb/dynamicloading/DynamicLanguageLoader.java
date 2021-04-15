package mb.spoofax.lwb.dynamicloading;

import mb.cfg.CompileLanguageInput;
import mb.resource.hierarchical.ResourcePath;

import java.util.List;

public interface DynamicLanguageLoader {
    DynamicLanguage load(
        ResourcePath rootDirectory,
        CompileLanguageInput compileInput,
        List<ResourcePath> classPath
    ) throws Exception;
}
