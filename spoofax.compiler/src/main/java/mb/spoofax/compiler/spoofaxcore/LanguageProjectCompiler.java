package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.util.ImmutableResourceDeps;
import mb.spoofax.compiler.util.ResourceDeps;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

public class LanguageProjectCompiler {
    private final Template buildGradleTemplate;

    private LanguageProjectCompiler(Template buildGradleTemplate) {
        this.buildGradleTemplate = buildGradleTemplate;
    }

    public static LanguageProjectCompiler fromClassLoaderResources() {
        final TemplateCompiler templateCompiler = new TemplateCompiler(LanguageProjectCompiler.class);
        return new LanguageProjectCompiler(
            templateCompiler.compile("build.gradle.kts.mustache")
        );
    }

    public ResourceDeps compile(LanguageProjectInput input, HierarchicalResource baseDir, Charset charset) throws IOException {
        baseDir.createDirectory(true);
        final HierarchicalResource buildGradleKts = getBuildGradleKtsFile(baseDir);
        try(final ResourceWriter writer = new ResourceWriter(buildGradleKts, charset)) {
            final HashMap<String, Object> map = new HashMap<>();
            final String dependencyCode = input.spoofaxCoreDependency().caseOf()
                .project((projectPath) -> "createProjectDependency(" + projectPath + ")")
                .module((notation) -> "createModuleDependency(" + notation + ")");
            map.put("dependencyCode", dependencyCode);
            final ArrayList<String> resourceCodes = new ArrayList<>();
            resourceCodes.add("\"target/metaborg/sdf.tbl\"");
            map.put("resourceCodes", resourceCodes);
            buildGradleTemplate.execute(input, map, writer);
            writer.flush();
        }
        return ImmutableResourceDeps.builder().addProvidedResources(buildGradleKts).build();
    }


    public HierarchicalResource getBuildGradleKtsFile(HierarchicalResource baseDir) {
        return baseDir.appendRelativePath("build.gradle.kts");
    }
}
