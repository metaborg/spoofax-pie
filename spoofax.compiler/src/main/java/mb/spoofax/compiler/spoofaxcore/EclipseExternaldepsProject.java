package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleRepository;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class EclipseExternaldepsProject {
    private final TemplateWriter settingsGradleTemplate;
    private final TemplateWriter buildGradleTemplate;

    public EclipseExternaldepsProject(TemplateCompiler templateCompiler) {
        this.settingsGradleTemplate = templateCompiler.getOrCompileToWriter("gradle_project/settings.gradle.kts.mustache");
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("eclipse_externaldeps_project/build.gradle.kts.mustache");
    }

    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        // Gradle files
        try {
            input.settingsGradleKtsFile().ifPresent((f) -> {
                try {
                    settingsGradleTemplate.write(input, f);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
        {
            final HashMap<String, Object> map = new HashMap<>();
            final ArrayList<GradleRepository> repositories = new ArrayList<>(shared.defaultRepositories());
            map.put("repositoryCodes", repositories.stream().map(GradleRepository::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
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

        @Value.Default default boolean standaloneProject() {
            return false;
        }

        @Value.Default @SuppressWarnings("immutables:untype") default Optional<ResourcePath> settingsGradleKtsFile() {
            if(standaloneProject()) {
                return Optional.of(shared().eclipseExternaldepsProject().baseDirectory().appendRelativePath("settings.gradle.kts"));
            } else {
                return Optional.empty();
            }
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
