package mb.spoofax.compiler.platform;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.Supplier;
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
import mb.spoofax.core.Coordinate;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Value.Enclosing
public class EclipseProjectCompiler implements TaskDef<Supplier<Result<Option<EclipseProjectCompiler.Input>, ?>>, Result<None, ?>> {
    private final TemplateWriter pluginXmlTemplate;
    private final TemplateWriter manifestTemplate;
    private final TemplateWriter packageInfoTemplate;
    private final TemplateWriter componentExtensionSchemaTemplate;
    private final TemplateWriter componentCustomizerTemplate;
    private final TemplateWriter participantTemplate;
    private final TemplateWriter participantFactoryTemplate;
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
    private final TemplateWriter checkCallbackTemplate;
    private final TemplateWriter metadataProviderTemplate;

    @Inject public EclipseProjectCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.pluginXmlTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/plugin.xml.mustache");
        this.manifestTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/MANIFEST.MF.mustache");
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/package-info.java.mustache");
        this.componentExtensionSchemaTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/component.exsd.mustache");
        this.componentCustomizerTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/ComponentCustomizer.java.mustache");
        this.participantTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/Participant.java.mustache");
        this.participantFactoryTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/ParticipantFactory.java.mustache");
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
        this.checkCallbackTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/CheckCallback.java.mustache");
        this.metadataProviderTemplate = templateCompiler.getOrCompileToWriter("eclipse_project/MetadataProvider.java.mustache");
    }


    @Override public String getId() {return getClass().getName();}

    @Override
    public Result<None, ?> exec(ExecContext context, Supplier<Result<Option<EclipseProjectCompiler.Input>, ?>> input) throws IOException {
        return context.require(input).mapThrowing(o -> o.mapThrowingOrElse(i -> compile(context, i), () -> None.instance));
    }

    @Override public boolean shouldExecWhenAffected(Supplier<Result<Option<Input>, ?>> input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public None compile(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.

        // Eclipse files
        pluginXmlTemplate.write(context, input.pluginXmlFile(), input);
        componentExtensionSchemaTemplate.write(context, input.componentExtensionSchemaFile(), input);
        manifestTemplate.write(context, input.manifestMfFile(), input);

        // Class files
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        if(input.adapterProjectDependency().isSome()) {
            // Only generate package-info.java if the adapter project is a separate project. Otherwise we will have
            // two package-info.java files in the same package, which is an error.
            packageInfoTemplate.write(context, input.packageInfo().file(generatedJavaSourcesDirectory), input);
        }
        componentCustomizerTemplate.write(context, input.baseComponentCustomizer().file(generatedJavaSourcesDirectory), input);
        participantTemplate.write(context, input.baseParticipant().file(generatedJavaSourcesDirectory), input);
        participantFactoryTemplate.write(context, input.baseParticipantFactory().file(generatedJavaSourcesDirectory), input);
        pluginTemplate.write(context, input.basePlugin().file(generatedJavaSourcesDirectory), input);
        moduleTemplate.write(context, input.baseEclipseModule().file(generatedJavaSourcesDirectory), input);
        componentTemplate.write(context, input.baseEclipseComponent().file(generatedJavaSourcesDirectory), input);
        identifiersTemplate.write(context, input.baseEclipseIdentifiers().file(generatedJavaSourcesDirectory), input);
        documentProviderTemplate.write(context, input.baseDocumentProvider().file(generatedJavaSourcesDirectory), input);
        editorTemplate.write(context, input.baseEditor().file(generatedJavaSourcesDirectory), input);
        editorTrackerTemplate.write(context, input.baseEditorTracker().file(generatedJavaSourcesDirectory), input);
        natureTemplate.write(context, input.baseNature().file(generatedJavaSourcesDirectory), input);
        addNatureHandlerTemplate.write(context, input.addNatureHandler().file(generatedJavaSourcesDirectory), input);
        removeNatureHandlerTemplate.write(context, input.removeNatureHandler().file(generatedJavaSourcesDirectory), input);
        projectBuilderTemplate.write(context, input.baseProjectBuilder().file(generatedJavaSourcesDirectory), input);
        mainMenuTemplate.write(context, input.baseMainMenu().file(generatedJavaSourcesDirectory), input);
        editorContextMenuTemplate.write(context, input.baseEditorContextMenu().file(generatedJavaSourcesDirectory), input);
        resourceContextMenuTemplate.write(context, input.baseResourceContextMenu().file(generatedJavaSourcesDirectory), input);
        runCommandHandlerTemplate.write(context, input.baseRunCommandHandler().file(generatedJavaSourcesDirectory), input);
        observeHandlerTemplate.write(context, input.baseObserveHandler().file(generatedJavaSourcesDirectory), input);
        unobserveHandlerTemplate.write(context, input.baseUnobserveHandler().file(generatedJavaSourcesDirectory), input);
        checkCallbackTemplate.write(context, input.checkCallback().file(generatedJavaSourcesDirectory), input);
        if(input.adapterProjectCompilerInput().multilangAnalyzer().isPresent()) {
            metadataProviderTemplate.write(context, input.baseMetadataProvider().file(generatedJavaSourcesDirectory), input);
        }

        return None.instance;
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
        bundleDependencies.add(GradleConfiguredBundleDependency.bundleApi(shared.toolingEclipseBundleDep()));
        if(input.adapterProjectCompilerInput().multilangAnalyzer().isPresent()) {
            bundleDependencies.add(GradleConfiguredBundleDependency.bundleApi(shared.multilangEclipseDep()));
        }
        if(input.adapterProjectCompilerInput().strategoRuntime().isPresent()) {
            bundleDependencies.add(GradleConfiguredBundleDependency.bundleApi(shared.strategolibEclipseDep()));
            bundleDependencies.add(GradleConfiguredBundleDependency.bundleApi(shared.gppEclipseDep()));
        }
        if(input.adapterProjectCompilerInput().dependOnRv32Im()) {
            bundleDependencies.add(GradleConfiguredBundleDependency.bundleApi(shared.rv32ImEclipseDep()));
        }
        if(input.adapterProjectCompilerInput().dynamix().isPresent()) {
            bundleDependencies.add(GradleConfiguredBundleDependency.bundleApi(shared.dynamixRuntimeEclipseDep()));
            bundleDependencies.add(GradleConfiguredBundleDependency.bundleApi(shared.timRuntimeEclipseDep()));
        }
        input.languageProjectDependency().ifSome(d -> bundleDependencies.add(GradleConfiguredBundleDependency.bundleEmbedApi(d)));
        input.adapterProjectDependency().ifSome(d -> bundleDependencies.add(GradleConfiguredBundleDependency.bundleEmbedApi(d)));
        return bundleDependencies;
    }

    public LinkedHashSet<String> getExportPackages(Input input) {
        final LinkedHashSet<String> packages = new LinkedHashSet<>();
        packages.add(input.languageProjectCompilerInput().languageProject().packageId());
        packages.add(input.adapterProjectCompilerInput().adapterProject().packageId());
        packages.add(input.packageId());
        return packages;
    }

    public LinkedHashSet<String> getPrivatePackages(Input input) {
        final LinkedHashSet<String> packages = new LinkedHashSet<>();
        input.languageProjectCompilerInput().strategoRuntime().ifPresent(i -> packages.addAll(i.strategyPackageIds()));
        return packages;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends EclipseProjectCompilerData.Input.Builder {
            public Builder withDefaultProjectFromParentDirectory(ResourcePath parentDirectory, Shared shared) {
                return withDefaultProject(parentDirectory.appendRelativePath(defaultArtifactId(shared)), shared);
            }

            public Builder withDefaultProject(ResourcePath baseDirectory, Shared shared) {
                final GradleProject gradleProject = GradleProject.builder()
                    .coordinate(new Coordinate(shared.defaultGroupId(), defaultArtifactId(shared), shared.defaultVersion()))
                    .baseDirectory(baseDirectory)
                    .build();
                return this
                    .project(gradleProject)
                    .packageId(defaultPackageId(shared))
                    .shared(shared)
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


            public Builder withDefaultsSameProjectFromParentDirectory(ResourcePath parentDirectory, Shared shared) {
                return withDefaultsSameProject(parentDirectory.appendRelativePath(defaultSameArtifactId(shared)), shared);
            }

            public Builder withDefaultsSameProject(ResourcePath baseDirectory, Shared shared) {
                final GradleProject gradleProject = GradleProject.builder()
                    .coordinate(new Coordinate(shared.defaultGroupId(), defaultSameArtifactId(shared), shared.defaultVersion()))
                    .baseDirectory(baseDirectory)
                    .build();
                return this
                    .project(gradleProject)
                    .packageId(defaultSamePackageId(shared))
                    .languageProjectDependency(Option.ofNone())
                    .adapterProjectDependency(Option.ofNone())
                    .shared(shared)
                    ;
            }

            public static String defaultSameProjectSuffix() {
                return "";
            }

            public static String defaultSameArtifactId(Shared shared) {
                return shared.defaultArtifactId() + defaultSameProjectSuffix();
            }

            public static String defaultSamePackageId(Shared shared) {
                return shared.defaultPackageId() + defaultSameProjectSuffix();
            }
        }

        static Builder builder() {return new Builder();}


        /// Project

        GradleProject project();

        String packageId();


        /// Gradle configuration

        /* None indicates that the language project is the same project as the adapter project */
        @Value.Default default Option<GradleDependency> languageProjectDependency() {
            return adapterProjectCompilerInput().languageProjectDependency();
        }

        /* None indicates that the Eclipse project is the same project as the adapter project */
        @Value.Default default Option<GradleDependency> adapterProjectDependency() {
            return Option.ofSome(adapterProjectCompilerInput().adapterProject().project().asProjectDependency());
        }

        List<GradleConfiguredDependency> additionalDependencies();

        List<GradleConfiguredBundleDependency> additionalBundleDependencies();


        /// Eclipse configuration

        @Value.Default default String pluginId() {return project().coordinate().artifactId;}

        @Value.Default default String componentExtensionPointId() {return pluginId() + ".component";}

        @Value.Default default String contextId() {return pluginId() + ".context";}

        @Value.Default default String documentProviderId() {return pluginId() + ".documentprovider";}

        @Value.Default default String editorId() {return pluginId() + ".editor";}

        @Value.Default default String natureRelativeId() {return "nature";}

        @Value.Default default String natureId() {return pluginId() + "." + natureRelativeId();}

        @Value.Default default String toggleCommentCommandId() {return pluginId() + ".togglecomment";}

        @Value.Default default String addNatureCommandId() {return natureId() + ".add";}

        @Value.Default default String removeNatureCommandId() {return natureId() + ".remove";}

        @Value.Default default String projectBuilderRelativeId() {return "builder";}

        @Value.Default default String projectBuilderId() {return pluginId() + "." + projectBuilderRelativeId();}

        @Value.Default default String baseMarkerId() {return pluginId() + ".marker";}

        @Value.Default default String infoMarkerId() {return baseMarkerId() + ".info";}

        @Value.Default default String warningMarkerId() {return baseMarkerId() + ".warning";}

        @Value.Default default String errorMarkerId() {return baseMarkerId() + ".error";}

        @Value.Default default String observeCommandId() {return pluginId() + ".observe";}

        @Value.Default default String unobserveCommandId() {return pluginId() + ".unobserve";}

        @Value.Default default String runCommandId() {return pluginId() + ".runcommand";}

        @Value.Default default String baseMenuId() {return pluginId() + ".menu";}

        @Value.Default default String resourceContextMenuId() {return baseMenuId() + ".resource.context";}

        @Value.Default default String editorContextMenuId() {return baseMenuId() + ".editor.context";}

        @Value.Default default String mainMenuId() {return baseMenuId() + ".main";}

        @Value.Default default String mainMenuDynamicId() {return mainMenuId() + ".dynamic";}


        Optional<ResourcePath> fileIconRelativePath();


        /// Eclipse files

        @Value.Default default ResourcePath generatedResourcesDirectory() {
            return project().buildGeneratedResourcesDirectory().appendRelativePath("eclipse");
        }

        default ResourcePath pluginXmlFile() {
            return generatedResourcesDirectory().appendRelativePath("plugin.xml");
        }

        default ResourcePath componentExtensionSchemaFile() {
            return generatedResourcesDirectory().appendRelativePath("schema/" + componentExtensionPointId() + ".exsd");
        }

        default ResourcePath manifestMfFile() {
            return generatedResourcesDirectory().appendRelativePath("META-INF/MANIFEST.MF");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {return ClassKind.Generated;}

        @Value.Default default ResourcePath generatedJavaSourcesDirectory() {
            return project().buildGeneratedSourcesDirectory().appendRelativePath("eclipse");
        }


        /// Eclipse project classes

        // package-info

        @Value.Default default TypeInfo basePackageInfo() {return TypeInfo.of(packageId(), "package-info");}

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return basePackageInfo();
        }

        @Value.Default default TypeInfo baseComponentCustomizer() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "ComponentCustomizer");
        }

        Optional<TypeInfo> extendComponentCustomizer();

        default TypeInfo componentCustomizer() {
            return extendComponentCustomizer().orElseGet(this::baseComponentCustomizer);
        }

        // Participant

        @Value.Default default TypeInfo baseParticipant() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EclipseParticipant");
        }

        Optional<TypeInfo> extendParticipant();

        default TypeInfo participant() {
            return extendParticipant().orElseGet(this::baseParticipant);
        }

        // LanguageFactory

        @Value.Default default TypeInfo baseParticipantFactory() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EclipseParticipantFactory");
        }

        Optional<TypeInfo> extendParticipantFactory();

        default TypeInfo participantFactory() {
            return extendParticipantFactory().orElseGet(this::baseParticipantFactory);
        }

        // Plugin

        @Value.Default default TypeInfo basePlugin() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Plugin");
        }

        Optional<TypeInfo> extendPlugin();

        default TypeInfo plugin() {
            return extendPlugin().orElseGet(this::basePlugin);
        }

        // Dagger component

        @Value.Default default TypeInfo baseEclipseComponent() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EclipseComponent");
        }

        Optional<TypeInfo> extendEclipseComponent();

        default TypeInfo eclipseComponent() {
            return extendEclipseComponent().orElseGet(this::baseEclipseComponent);
        }

        default TypeInfo daggerEclipseComponent() {
            return TypeInfo.of(eclipseComponent().packageId(), "Dagger" + eclipseComponent().id());
        }

        // Dagger module

        @Value.Default default TypeInfo baseEclipseModule() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EclipseModule");
        }

        Optional<TypeInfo> extendEclipseModule();

        default TypeInfo eclipseModule() {
            return extendEclipseModule().orElseGet(this::baseEclipseModule);
        }

        // Eclipse Identifiers

        @Value.Default default TypeInfo baseEclipseIdentifiers() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EclipseIdentifiers");
        }

        Optional<TypeInfo> extendEclipseIdentifiers();

        default TypeInfo eclipseIdentifiers() {
            return extendEclipseIdentifiers().orElseGet(this::baseEclipseIdentifiers);
        }

        // Document provider

        @Value.Default default TypeInfo baseDocumentProvider() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "DocumentProvider");
        }

        Optional<TypeInfo> extendDocumentProvider();

        default TypeInfo documentProvider() {
            return extendDocumentProvider().orElseGet(this::baseDocumentProvider);
        }

        // Editor

        @Value.Default default TypeInfo baseEditor() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Editor");
        }

        Optional<TypeInfo> extendEditor();

        default TypeInfo editor() {
            return extendEditor().orElseGet(this::baseEditor);
        }

        // Editor Tracker

        @Value.Default default TypeInfo baseEditorTracker() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EditorTracker");
        }

        Optional<TypeInfo> extendEditorTracker();

        default TypeInfo editorTracker() {
            return extendEditorTracker().orElseGet(this::baseEditorTracker);
        }

        // Project nature

        @Value.Default default TypeInfo baseNature() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Nature");
        }

        Optional<TypeInfo> extendNature();

        default TypeInfo nature() {
            return extendNature().orElseGet(this::baseNature);
        }

        // Add nature handler

        @Value.Default default TypeInfo baseAddNatureHandler() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "AddNatureHandler");
        }

        Optional<TypeInfo> extendAddNatureHandler();

        default TypeInfo addNatureHandler() {
            return extendAddNatureHandler().orElseGet(this::baseAddNatureHandler);
        }

        // Remove nature handler

        @Value.Default default TypeInfo baseRemoveNatureHandler() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "RemoveNatureHandler");
        }

        Optional<TypeInfo> extendRemoveNatureHandler();

        default TypeInfo removeNatureHandler() {
            return extendRemoveNatureHandler().orElseGet(this::baseRemoveNatureHandler);
        }

        // Project builder

        @Value.Default default TypeInfo baseProjectBuilder() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "ProjectBuilder");
        }

        Optional<TypeInfo> extendProjectBuilder();

        default TypeInfo projectBuilder() {
            return extendProjectBuilder().orElseGet(this::baseProjectBuilder);
        }

        // Main menu

        @Value.Default default TypeInfo baseMainMenu() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "MainMenu");
        }

        Optional<TypeInfo> extendMainMenu();

        default TypeInfo mainMenu() {
            return extendMainMenu().orElseGet(this::baseMainMenu);
        }

        // Editor context menu

        @Value.Default default TypeInfo baseEditorContextMenu() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "EditorContextMenu");
        }

        Optional<TypeInfo> extendEditorContextMenu();

        default TypeInfo editorContextMenu() {
            return extendEditorContextMenu().orElseGet(this::baseEditorContextMenu);
        }

        // Resource context menu

        @Value.Default default TypeInfo baseResourceContextMenu() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "ResourceContextMenu");
        }

        Optional<TypeInfo> extendResourceContextMenu();

        default TypeInfo resourceContextMenu() {
            return extendResourceContextMenu().orElseGet(this::baseResourceContextMenu);
        }

        // Command handler

        @Value.Default default TypeInfo baseRunCommandHandler() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "RunCommandHandler");
        }

        Optional<TypeInfo> extendRunCommandHandler();

        default TypeInfo runCommandHandler() {
            return extendRunCommandHandler().orElseGet(this::baseRunCommandHandler);
        }

        // Observe handler

        @Value.Default default TypeInfo baseObserveHandler() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "ObserveHandler");
        }

        Optional<TypeInfo> extendObserveHandler();

        default TypeInfo observeHandler() {
            return extendObserveHandler().orElseGet(this::baseObserveHandler);
        }

        // Unobserve handler

        @Value.Default default TypeInfo baseUnobserveHandler() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "UnobserveHandler");
        }

        Optional<TypeInfo> extendUnobserveHandler();

        default TypeInfo unobserveHandler() {
            return extendUnobserveHandler().orElseGet(this::baseUnobserveHandler);
        }

        // Check callback

        @Value.Default default TypeInfo baseCheckCallback() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "CheckCallback");
        }

        Optional<TypeInfo> extendCheckCallback();

        default TypeInfo checkCallback() {
            return extendCheckCallback().orElseGet(this::baseCheckCallback);
        }

        // Language Metadata Provider

        @Value.Default default TypeInfo baseMetadataProvider() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "MetadataProvider");
        }

        Optional<TypeInfo> extendMetadataProvider();

        default TypeInfo metadataProvider() {
            return extendMetadataProvider().orElseGet(this::baseMetadataProvider);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ArrayList<ResourcePath> resourcePaths() {
            final ArrayList<ResourcePath> sourcePaths = new ArrayList<>();
            sourcePaths.add(generatedResourcesDirectory());
            return sourcePaths;
        }

        default ArrayList<ResourcePath> resources() {
            final ArrayList<ResourcePath> providedResources = new ArrayList<>();
            providedResources.add(pluginXmlFile());
            providedResources.add(componentExtensionSchemaFile());
            providedResources.add(manifestMfFile());
            return providedResources;
        }

        default ArrayList<ResourcePath> javaSourcePaths() {
            final ArrayList<ResourcePath> sourcePaths = new ArrayList<>();
            sourcePaths.add(generatedJavaSourcesDirectory());
            return sourcePaths;
        }

        default ArrayList<ResourcePath> javaSourceFiles() {
            final ArrayList<ResourcePath> generatedFiles = new ArrayList<>();
            if(classKind().isGenerating()) {
                if(adapterProjectDependency().isSome()) {
                    // Only generate package-info.java if the adapter project is a separate project. Otherwise we will
                    // have two package-info.java files in the same package, which is an error.
                    generatedFiles.add(basePackageInfo().file(this.generatedJavaSourcesDirectory()));
                }
                generatedFiles.add(baseParticipant().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseParticipantFactory().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(basePlugin().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseEclipseComponent().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseEclipseModule().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseEclipseIdentifiers().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseDocumentProvider().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseEditor().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseEditorTracker().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseNature().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseAddNatureHandler().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseRemoveNatureHandler().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseProjectBuilder().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseMainMenu().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseEditorContextMenu().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseResourceContextMenu().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseRunCommandHandler().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseObserveHandler().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseUnobserveHandler().file(this.generatedJavaSourcesDirectory()));
                generatedFiles.add(baseCheckCallback().file(this.generatedJavaSourcesDirectory()));
                if(adapterProjectCompilerInput().multilangAnalyzer().isPresent()) {
                    generatedFiles.add(baseMetadataProvider().file(this.generatedJavaSourcesDirectory()));
                }
            }
            return generatedFiles;
        }


        /// Automatically provided sub-inputs

        @Value.Auxiliary Shared shared();

        AdapterProjectCompiler.Input adapterProjectCompilerInput();

        LanguageProjectCompiler.Input languageProjectCompilerInput();
    }
}
