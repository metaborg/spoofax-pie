package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.JavaProject;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

@Value.Enclosing
public class AdapterProject {
    private final ResourceService resourceService;
    private final Template buildGradleTemplate;

    private AdapterProject(ResourceService resourceService, Template buildGradleTemplate) {
        this.resourceService = resourceService;
        this.buildGradleTemplate = buildGradleTemplate;
    }

    public static AdapterProject fromClassLoaderResources(ResourceService resourceService) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(AdapterProject.class);
        return new AdapterProject(
            resourceService,
            templateCompiler.compile("language_project/build.gradle.kts.mustache")
        );
    }


    public Output compile(Input input, Charset charset) throws IOException {
        final Output output = Output.builder().withDefaultsBasedOnInput(input).build();

        final HierarchicalResource baseDirectory = resourceService.getHierarchicalResource(output.baseDirectory());
        baseDirectory.ensureDirectoryExists();

        final HierarchicalResource buildGradleKtsFile = resourceService.getHierarchicalResource(output.buildGradleKtsFile());
        try(final ResourceWriter writer = new ResourceWriter(buildGradleKtsFile, charset)) {
            buildGradleTemplate.execute(input, writer);
            writer.flush();
        }

        return output;
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends AdapterProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();


        @Value.Default default JavaProject project() {
            final Shared shared = shared();
            final String artifactId = shared.defaultArtifactId() + ".spoofax";
            return JavaProject.builder()
                .coordinate(shared.defaultGroupId(), artifactId, shared.defaultVersion())
                .packageId(shared.basePackageId())
                .baseDirectory(shared.baseDirectory().appendSegment(artifactId))
                .build();
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends AdapterProjectData.Output.Builder {
            public Builder withDefaultsBasedOnInput(Input input) {
                final ResourcePath baseDirectory = input.project().baseDirectory();
                return this
                    .baseDirectory(baseDirectory)
                    .buildGradleKtsFile(baseDirectory.appendRelativePath("build.gradle.kts"))
                    ;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath baseDirectory();

        ResourcePath buildGradleKtsFile();
    }
}
