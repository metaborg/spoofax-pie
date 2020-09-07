package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.Coordinate;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.GradleRepository;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Value.Enclosing
public class RootProjectCompiler implements TaskDef<RootProjectCompiler.Input, RootProjectCompiler.Output> {
    private final TemplateWriter settingsGradleTemplate;
    private final TemplateWriter buildGradleTemplate;

    @Inject public RootProjectCompiler(TemplateCompiler templateCompiler) {
        this.settingsGradleTemplate = templateCompiler.getOrCompileToWriter("root_project/settings.gradle.kts.mustache");
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("root_project/build.gradle.kts.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        final Shared shared = input.shared();

        {
            final HashMap<String, Object> map = new HashMap<>();
            final ArrayList<GradleRepository> pluginRepositories = new ArrayList<>(shared.defaultPluginRepositories());
            map.put("pluginRepositoryCodes", pluginRepositories.stream().map(GradleRepository::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
            final ArrayList<String> includedProjects = new ArrayList<>(input.includedProjects());
            map.put("includedProjects", includedProjects);
            settingsGradleTemplate.write(context, input.settingsGradleKtsFile(), input, map);
        }
        {
            final HashMap<String, Object> map = new HashMap<>();
            final ArrayList<GradleRepository> repositories = new ArrayList<>(shared.defaultRepositories());
            map.put("repositoryCodes", repositories.stream().map(GradleRepository::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
            buildGradleTemplate.write(context, input.buildGradleKtsFile(), input, map);
        }

        return Output.builder().fromInput(input).build();
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends RootProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        Shared shared();


        @Value.Default default GradleProject project() {
            final String artifactId = shared().defaultArtifactId();
            return GradleProject.builder()
                .coordinate(Coordinate.of(shared().defaultGroupId(), artifactId, shared().defaultVersion()))
                .baseDirectory(shared().baseDirectory())
                .build();
        }


        @Value.Default default ResourcePath buildGradleKtsFile() {
            return project().baseDirectory().appendRelativePath("build.gradle.kts");
        }

        @Value.Default default ResourcePath settingsGradleKtsFile() {
            return project().baseDirectory().appendRelativePath("settings.gradle.kts");
        }

        List<String> includedProjects();
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends RootProjectCompilerData.Output.Builder {
            public Builder fromInput(Input input) {
                baseDirectory(input.project().baseDirectory());
                return this;
            }
        }

        static Builder builder() { return new Builder(); }


        ResourcePath baseDirectory();
    }
}
