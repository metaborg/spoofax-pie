package mb.spoofax.compiler.platform;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.util.GradleConfiguredBundleDependency;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleProject;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class EclipseExternaldepsProjectCompiler implements TaskDef<EclipseExternaldepsProjectCompiler.Input, EclipseExternaldepsProjectCompiler.Output> {
    @Inject public EclipseExternaldepsProjectCompiler() {}


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        return Output.builder().build();
    }


    public ArrayList<GradleConfiguredDependency> getDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.apiPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessorPlatform(shared.spoofaxDependencyConstraintsDep()));
        return dependencies;
    }

    public ArrayList<GradleConfiguredBundleDependency> getBundleDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredBundleDependency> bundleDependencies = new ArrayList<>();
        bundleDependencies.add(GradleConfiguredBundleDependency.bundleTargetPlatformApi("javax.inject", null));
        bundleDependencies.add(GradleConfiguredBundleDependency.bundleApi(shared.spoofaxEclipseExternaldepsDep()));
        bundleDependencies.add(GradleConfiguredBundleDependency.bundleEmbedApi(input.languageProjectDependency()));
        bundleDependencies.add(GradleConfiguredBundleDependency.bundleEmbedApi(input.adapterProjectDependency()));
        if(input.adapterProjectCompilerInput().multilangAnalyzer().isPresent()) {
            bundleDependencies.add(GradleConfiguredBundleDependency.bundleApi(shared.multilangEclipseExternaldepsDep()));
        }
        return bundleDependencies;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends EclipseExternaldepsProjectCompilerData.Input.Builder {
            public Builder withDefaultProjectFromParentDirectory(ResourcePath parentDirectory, Shared shared) {
                return withDefaultProject(parentDirectory.appendRelativePath(defaultArtifactId(shared)), shared);
            }

            public Builder withDefaultProject(ResourcePath baseDirectory, Shared shared) {
                final GradleProject gradleProject = GradleProject.builder()
                    .coordinate(shared.defaultGroupId(), defaultArtifactId(shared), Optional.of(shared.defaultVersion()))
                    .baseDirectory(baseDirectory)
                    .build();
                return this
                    .project(gradleProject)
                    .packageId(defaultPackageId(shared))
                    ;
            }

            public static String defaultProjectSuffix() {
                return ".eclipse.externaldeps";
            }

            public static String defaultArtifactId(Shared shared) {
                return shared.defaultArtifactId() + defaultProjectSuffix();
            }

            public static String defaultPackageId(Shared shared) {
                return shared.defaultPackageId() + defaultProjectSuffix();
            }
        }

        static Builder builder() { return new Builder(); }


        /// Project

        GradleProject project();

        String packageId();


        /// Configuration

        GradleDependency languageProjectDependency();

        GradleDependency adapterProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();


        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return project().baseDirectory().appendRelativePath("build.gradle.kts");
        }


        /// Provided files

        default ArrayList<ResourcePath> providedFiles() {
            return new ArrayList<>();
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProjectCompiler.Input adapterProjectCompilerInput();
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends EclipseExternaldepsProjectCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
