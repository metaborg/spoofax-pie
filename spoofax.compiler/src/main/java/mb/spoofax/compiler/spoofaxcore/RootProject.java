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
import java.util.List;

@Value.Enclosing
public class RootProject {
    private final ResourceService resourceService;
    private final Template settingsGradleTemplate;
    private final Template buildGradleTemplate;

    private RootProject(ResourceService resourceService, Template settingsGradleTemplate, Template buildGradleTemplate) {
        this.resourceService = resourceService;
        this.settingsGradleTemplate = settingsGradleTemplate;
        this.buildGradleTemplate = buildGradleTemplate;
    }

    public static RootProject fromClassLoaderResources(ResourceService resourceService) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(RootProject.class);
        return new RootProject(
            resourceService,
            templateCompiler.compile("root_project/settings.gradle.kts.mustache"),
            templateCompiler.compile("root_project/build.gradle.kts.mustache")
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
            buildGradleTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends RootProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();


        @Value.Default default String name() {
            return shared().defaultArtifactId();
        }

        @Value.Default default ResourcePath baseDirectory() {
            return shared().baseDirectory();
        }

        List<String> includedProjects();
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends RootProjectData.Output.Builder {
            public Builder withDefaultsBasedOnInput(Input input) {
                final ResourcePath baseDirectory = input.baseDirectory();
                return this
                    .baseDirectory(baseDirectory)
                    .settingsGradleKtsFile(baseDirectory.appendRelativePath("settings.gradle.kts"))
                    .buildGradleKtsFile(baseDirectory.appendRelativePath("build.gradle.kts"))
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
