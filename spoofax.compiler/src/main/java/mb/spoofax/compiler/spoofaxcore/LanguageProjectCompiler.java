package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

@Value.Enclosing @ImmutablesStyle
public class LanguageProjectCompiler {
    private final ResourceService resourceService;
    private final Template settingsGradleTemplate;
    private final Template buildGradleTemplate;

    private LanguageProjectCompiler(ResourceService resourceService, Template settingsGradleTemplate, Template buildGradleTemplate) {
        this.resourceService = resourceService;
        this.settingsGradleTemplate = settingsGradleTemplate;
        this.buildGradleTemplate = buildGradleTemplate;
    }

    public static LanguageProjectCompiler fromClassLoaderResources(ResourceService resourceService) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(LanguageProjectCompiler.class);
        return new LanguageProjectCompiler(
            resourceService,
            templateCompiler.compile("settings.gradle.kts.mustache"),
            templateCompiler.compile("build.gradle.kts.mustache")
        );
    }


    public Output compile(Input input, Charset charset) throws IOException {
        final Output output = Output.builder().withDefaultsBasedOnInput(input).build();

        final HierarchicalResource baseDirectory = resourceService.getHierarchicalResource(output.baseDirectory());
        baseDirectory.ensureDirectoryExists();

        final HierarchicalResource settingsGradleKtsFile = resourceService.getHierarchicalResource(output.settingsGradleKtsFile());
        try(final ResourceWriter writer = new ResourceWriter(settingsGradleKtsFile, charset)) {
            settingsGradleTemplate.execute(input, writer);
            writer.flush();
        }

        final HierarchicalResource buildGradleKtsFile = resourceService.getHierarchicalResource(output.buildGradleKtsFile());
        try(final ResourceWriter writer = new ResourceWriter(buildGradleKtsFile, charset)) {
            final HashMap<String, Object> map = new HashMap<>();
            final String dependencyCode = input.languageSpecificationDependency().caseOf()
                .project((projectPath) -> "createProjectDependency(\"" + projectPath + "\")")
                .module((coordinate) -> "createModuleDependency(\"" + coordinate.gradleNotation() + "\")");
            map.put("dependencyCode", dependencyCode);
            final ArrayList<String> resourceCodes = new ArrayList<>();
            resourceCodes.add("\"target/metaborg/sdf.tbl\"");
            map.put("resourceCodes", resourceCodes);
            buildGradleTemplate.execute(input, map, writer);
            writer.flush();
        }

        return output;
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends LanguageProjectCompilerData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        @Value.Default default JavaProject project() {
            final Shared shared = shared();
            final String artifactId = shared.defaultArtifactId();
            return JavaProject.builder()
                .coordinate(shared.defaultGroupId(), artifactId, shared.defaultVersion())
                .packageId(shared.basePackageId())
                .directory(shared.baseDirectory().appendSegment(artifactId))
                .build();
        }

        JavaDependency languageSpecificationDependency();

        @Value.Default default boolean includeStrategoClasses() { return false; }

        @Value.Default default boolean includeStrategoJavaStrategyClasses() { return false; }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends LanguageProjectCompilerData.Output.Builder {
            public Builder withDefaultsBasedOnInput(Input input) {
                final ResourcePath directory = input.project().directory();
                return this
                    .baseDirectory(directory)
                    .settingsGradleKtsFile(directory.appendRelativePath("settings.gradle.kts"))
                    .buildGradleKtsFile(directory.appendRelativePath("build.gradle.kts"))
                    ;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath baseDirectory();

        ResourcePath settingsGradleKtsFile();

        ResourcePath buildGradleKtsFile();
    }
}
