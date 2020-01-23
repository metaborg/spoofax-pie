package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class CliProject {
    private final TemplateWriter buildGradleTemplate;
    private final TemplateWriter mainTemplate;

    public CliProject(TemplateCompiler templateCompiler) {
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("cli_project/build.gradle.kts.mustache");
        this.mainTemplate = templateCompiler.getOrCompileToWriter("cli_project/Main.java.mustache");
    }

    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        // Gradle files
        {
            final HashMap<String, Object> map = new HashMap<>();

            final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
            dependencies.add(GradleConfiguredDependency.implementation(input.adapterProjectDependency()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.spoofaxCliDep()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.logBackendSLF4JDep()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.slf4jSimpleDep()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.pieRuntimeDep()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.pieDaggerDep()));
            dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
            map.put("dependencyCodes", dependencies.stream().map(GradleConfiguredDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));

            buildGradleTemplate.write(input, map, input.buildGradleKtsFile());
        }

        // Class files
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        mainTemplate.write(input, input.genMain().file(classesGenDirectory));

        return Output.builder().fromInput(input).build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends CliProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        AdapterProject.Input adapterProject();


        /// Configuration

        GradleDependency adapterProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();


        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return shared().cliProject().baseDirectory().appendRelativePath("build.gradle.kts");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return shared().cliProject().genSourceSpoofaxJavaDirectory();
        }


        /// CLI project classes

        // Main

        @Value.Default default TypeInfo genMain() {
            return TypeInfo.of(shared().cliPackage(), "Main");
        }

        Optional<TypeInfo> manualMain();

        default TypeInfo main() {
            if(classKind().isManual() && manualMain().isPresent()) {
                return manualMain().get();
            }
            return genMain();
        }


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualMain().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualMain' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends CliProjectData.Output.Builder {
            public Builder fromInput(Input input) {
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }
    }
}
