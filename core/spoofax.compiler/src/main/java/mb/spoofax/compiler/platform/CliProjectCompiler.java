package mb.spoofax.compiler.platform;

import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Value.Enclosing
public class CliProjectCompiler implements TaskDef<CliProjectCompiler.Input, CliProjectCompiler.Output> {
    private final TemplateWriter packageInfoTemplate;
    private final TemplateWriter mainTemplate;

    @Inject public CliProjectCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("cli_project/package-info.java.mustache");
        this.mainTemplate = templateCompiler.getOrCompileToWriter("cli_project/Main.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        final Shared shared = input.shared();

        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManual()) return outputBuilder.build(); // Nothing to generate: return.

        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        packageInfoTemplate.write(context, input.basePackageInfo().file(generatedJavaSourcesDirectory), input);
        mainTemplate.write(context, input.baseMain().file(generatedJavaSourcesDirectory), input);

        return outputBuilder.build();
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    @Override public Serializable key(Input input) {
        return input.project().baseDirectory();
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
        if(input.adapterProjectCompilerInput().strategoRuntime().isPresent()) {
            dependencies.add(GradleConfiguredDependency.implementation(shared.strategolibDep()));
        }
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        return dependencies;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends CliProjectCompilerData.Input.Builder {
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
                return ".cli";
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

        @Value.Default default GradleDependency adapterProjectDependency() {
            return adapterProjectCompilerInput().adapterProject().project().asProjectDependency();
        }

        List<GradleConfiguredDependency> additionalDependencies();


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }

        default ResourcePath generatedJavaSourcesDirectory() {
            return project().buildGeneratedSourcesDirectory().appendRelativePath("cli");
        }


        /// Classes

        // package-info

        @Value.Default default TypeInfo basePackageInfo() {
            return TypeInfo.of(packageId(), "package-info");
        }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return basePackageInfo();
        }

        // Main

        @Value.Default default TypeInfo baseMain() {
            return TypeInfo.of(packageId(), "Main");
        }

        Optional<TypeInfo> extendMain();

        default TypeInfo main() {
            return extendMain().orElseGet(this::baseMain);
        }


        /// Provided files

        default ArrayList<ResourcePath> providedFiles() {
            final ArrayList<ResourcePath> generatedFiles = new ArrayList<>();
            if(classKind().isGenerating()) {
                generatedFiles.add(basePackageInfo().file(generatedJavaSourcesDirectory()));
                generatedFiles.add(baseMain().file(generatedJavaSourcesDirectory()));
            }
            return generatedFiles;
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProjectCompiler.Input adapterProjectCompilerInput();


        @Value.Check default void check() {

        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends CliProjectCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
