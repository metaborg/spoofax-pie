package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.util.ImmutableResourceDependencies;
import mb.spoofax.compiler.util.ResourceDependencies;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

public class LanguageProjectCompiler {
    private final ResourceService resourceService;
    private final Template buildGradleTemplate;

    private LanguageProjectCompiler(ResourceService resourceService, Template buildGradleTemplate) {
        this.resourceService = resourceService;
        this.buildGradleTemplate = buildGradleTemplate;
    }

    public static LanguageProjectCompiler fromClassLoaderResources(ResourceService resourceService) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(LanguageProjectCompiler.class);
        return new LanguageProjectCompiler(
            resourceService, templateCompiler.compile("build.gradle.kts.mustache")
        );
    }

    public ResourceDependencies compile(LanguageProjectCompilerInput input, Charset charset) throws IOException {
        final HierarchicalResource baseDirectory = resourceService.getHierarchicalResource(input.project().directory());
        baseDirectory.ensureDirectoryExists();

        final HierarchicalResource buildGradleKtsFile = getBuildGradleKtsFile(input);
        try(final ResourceWriter writer = new ResourceWriter(buildGradleKtsFile, charset)) {
            final HashMap<String, Object> map = new HashMap<>();
            final String dependencyCode = input.languageSpecificationDependency().caseOf()
                .project((projectPath) -> "createProjectDependency(" + projectPath + ")")
                .module((coordinate) -> "createModuleDependency(" + coordinate.gradleNotation() + ")");
            map.put("dependencyCode", dependencyCode);
            final ArrayList<String> resourceCodes = new ArrayList<>();
            resourceCodes.add("\"target/metaborg/sdf.tbl\"");
            map.put("resourceCodes", resourceCodes);
            buildGradleTemplate.execute(input, map, writer);
            writer.flush();
        }
        return ImmutableResourceDependencies.builder().addProvidedResources(buildGradleKtsFile).build();
    }


    // TODO: remove following methods, as they are leaking the internal workings of this compiler.

    public HierarchicalResource getBuildGradleKtsFile(LanguageProjectCompilerInput input) {
        return resourceService.getHierarchicalResource(input.project().directory().appendRelativePath("build.gradle.kts"));
    }
}
