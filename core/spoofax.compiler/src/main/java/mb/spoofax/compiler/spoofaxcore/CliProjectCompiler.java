package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.Coordinate;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class CliProjectCompiler {
    private final TemplateWriter buildGradleTemplate;
    private final TemplateWriter packageInfoTemplate;
    private final TemplateWriter mainTemplate;

    public CliProjectCompiler(TemplateCompiler templateCompiler) {
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("cli_project/build.gradle.kts.mustache");
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("cli_project/package-info.java.mustache");
        this.mainTemplate = templateCompiler.getOrCompileToWriter("cli_project/Main.java.mustache");
    }

    public void generateInitial(Input input) throws IOException {
        buildGradleTemplate.write(input.buildGradleKtsFile(), input);
    }

    public ArrayList<GradleConfiguredDependency> getDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.implementationPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessorPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.implementation(input.adapterProjectDependency()));
        dependencies.add(GradleConfiguredDependency.implementation(shared.spoofaxCliDep()));
        dependencies.add(GradleConfiguredDependency.implementation(shared.logBackendSLF4JDep()));
        dependencies.add(GradleConfiguredDependency.implementation(shared.slf4jSimpleDep()));
        dependencies.add(GradleConfiguredDependency.implementation(shared.pieRuntimeDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        return dependencies;
    }

    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        packageInfoTemplate.write(input.genPackageInfo().file(classesGenDirectory), input);
        mainTemplate.write(input.genMain().file(classesGenDirectory), input);

        return Output.builder().addAllProvidedFiles(input.providedFiles()).build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends CliProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Project

        @Value.Default default String defaultProjectSuffix() {
            return ".cli";
        }

        @Value.Default default GradleProject project() {
            final String artifactId = shared().defaultArtifactId() + defaultProjectSuffix();
            return GradleProject.builder()
                .coordinate(Coordinate.of(shared().defaultGroupId(), artifactId, shared().defaultVersion()))
                .baseDirectory(shared().baseDirectory().appendSegment(artifactId))
                .build();
        }

        @Value.Default default String packageId() {
            return shared().defaultBasePackageId() + defaultProjectSuffix();
        }


        /// Configuration

        @Value.Default default GradleDependency adapterProjectDependency() {
            return adapterProjectCompilerInput().adapterProject().project().asProjectDependency();
        }

        List<GradleConfiguredDependency> additionalDependencies();


        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return project().baseDirectory().appendRelativePath("build.gradle.kts");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return project().genSourceSpoofaxJavaDirectory();
        }


        /// Classes

        // package-info

        @Value.Default default TypeInfo genPackageInfo() {
            return TypeInfo.of(packageId(), "package-info");
        }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return genPackageInfo();
        }

        // Main

        @Value.Default default TypeInfo genMain() {
            return TypeInfo.of(packageId(), "Main");
        }

        Optional<TypeInfo> manualMain();

        default TypeInfo main() {
            if(classKind().isManual() && manualMain().isPresent()) {
                return manualMain().get();
            }
            return genMain();
        }


        /// Provided files

        default ArrayList<ResourcePath> providedFiles() {
            final ArrayList<ResourcePath> generatedFiles = new ArrayList<>();
            if(classKind().isGenerating()) {
                generatedFiles.add(genPackageInfo().file(classesGenDirectory()));
                generatedFiles.add(genMain().file(classesGenDirectory()));
            }
            return generatedFiles;
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProjectCompiler.Input adapterProjectCompilerInput();


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManualOnly();
            if(!manual) return;
            if(!manualMain().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualMain' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends CliProjectCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }

        List<ResourcePath> providedFiles();
    }
}
