package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

@Value.Enclosing
public class AdapterProject {
    private final Template buildGradleTemplate;
    private final ResourceService resourceService;
    private final Charset charset;

    private AdapterProject(Template buildGradleTemplate, ResourceService resourceService, Charset charset) {
        this.resourceService = resourceService;
        this.buildGradleTemplate = buildGradleTemplate;
        this.charset = charset;
    }

    public static AdapterProject fromClassLoaderResources(ResourceService resourceService, Charset charset) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(AdapterProject.class);
        return new AdapterProject(
            templateCompiler.compile("language_project/build.gradle.kts.mustache"),
            resourceService,
            charset
        );
    }


    public Output compile(Input input) throws IOException {
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
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends AdapterProjectData.Output.Builder {
            public Builder withDefaultsBasedOnInput(Input input) {
                final ResourcePath baseDirectory = input.shared().adapterProject().baseDirectory();
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
