package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Value.Enclosing
public class EclipseExternaldepsProjectCompiler {
    private final TemplateWriter buildGradleTemplate;

    public EclipseExternaldepsProjectCompiler(TemplateCompiler templateCompiler) {
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("eclipse_externaldeps_project/build.gradle.kts.mustache");
    }

    public void generateInitial(Input input) throws IOException {
        buildGradleTemplate.write(input.buildGradleKtsFile(), input);
    }

    public ArrayList<GradleConfiguredDependency> getDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.api(input.languageProjectDependency()));
        dependencies.add(GradleConfiguredDependency.api(input.adapterProjectDependency()));
        return dependencies;
    }

    public Output compile(Input input) throws IOException {
        return Output.builder().addAllProvidedFiles(input.providedFiles()).build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends EclipseExternaldepsProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Project

        @Value.Default default String defaultProjectSuffix() {
            return ".eclipse.externaldeps";
        }

        @Value.Default default GradleProject project() {
            final String artifactId = shared().defaultArtifactId() + defaultProjectSuffix();
            return GradleProject.builder()
                .coordinate(shared().defaultGroupId(), artifactId, shared().defaultVersion())
                .baseDirectory(shared().baseDirectory().appendSegment(artifactId))
                .build();
        }


        /// Configuration

        GradleDependency languageProjectDependency();

        GradleDependency adapterProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();


        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return project().baseDirectory().appendRelativePath("build.gradle.kts");
        }

        @Value.Default default ResourcePath generatedGradleKtsFile() {
            return project().genSourceSpoofaxGradleDirectory().appendRelativePath("generated.gradle.kts");
        }

        default String relativeGeneratedGradleKtsFile() {
            final ResourcePath parentDirectory = Objects.requireNonNull(buildGradleKtsFile().getParent());
            return parentDirectory.relativizeToString(generatedGradleKtsFile());
        }


        /// Provided files

        default ArrayList<ResourcePath> providedFiles() {
            return new ArrayList<>();
        }


        /// Automatically provided sub-inputs

        Shared shared();
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends EclipseExternaldepsProjectCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }

        List<ResourcePath> providedFiles();
    }
}
