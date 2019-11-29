package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class AdapterProject {
    private final Template buildGradleTemplate;
    private final Template settingsGradleTemplate;
    private final ResourceService resourceService;
    private final Charset charset;

    private AdapterProject(
        Template buildGradleTemplate,
        Template settingsGradleTemplate,
        ResourceService resourceService,
        Charset charset
    ) {
        this.settingsGradleTemplate = settingsGradleTemplate;
        this.resourceService = resourceService;
        this.buildGradleTemplate = buildGradleTemplate;
        this.charset = charset;
    }

    public static AdapterProject fromClassLoaderResources(ResourceService resourceService, Charset charset) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(AdapterProject.class);
        return new AdapterProject(
            templateCompiler.compile("adapter_project/build.gradle.kts.mustache"),
            templateCompiler.compile("gradle_project/settings.gradle.kts.mustache"),
            resourceService,
            charset
        );
    }


    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();
        final GradleProject adapterProject = shared.adapterProject();

        final HierarchicalResource baseDirectory = resourceService.getHierarchicalResource(adapterProject.baseDirectory());
        baseDirectory.ensureDirectoryExists();

        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.api(input.languageProjectDependency()));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCoreDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.pieApiDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.pieDaggerDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.daggerDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));

        final HierarchicalResource buildGradleKtsFile = resourceService.getHierarchicalResource(input.buildGradleKtsFile());
        try(final ResourceWriter writer = new ResourceWriter(buildGradleKtsFile, charset)) {
            final HashMap<String, Object> map = new HashMap<>();
            map.put("dependencyCodes", dependencies.stream().map(GradleConfiguredDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
            buildGradleTemplate.execute(input, map, writer);
            writer.flush();
        }

        try {
            input.settingsGradleKtsFile().ifPresent((f) -> {
                final HierarchicalResource settingsGradleKtsFile = resourceService.getHierarchicalResource(f);
                try(final ResourceWriter writer = new ResourceWriter(settingsGradleKtsFile, charset)) {
                    settingsGradleTemplate.execute(input, writer);
                    writer.flush();
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        return Output.builder().fromInput(input).build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends AdapterProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        Parser.Input parser();

        Optional<Styler.Input> styler();

        Optional<StrategoRuntime.Input> strategoRuntime();

        Optional<ConstraintAnalyzer.Input> constraintAnalyzer();


        @Value.Default default ResourcePath buildGradleKtsFile() {
            return shared().adapterProject().baseDirectory().appendRelativePath("build.gradle.kts");
        }

        @Value.Default default boolean standaloneProject() {
            return false;
        }

        @Value.Default @SuppressWarnings("immutables:untype") default Optional<ResourcePath> settingsGradleKtsFile() {
            if(standaloneProject()) {
                return Optional.of(shared().adapterProject().baseDirectory().appendRelativePath("settings.gradle.kts"));
            } else {
                return Optional.empty();
            }
        }


        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        Optional<String> manualInstanceClass();

        Optional<String> manualModuleClass();

        Optional<String> manualComponentClass();

        @Value.Default default String genInstanceClass() {
            return shared().classSuffix() + "Instance";
        }

        @Value.Derived default String genInstanceFileName() {
            return genInstanceClass() + ".java";
        }

        @Value.Default default String genModuleClass() {
            return shared().classSuffix() + "Module";
        }

        @Value.Derived default String genModuleFileName() {
            return genModuleClass() + ".java";
        }

        @Value.Default default String genComponentClass() {
            return shared().classSuffix() + "StylerFactory";
        }

        @Value.Derived default String genComponentFileName() {
            return genComponentClass() + ".java";
        }


        GradleDependency languageProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends AdapterProjectData.Output.Builder {
            public Builder fromInput(Input input) {
                baseDirectory(input.shared().adapterProject().baseDirectory());
                buildGradleKtsFile(input.buildGradleKtsFile());
                settingsGradleKtsFile(input.settingsGradleKtsFile());
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath baseDirectory();

        ResourcePath buildGradleKtsFile();

        Optional<ResourcePath> settingsGradleKtsFile();
    }
}
