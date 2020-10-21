package mb.spoofax.compiler.platform;

import mb.common.option.Option;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredBundleDependency;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class EclipseProjectCompiler implements TaskDef<EclipseProjectCompiler.Input, EclipseProjectCompiler.Output> {
    private final TemplateWriter pluginXmlTemplate;
    private final TemplateWriter manifestTemplate;
    private final TemplateWriter packageInfoTemplate;
    private final TemplateWriter pluginTemplate;
    private final TemplateWriter moduleTemplate;
    private final TemplateWriter componentTemplate;
    private final TemplateWriter identifiersTemplate;
    private final TemplateWriter documentProviderTemplate;
    private final TemplateWriter editorTemplate;
    private final TemplateWriter editorTrackerTemplate;
    private final TemplateWriter natureTemplate;
    private final TemplateWriter addNatureHandlerTemplate;
    private final TemplateWriter removeNatureHandlerTemplate;
    private final TemplateWriter projectBuilderTemplate;
    private final TemplateWriter mainMenuTemplate;
    private final TemplateWriter editorContextMenuTemplate;
    private final TemplateWriter resourceContextMenuTemplate;
    private final TemplateWriter runCommandHandlerTemplate;
    private final TemplateWriter observeHandlerTemplate;
    private final TemplateWriter unobserveHandlerTemplate;
    private final TemplateWriter metadataProviderTemplate;

    @Inject public EclipseProjectCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.pluginXmlTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/plugin.xml.mustache");
        this.manifestTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/MANIFEST.MF.mustache");
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/package-info.java.mustache");
        this.pluginTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/Plugin.java.mustache");
        this.moduleTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/Module.java.mustache");
        this.componentTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/Component.java.mustache");
        this.identifiersTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/Identifiers.java.mustache");
        this.documentProviderTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/DocumentProvider.java.mustache");
        this.editorTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/Editor.java.mustache");
        this.editorTrackerTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/EditorTracker.java.mustache");
        this.natureTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/Nature.java.mustache");
        this.addNatureHandlerTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/AddNatureHandler.java.mustache");
        this.removeNatureHandlerTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/RemoveNatureHandler.java.mustache");
        this.projectBuilderTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/ProjectBuilder.java.mustache");
        this.mainMenuTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/MainMenu.java.mustache");
        this.editorContextMenuTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/EditorContextMenu.java.mustache");
        this.resourceContextMenuTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/ResourceContextMenu.java.mustache");
        this.runCommandHandlerTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/RunCommandHandler.java.mustache");
        this.observeHandlerTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/ObserveHandler.java.mustache");
        this.unobserveHandlerTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/UnobserveHandler.java.mustache");
        this.metadataProviderTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/MetadataProvider.java.mustache");
    }


    @Override public String getId() { return getClass().getName(); }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        final Shared shared = input.shared();

        // Eclipse files
        pluginXmlTemplate.write(context, input.pluginXmlFile(), input);
        manifestTemplate.write(context, input.manifestMfFile(), input);

        // Class files
        final ResourcePath classesGenDirectory = input.generatedJavaSourcesDirectory();
        packageInfoTemplate.write(context, input.genPackageInfo().file(classesGenDirectory), input);
        pluginTemplate.write(context, input.genPlugin().file(classesGenDirectory), input);
        moduleTemplate.write(context, input.genEclipseModule().file(classesGenDirectory), input);
        componentTemplate.write(context, input.genEclipseComponent().file(classesGenDirectory), input);
        identifiersTemplate.write(context, input.genEclipseIdentifiers().file(classesGenDirectory), input);
        documentProviderTemplate.write(context, input.genDocumentProvider().file(classesGenDirectory), input);
        editorTemplate.write(context, input.genEditor().file(classesGenDirectory), input);
        editorTrackerTemplate.write(context, input.genEditorTracker().file(classesGenDirectory), input);
        natureTemplate.write(context, input.genNature().file(classesGenDirectory), input);
        addNatureHandlerTemplate.write(context, input.addNatureHandler().file(classesGenDirectory), input);
        removeNatureHandlerTemplate.write(context, input.removeNatureHandler().file(classesGenDirectory), input);
        projectBuilderTemplate.write(context, input.genProjectBuilder().file(classesGenDirectory), input);
        mainMenuTemplate.write(context, input.genMainMenu().file(classesGenDirectory), input);
        editorContextMenuTemplate.write(context, input.genEditorContextMenu().file(classesGenDirectory), input);
        resourceContextMenuTemplate.write(context, input.genResourceContextMenu().file(classesGenDirectory), input);
        runCommandHandlerTemplate.write(context, input.genRunCommandHandler().file(classesGenDirectory), input);
        observeHandlerTemplate.write(context, input.genObserveHandler().file(classesGenDirectory), input);
        unobserveHandlerTemplate.write(context, input.genUnobserveHandler().file(classesGenDirectory), input);
        if(input.adapterProjectCompilerInput().multilangAnalyzer().isPresent()) {
            metadataProviderTemplate.write(context, input.genMetadataProvider().file(classesGenDirectory), input);
        }

        return Output.builder().build();
    }


    public ArrayList<GradleConfiguredDependency> getDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.apiPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessorPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));
        return dependencies;
    }

    public ArrayList<GradleConfiguredBundleDependency> getBundleDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredBundleDependency> bundleDependencies = new ArrayList<>(input.additionalBundleDependencies());
        bundleDependencies.add(GradleConfiguredBundleDependency.bundleTargetPlatformApi("javax.inject", null));
        bundleDependencies.add(GradleConfiguredBundleDependency.bundleApi(shared.spoofaxEclipseDep()));
        if(input.adapterProjectCompilerInput().multilangAnalyzer().isPresent()) {
            bundleDependencies.add(GradleConfiguredBundleDependency.bundleApi(shared.multilangEclipseDep()));
        }
        input.languageProjectDependency().ifSome((d) -> bundleDependencies.add(GradleConfiguredBundleDependency.bundleEmbedApi(d)));
        bundleDependencies.add(GradleConfiguredBundleDependency.bundleEmbedApi(input.adapterProjectDependency()));
        return bundleDependencies;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends EclipseProjectCompilerData.Input.Builder {
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
                return ".eclipse";
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


        /// Gradle configuration

        /* None indicates that the language project is the same project as the adapter project */
        default Option<GradleDependency> languageProjectDependency() {
            return adapterProjectCompilerInput().languageProjectDependency();
        }

        default GradleDependency adapterProjectDependency() {
            return adapterProjectCompilerInput().adapterProject().project().asProjectDependency();
        }

        List<GradleConfiguredDependency> additionalDependencies();

        List<GradleConfiguredBundleDependency> additionalBundleDependencies();


        /// Eclipse configuration

        @Value.Default default String pluginId() { return project().coordinate().artifactId(); }

        @Value.Default default String contextId() { return pluginId() + ".context"; }

        @Value.Default default String documentProviderId() { return pluginId() + ".documentprovider"; }

        @Value.Default default String editorId() { return pluginId() + ".editor"; }

        @Value.Default default String natureRelativeId() { return "nature"; }

        @Value.Default default String natureId() { return pluginId() + "." + natureRelativeId(); }

        @Value.Default default String addNatureCommandId() { return natureId() + ".add"; }

        @Value.Default default String removeNatureCommandId() { return natureId() + ".remove"; }

        @Value.Default default String projectBuilderRelativeId() { return "builder"; }

        @Value.Default default String projectBuilderId() { return pluginId() + "." + projectBuilderRelativeId(); }

        @Value.Default default String baseMarkerId() { return pluginId() + ".marker"; }

        @Value.Default default String infoMarkerId() { return baseMarkerId() + ".info"; }

        @Value.Default default String warningMarkerId() { return baseMarkerId() + ".warning"; }

        @Value.Default default String errorMarkerId() { return baseMarkerId() + ".error"; }

        @Value.Default default String observeCommandId() { return pluginId() + ".observe"; }

        @Value.Default default String unobserveCommandId() { return pluginId() + ".unobserve"; }

        @Value.Default default String runCommandId() { return pluginId() + ".runcommand"; }

        @Value.Default default String baseMenuId() { return pluginId() + ".menu"; }

        @Value.Default default String resourceContextMenuId() { return baseMenuId() + ".resource.context"; }

        @Value.Default default String editorContextMenuId() { return baseMenuId() + ".editor.context"; }

        @Value.Default default String mainMenuId() { return baseMenuId() + ".main"; }

        @Value.Default default String mainMenuDynamicId() { return mainMenuId() + ".dynamic"; }


        Optional<ResourcePath> fileIconRelativePath();


        /// Eclipse files

        @Value.Default default ResourcePath generatedResourcesDirectory() {
            return project().buildGeneratedResourcesDirectory().appendRelativePath("eclipse");
        }

        default ResourcePath pluginXmlFile() {
            return generatedResourcesDirectory().appendRelativePath("plugin.xml");
        }

        default ResourcePath manifestMfFile() {
            return generatedResourcesDirectory().appendRelativePath("META-INF/MANIFEST.MF");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }

        @Value.Default default ResourcePath generatedJavaSourcesDirectory() {
            return project().buildGeneratedSourcesDirectory().appendRelativePath("eclipse");
        }


        /// Eclipse project classes

        // package-info

        @Value.Default default TypeInfo genPackageInfo() { return TypeInfo.of(packageId(), "package-info"); }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return genPackageInfo();
        }

        // Plugin

        @Value.Default default TypeInfo genPlugin() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Plugin");
        }

        Optional<TypeInfo> manualPlugin();

        default TypeInfo plugin() {
            if(classKind().isManual() && manualPlugin().isPresent()) {
                return manualPlugin().get();
            }
            return genPlugin();
        }

        // Dagger component

        @Value.Default default TypeInfo genEclipseComponent() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EclipseComponent");
        }

        Optional<TypeInfo> manualEclipseComponent();

        default TypeInfo eclipseComponent() {
            if(classKind().isManual() && manualEclipseComponent().isPresent()) {
                return manualEclipseComponent().get();
            }
            return genEclipseComponent();
        }

        default TypeInfo daggerEclipseComponent() {
            return TypeInfo.of(eclipseComponent().packageId(), "Dagger" + eclipseComponent().id());
        }

        // Dagger module

        @Value.Default default TypeInfo genEclipseModule() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EclipseModule");
        }

        Optional<TypeInfo> manualEclipseModule();

        default TypeInfo eclipseModule() {
            if(classKind().isManual() && manualEclipseModule().isPresent()) {
                return manualEclipseModule().get();
            }
            return genEclipseModule();
        }

        // Eclipse Identifiers

        @Value.Default default TypeInfo genEclipseIdentifiers() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EclipseIdentifiers");
        }

        Optional<TypeInfo> manualEclipseIdentifiers();

        default TypeInfo eclipseIdentifiers() {
            if(classKind().isManual() && manualEclipseIdentifiers().isPresent()) {
                return manualEclipseIdentifiers().get();
            }
            return genEclipseIdentifiers();
        }

        // Document provider

        @Value.Default default TypeInfo genDocumentProvider() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "DocumentProvider");
        }

        Optional<TypeInfo> manualDocumentProvider();

        default TypeInfo documentProvider() {
            if(classKind().isManual() && manualDocumentProvider().isPresent()) {
                return manualDocumentProvider().get();
            }
            return genDocumentProvider();
        }

        // Editor

        @Value.Default default TypeInfo genEditor() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Editor");
        }

        Optional<TypeInfo> manualEditor();

        default TypeInfo editor() {
            if(classKind().isManual() && manualEditor().isPresent()) {
                return manualEditor().get();
            }
            return genEditor();
        }

        // Editor Tracker

        @Value.Default default TypeInfo genEditorTracker() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EditorTracker");
        }

        Optional<TypeInfo> manualEditorTracker();

        default TypeInfo editorTracker() {
            if(classKind().isManual() && manualEditorTracker().isPresent()) {
                return manualEditorTracker().get();
            }
            return genEditorTracker();
        }

        // Project nature

        @Value.Default default TypeInfo genNature() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Nature");
        }

        Optional<TypeInfo> manualNature();

        default TypeInfo nature() {
            if(classKind().isManual() && manualNature().isPresent()) {
                return manualNature().get();
            }
            return genNature();
        }

        // Add nature handler

        @Value.Default default TypeInfo genAddNatureHandler() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "AddNatureHandler");
        }

        Optional<TypeInfo> manualAddNatureHandler();

        default TypeInfo addNatureHandler() {
            if(classKind().isManual() && manualAddNatureHandler().isPresent()) {
                return manualAddNatureHandler().get();
            }
            return genAddNatureHandler();
        }

        // Remove nature handler

        @Value.Default default TypeInfo genRemoveNatureHandler() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "RemoveNatureHandler");
        }

        Optional<TypeInfo> manualRemoveNatureHandler();

        default TypeInfo removeNatureHandler() {
            if(classKind().isManual() && manualRemoveNatureHandler().isPresent()) {
                return manualRemoveNatureHandler().get();
            }
            return genRemoveNatureHandler();
        }

        // Project builder

        @Value.Default default TypeInfo genProjectBuilder() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "ProjectBuilder");
        }

        Optional<TypeInfo> manualProjectBuilder();

        default TypeInfo projectBuilder() {
            if(classKind().isManual() && manualProjectBuilder().isPresent()) {
                return manualProjectBuilder().get();
            }
            return genProjectBuilder();
        }

        // Main menu

        @Value.Default default TypeInfo genMainMenu() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "MainMenu");
        }

        Optional<TypeInfo> manualMainMenu();

        default TypeInfo mainMenu() {
            if(classKind().isManual() && manualMainMenu().isPresent()) {
                return manualMainMenu().get();
            }
            return genMainMenu();
        }

        // Editor context menu

        @Value.Default default TypeInfo genEditorContextMenu() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EditorContextMenu");
        }

        Optional<TypeInfo> manualEditorContextMenu();

        default TypeInfo editorContextMenu() {
            if(classKind().isManual() && manualEditorContextMenu().isPresent()) {
                return manualEditorContextMenu().get();
            }
            return genEditorContextMenu();
        }

        // Resource context menu

        @Value.Default default TypeInfo genResourceContextMenu() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "ResourceContextMenu");
        }

        Optional<TypeInfo> manualResourceContextMenu();

        default TypeInfo resourceContextMenu() {
            if(classKind().isManual() && manualResourceContextMenu().isPresent()) {
                return manualResourceContextMenu().get();
            }
            return genResourceContextMenu();
        }

        // Command handler

        @Value.Default default TypeInfo genRunCommandHandler() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "RunCommandHandler");
        }

        Optional<TypeInfo> manualRunCommandHandler();

        default TypeInfo runCommandHandler() {
            if(classKind().isManual() && manualRunCommandHandler().isPresent()) {
                return manualRunCommandHandler().get();
            }
            return genRunCommandHandler();
        }

        // Observe handler

        @Value.Default default TypeInfo genObserveHandler() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "ObserveHandler");
        }

        Optional<TypeInfo> manualObserveHandler();

        default TypeInfo observeHandler() {
            if(classKind().isManual() && manualObserveHandler().isPresent()) {
                return manualObserveHandler().get();
            }
            return genObserveHandler();
        }

        // Unobserve handler

        @Value.Default default TypeInfo genUnobserveHandler() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "UnobserveHandler");
        }

        Optional<TypeInfo> manualUnobserveHandler();

        default TypeInfo unobserveHandler() {
            if(classKind().isManual() && manualUnobserveHandler().isPresent()) {
                return manualUnobserveHandler().get();
            }
            return genUnobserveHandler();
        }

        // Language Metadata Provider

        @Value.Default default TypeInfo genMetadataProvider() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "MetadataProvider");
        }

        Optional<TypeInfo> manualMetadataProvider();

        default TypeInfo metadataProvider() {
            if(classKind().isManual() && manualMetadataProvider().isPresent()) {
                return manualMetadataProvider().get();
            }
            return genMetadataProvider();
        }


        /// Provided files

        default ArrayList<ResourcePath> providedFiles() {
            final ArrayList<ResourcePath> generatedFiles = new ArrayList<>();
            generatedFiles.add(pluginXmlFile());
            generatedFiles.add(manifestMfFile());
            if(classKind().isGenerating()) {
                generatedFiles.add(genPackageInfo().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genPlugin().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genEclipseComponent().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genEclipseModule().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genEclipseIdentifiers().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genDocumentProvider().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genEditor().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genEditorTracker().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genNature().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genAddNatureHandler().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genRemoveNatureHandler().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genProjectBuilder().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genMainMenu().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genEditorContextMenu().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genResourceContextMenu().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genRunCommandHandler().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genObserveHandler().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(genUnobserveHandler().file(this.generatedJavaSourcesDirectory()));
                if(adapterProjectCompilerInput().multilangAnalyzer().isPresent()) {
                    generatedFiles.add(genMetadataProvider().file(this.generatedJavaSourcesDirectory()));
                }
            }
            return generatedFiles;
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProjectCompiler.Input adapterProjectCompilerInput();

        LanguageProjectCompiler.Input languageProjectCompilerInput();


        // TODO: implement check
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends EclipseProjectCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
