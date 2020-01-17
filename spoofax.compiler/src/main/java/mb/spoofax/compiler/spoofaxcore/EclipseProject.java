package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
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
public class EclipseProject {
    private final Template buildGradleTemplate;
    private final Template settingsGradleTemplate;
    private final Template pluginXmlTemplate;
    private final Template manifestTemplate;
    private final Template pluginTemplate;
    private final Template moduleTemplate;
    private final Template componentTemplate;
    private final Template identifiersTemplate;
    private final Template editorTrackerTemplate;
    private final ResourceService resourceService;
    private final Charset charset;

    public EclipseProject(
        Template buildGradleTemplate,
        Template settingsGradleTemplate,
        Template pluginXmlTemplate,
        Template manifestTemplate,
        Template pluginTemplate,
        Template moduleTemplate,
        Template componentTemplate,
        Template identifiersTemplate,
        Template editorTrackerTemplate,
        ResourceService resourceService,
        Charset charset
    ) {
        this.buildGradleTemplate = buildGradleTemplate;
        this.settingsGradleTemplate = settingsGradleTemplate;
        this.pluginXmlTemplate = pluginXmlTemplate;
        this.manifestTemplate = manifestTemplate;
        this.pluginTemplate = pluginTemplate;
        this.moduleTemplate = moduleTemplate;
        this.componentTemplate = componentTemplate;
        this.identifiersTemplate = identifiersTemplate;
        this.editorTrackerTemplate = editorTrackerTemplate;
        this.resourceService = resourceService;
        this.charset = charset;
    }

    public static EclipseProject fromClassLoaderResources(ResourceService resourceService, Charset charset) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(CliProject.class);
        return new EclipseProject(
            templateCompiler.getOrCompile("eclipse_project/build.gradle.kts.mustache"),
            templateCompiler.getOrCompile("gradle_project/settings.gradle.kts.mustache"),
            templateCompiler.getOrCompile("eclipse_project/plugin.xml.mustache"),
            templateCompiler.getOrCompile("eclipse_project/MANIFEST.MF.mustache"),
            templateCompiler.getOrCompile("eclipse_project/Plugin.java.mustache"),
            templateCompiler.getOrCompile("eclipse_project/EclipseModule.java.mustache"),
            templateCompiler.getOrCompile("eclipse_project/EclipseComponent.java.mustache"),
            templateCompiler.getOrCompile("eclipse_project/EclipseIdentifiers.java.mustache"),
            templateCompiler.getOrCompile("eclipse_project/EditorTracker.java.mustache"),
            resourceService,
            charset
        );
    }


    public Output compile(EclipseProject.Input input) throws IOException {
        final Shared shared = input.shared();

        final HierarchicalResource baseDirectory = resourceService.getHierarchicalResource(shared.eclipseProject().baseDirectory());
        baseDirectory.ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.buildGradleKtsFile()).createParents(), charset)) {
            final HashMap<String, Object> map = new HashMap<>();
            final ArrayList<GradleRepository> repositories = new ArrayList<>(shared.defaultRepositories());
            map.put("repositoryCodes", repositories.stream().map(GradleRepository::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
            final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
            dependencies.add(GradleConfiguredDependency.implementation(input.adapterProjectDependency()));
            dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
            dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));
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

        try(final ResourceWriter writer = new ResourceWriter(baseDirectory.appendSegment("plugin.xml"), charset)) {
            pluginXmlTemplate.execute(input, writer);
            writer.flush();
        }
        try(final ResourceWriter writer = new ResourceWriter(baseDirectory.appendRelativePath("META-INF/MANIFEST.MF").createParents(), charset)) {
            manifestTemplate.execute(input, writer);
            writer.flush();
        }

        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        resourceService.getHierarchicalResource(classesGenDirectory).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genPlugin().file(classesGenDirectory)).createParents(), charset)) {
            pluginTemplate.execute(input, writer);
            writer.flush();
        }
        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genModule().file(classesGenDirectory)).createParents(), charset)) {
            moduleTemplate.execute(input, writer);
            writer.flush();
        }
        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genComponent().file(classesGenDirectory)).createParents(), charset)) {
            componentTemplate.execute(input, writer);
            writer.flush();
        }


        return Output.builder().fromInput(input).build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends EclipseProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        AdapterProject.Input adapterProject();


        /// Configuration

        GradleDependency adapterProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();


        /// Identifiers

        @Value.Default default String pluginId() {
            return shared().eclipseProject().coordinate().artifactId();
        }

        @Value.Default default String contextId() { return pluginId() + ".context"; }

        @Value.Default default String addNatureCommandId() {
            return pluginId() + ".nature.add";
        }

        @Value.Default default String removeNatureCommandId() {
            return pluginId() + ".nature.remove";
        }

        @Value.Default default String observeCommandId() {
            return pluginId() + ".observe";
        }

        @Value.Default default String unobserveCommandId() {
            return pluginId() + ".unobserve";
        }

        @Value.Default default String runCommandCommandId() {
            return pluginId() + ".command";
        }

        @Value.Default default String baseMarkerId() {
            return pluginId() + ".marker";
        }

        @Value.Default default String infoMarkerId() {
            return baseMarkerId() + ".info";
        }

        @Value.Default default String warningMarkerId() {
            return baseMarkerId() + ".warning";
        }

        @Value.Default default String errorMarkerId() {
            return baseMarkerId() + ".error";
        }


        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return shared().eclipseProject().baseDirectory().appendRelativePath("build.gradle.kts");
        }

        @Value.Default default boolean standaloneProject() {
            return false;
        }

        @Value.Default @SuppressWarnings("immutables:untype") default Optional<ResourcePath> settingsGradleKtsFile() {
            if(standaloneProject()) {
                return Optional.of(shared().eclipseProject().baseDirectory().appendRelativePath("settings.gradle.kts"));
            } else {
                return Optional.empty();
            }
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return shared().eclipseProject().genSourceSpoofaxJavaDirectory();
        }


        /// Eclipse project classes

        // Plugin

        @Value.Default default TypeInfo genPlugin() {
            return TypeInfo.of(shared().eclipsePackage(), shared().classPrefix() + "Plugin");
        }

        Optional<TypeInfo> manualPlugin();

        default TypeInfo plugin() {
            if(classKind().isManual() && manualPlugin().isPresent()) {
                return manualPlugin().get();
            }
            return genPlugin();
        }

        // Dagger component

        @Value.Default default TypeInfo genComponent() {
            return TypeInfo.of(shared().eclipsePackage(), shared().classPrefix() + "EclipseComponent");
        }

        Optional<TypeInfo> manualComponent();

        default TypeInfo component() {
            if(classKind().isManual() && manualComponent().isPresent()) {
                return manualComponent().get();
            }
            return genComponent();
        }

        default TypeInfo daggerComponent() {
            return TypeInfo.of(component().packageId(), "Dagger" + component().id());
        }

        // Dagger module

        @Value.Default default TypeInfo genModule() {
            return TypeInfo.of(shared().eclipsePackage(), shared().classPrefix() + "EclipseModule");
        }

        Optional<TypeInfo> manualModule();

        default TypeInfo module() {
            if(classKind().isManual() && manualModule().isPresent()) {
                return manualModule().get();
            }
            return genModule();
        }

        // Eclipse Identifiers

        @Value.Default default TypeInfo genEclipseIdentifiers() {
            return TypeInfo.of(shared().eclipsePackage(), shared().classPrefix() + "EclipseIdentifiers");
        }

        Optional<TypeInfo> manualEclipseIdentifiers();

        default TypeInfo eclipseIdentifiers() {
            if(classKind().isManual() && manualEclipseIdentifiers().isPresent()) {
                return manualEclipseIdentifiers().get();
            }
            return genEclipseIdentifiers();
        }

        // Editor Tracker

        @Value.Default default TypeInfo genEditorTracker() {
            return TypeInfo.of(shared().eclipsePackage(), shared().classPrefix() + "EditorTracker");
        }

        Optional<TypeInfo> manualEditorTracker();

        default TypeInfo editorTracker() {
            if(classKind().isManual() && manualEditorTracker().isPresent()) {
                return manualEditorTracker().get();
            }
            return genEditorTracker();
        }


        // TODO: implement check
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends EclipseProjectData.Output.Builder {
            public Builder fromInput(Input input) {
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }
    }
}
