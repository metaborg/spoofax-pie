package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleRepository;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TypeInfo;
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
public class CliProject {
    private final Template buildGradleTemplate;
    private final Template settingsGradleTemplate;
    private final Template mainTemplate;
    private final ResourceService resourceService;
    private final Charset charset;


    private CliProject(
        Template buildGradleTemplate,
        Template settingsGradleTemplate,
        Template mainTemplate,
        ResourceService resourceService,
        Charset charset
    ) {
        this.mainTemplate = mainTemplate;
        this.resourceService = resourceService;
        this.charset = charset;
        this.buildGradleTemplate = buildGradleTemplate;
        this.settingsGradleTemplate = settingsGradleTemplate;
    }

    public static CliProject fromClassLoaderResources(ResourceService resourceService, Charset charset) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(CliProject.class, resourceService, charset);
        return new CliProject(
            templateCompiler.getOrCompile("cli_project/build.gradle.kts.mustache"),
            templateCompiler.getOrCompile("gradle_project/settings.gradle.kts.mustache"),
            templateCompiler.getOrCompile("cli_project/Main.java.mustache"),
            resourceService,
            charset
        );
    }


    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        resourceService.getHierarchicalResource(shared.cliProject().baseDirectory()).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.buildGradleKtsFile()).createParents(), charset)) {
            final HashMap<String, Object> map = new HashMap<>();
            final ArrayList<GradleRepository> repositories = new ArrayList<>(shared.defaultRepositories());
            map.put("repositoryCodes", repositories.stream().map(GradleRepository::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
            final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
            dependencies.add(GradleConfiguredDependency.implementation(input.adapterProjectDependency()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.spoofaxCliDep()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.logBackendSLF4JDep()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.slf4jSimpleDep()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.pieRuntimeDep()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.pieDaggerDep()));
            dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
            map.put("dependencyCodes", dependencies.stream().map(GradleConfiguredDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
            buildGradleTemplate.execute(input, map, writer);
            writer.flush();
        }

        try {
            input.settingsGradleKtsFile().ifPresent((f) -> {
                try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(f).createParents(), charset)) {
                    settingsGradleTemplate.execute(input, writer);
                    writer.flush();
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        resourceService.getHierarchicalResource(classesGenDirectory).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genMain().file(classesGenDirectory)).createParents(), charset)) {
            mainTemplate.execute(input, writer);
            writer.flush();
        }

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

        @Value.Default default boolean standaloneProject() {
            return false;
        }

        @Value.Default @SuppressWarnings("immutables:untype") default Optional<ResourcePath> settingsGradleKtsFile() {
            if(standaloneProject()) {
                return Optional.of(shared().cliProject().baseDirectory().appendRelativePath("settings.gradle.kts"));
            } else {
                return Optional.empty();
            }
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
