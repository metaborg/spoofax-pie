package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Value.Enclosing
public class EclipseExternaldepsProject {
    private final TemplateWriter buildGradleTemplate;

    public EclipseExternaldepsProject(TemplateCompiler templateCompiler) {
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("eclipse_externaldeps_project/build.gradle.kts.mustache");
    }

    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        // Gradle files
        {
            final HashMap<String, Object> map = new HashMap<>();

            final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
            dependencies.add(GradleConfiguredDependency.api(input.languageProjectDependency()));
            dependencies.add(GradleConfiguredDependency.api(input.adapterProjectDependency()));
            map.put("dependencyCodes", dependencies.stream().map(GradleConfiguredDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));

            buildGradleTemplate.write(input, map, input.buildGradleKtsFile());
        }

        return Output.builder().fromInput(input).build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends EclipseExternaldepsProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();


        /// Configuration

        GradleDependency languageProjectDependency();

        GradleDependency adapterProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();


        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return shared().eclipseExternaldepsProject().baseDirectory().appendRelativePath("build.gradle.kts");
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends EclipseExternaldepsProjectData.Output.Builder {
            public Builder fromInput(Input input) {
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }
    }
}
