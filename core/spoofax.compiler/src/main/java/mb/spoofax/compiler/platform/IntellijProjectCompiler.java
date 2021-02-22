package mb.spoofax.compiler.platform;

import mb.pie.api.ExecContext;
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

@Value.Enclosing
public class IntellijProjectCompiler implements TaskDef<IntellijProjectCompiler.Input, IntellijProjectCompiler.Output> {
    private final TemplateWriter pluginXmlTemplate;
    private final TemplateWriter packageInfoTemplate;
    private final TemplateWriter moduleTemplate;
    private final TemplateWriter componentTemplate;
    private final TemplateWriter pluginTemplate;
    private final TemplateWriter loaderTemplate;
    private final TemplateWriter languageTemplate;
    private final TemplateWriter fileTypeTemplate;
    private final TemplateWriter fileElementTypeTemplate;
    private final TemplateWriter fileTypeFactoryTemplate;
    private final TemplateWriter lexerFactoryTemplate;
    private final TemplateWriter syntaxHighlighterFactoryTemplate;
    private final TemplateWriter parserDefinitionTemplate;

    @Inject public IntellijProjectCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.pluginXmlTemplate = templateCompiler.getOrCompileToWriter("intellij_project/plugin.xml.mustache");
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("intellij_project/package-info.java.mustache");
        this.moduleTemplate = templateCompiler.getOrCompileToWriter("intellij_project/Module.java.mustache");
        this.componentTemplate = templateCompiler.getOrCompileToWriter("intellij_project/Component.java.mustache");
        this.pluginTemplate = templateCompiler.getOrCompileToWriter("intellij_project/Plugin.java.mustache");
        this.loaderTemplate = templateCompiler.getOrCompileToWriter("intellij_project/Loader.java.mustache");
        this.languageTemplate = templateCompiler.getOrCompileToWriter("intellij_project/Language.java.mustache");
        this.fileTypeTemplate = templateCompiler.getOrCompileToWriter("intellij_project/FileType.java.mustache");
        this.fileElementTypeTemplate = templateCompiler.getOrCompileToWriter("intellij_project/FileElementType.java.mustache");
        this.fileTypeFactoryTemplate = templateCompiler.getOrCompileToWriter("intellij_project/FileTypeFactory.java.mustache");
        this.lexerFactoryTemplate = templateCompiler.getOrCompileToWriter("intellij_project/LexerFactory.java.mustache");
        this.syntaxHighlighterFactoryTemplate = templateCompiler.getOrCompileToWriter("intellij_project/SyntaxHighlighterFactory.java.mustache");
        this.parserDefinitionTemplate = templateCompiler.getOrCompileToWriter("intellij_project/ParserDefinition.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        final Shared shared = input.shared();

        final Output.Builder outputBuilder = Output.builder();
        if(input.classKind().isManual()) return outputBuilder.build(); // Nothing to generate: return.

        // IntelliJ files
        pluginXmlTemplate.write(context, input.pluginXmlFile(), input);

        // Class files
        final ResourcePath classesGenDirectory = input.generatedJavaSourcesDirectory();
        packageInfoTemplate.write(context, input.basePackageInfo().file(classesGenDirectory), input);
        moduleTemplate.write(context, input.baseModule().file(classesGenDirectory), input);
        componentTemplate.write(context, input.baseComponent().file(classesGenDirectory), input);
        pluginTemplate.write(context, input.basePlugin().file(classesGenDirectory), input);
        loaderTemplate.write(context, input.baseLoader().file(classesGenDirectory), input);
        languageTemplate.write(context, input.baseLanguage().file(classesGenDirectory), input);
        fileTypeTemplate.write(context, input.baseFileType().file(classesGenDirectory), input);
        fileElementTypeTemplate.write(context, input.baseFileElementType().file(classesGenDirectory), input);
        fileTypeFactoryTemplate.write(context, input.baseFileTypeFactory().file(classesGenDirectory), input);
        lexerFactoryTemplate.write(context, input.baseLexerFactory().file(classesGenDirectory), input);
        syntaxHighlighterFactoryTemplate.write(context, input.syntaxHighlighterFactory().file(classesGenDirectory), input);
        parserDefinitionTemplate.write(context, input.parserDefinition().file(classesGenDirectory), input);

        return outputBuilder.build();
    }


    public ArrayList<GradleConfiguredDependency> getDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.apiPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessorPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.implementation(shared.pieRuntimeDep()));
        // HACK: exclude adapter project dependency, as slf4j must be excluded from it for the IntelliJ plugin to work, which is not possible with 'GradleConfiguredDependency'.
        //dependencies.add(GradleConfiguredDependency.implementation(input.adapterProjectDependency()));
        dependencies.add(GradleConfiguredDependency.implementation(shared.spoofaxIntellijDep()));
        dependencies.add(GradleConfiguredDependency.implementation(shared.daggerDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));
        return dependencies;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends IntellijProjectCompilerData.Input.Builder {
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
                return ".intellij";
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

        @Value.Default default GradleDependency adapterProjectDependency() {
            return adapterProjectCompilerInput().adapterProject().project().asProjectDependency();
        }

        List<GradleConfiguredDependency> additionalDependencies();


        /// IntelliJ files

        @Value.Default default ResourcePath generatedResourcesDirectory() {
            return project().buildGeneratedResourcesDirectory().appendRelativePath("intellij");
        }

        default ResourcePath pluginXmlFile() {
            return generatedResourcesDirectory().appendRelativePath("META-INF/plugin.xml");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        @Value.Default default ResourcePath generatedJavaSourcesDirectory() {
            return project().buildGeneratedSourcesDirectory().appendRelativePath("intellij");
        }


        /// IntelliJ project classes

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

        // IntelliJ module

        @Value.Default default TypeInfo baseModule() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "IntellijModule");
        }

        Optional<TypeInfo> extendModule();

        default TypeInfo module() {
            return extendModule().orElseGet(this::baseModule);
        }

        // IntelliJ component

        @Value.Default default TypeInfo baseComponent() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "IntellijComponent");
        }

        Optional<TypeInfo> extendComponent();

        default TypeInfo component() {
            return extendComponent().orElseGet(this::baseComponent);
        }

        default TypeInfo daggerComponent() {
            return TypeInfo.of(component().packageId(), "Dagger" + component().id());
        }

        // Plugin

        @Value.Default default TypeInfo basePlugin() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Plugin");
        }

        Optional<TypeInfo> extendPlugin();

        default TypeInfo plugin() {
            return extendPlugin().orElseGet(this::basePlugin);
        }

        // Loader

        @Value.Default default TypeInfo baseLoader() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Loader");
        }

        Optional<TypeInfo> extendLoader();

        default TypeInfo loader() {
            return extendLoader().orElseGet(this::baseLoader);
        }

        // Language

        @Value.Default default TypeInfo baseLanguage() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Language");
        }

        Optional<TypeInfo> extendLanguage();

        default TypeInfo language() {
            return extendLanguage().orElseGet(this::baseLanguage);
        }

        // File type

        @Value.Default default TypeInfo baseFileType() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "FileType");
        }

        Optional<TypeInfo> extendFileType();

        default TypeInfo fileType() {
            return extendFileType().orElseGet(this::baseFileType);
        }

        // File type

        @Value.Default default TypeInfo baseFileElementType() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "FileElementType");
        }

        Optional<TypeInfo> extendFileElementType();

        default TypeInfo fileElementType() {
            return extendFileElementType().orElseGet(this::baseFileElementType);
        }

        // File type factory

        @Value.Default default TypeInfo baseFileTypeFactory() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "FileTypeFactory");
        }

        Optional<TypeInfo> extendFileTypeFactory();

        default TypeInfo fileTypeFactory() {
            return extendFileTypeFactory().orElseGet(this::baseFileTypeFactory);
        }

        // Lexer factory

        @Value.Default default TypeInfo baseLexerFactory() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "LexerFactory");
        }

        Optional<TypeInfo> extendLexerFactory();

        default TypeInfo lexerFactory() {
            return extendLexerFactory().orElseGet(this::baseLexerFactory);
        }

        // Syntax highlighter factory

        @Value.Default default TypeInfo baseSyntaxHighlighterFactory() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "SyntaxHighlighterFactory");
        }

        Optional<TypeInfo> extendSyntaxHighlighterFactory();

        default TypeInfo syntaxHighlighterFactory() {
            return extendSyntaxHighlighterFactory().orElseGet(this::baseSyntaxHighlighterFactory);
        }

        // Parser definition

        @Value.Default default TypeInfo baseParserDefinition() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "ParserDefinition");
        }

        Optional<TypeInfo> extendParserDefinition();

        default TypeInfo parserDefinition() {
            return extendParserDefinition().orElseGet(this::baseParserDefinition);
        }


        /// Provided files

        default ArrayList<ResourcePath> providedFiles() {
            final ArrayList<ResourcePath> generatedFiles = new ArrayList<>();
            generatedFiles.add(pluginXmlFile());
            if(classKind().isGenerating()) {
                generatedFiles.add(basePackageInfo().file(generatedJavaSourcesDirectory()));
                generatedFiles.add(baseModule().file(generatedJavaSourcesDirectory()));
                generatedFiles.add(baseComponent().file(generatedJavaSourcesDirectory()));
                generatedFiles.add(basePlugin().file(generatedJavaSourcesDirectory()));
                generatedFiles.add(baseLoader().file(generatedJavaSourcesDirectory()));
                generatedFiles.add(baseFileType().file(generatedJavaSourcesDirectory()));
                generatedFiles.add(baseFileElementType().file(generatedJavaSourcesDirectory()));
                generatedFiles.add(baseFileTypeFactory().file(generatedJavaSourcesDirectory()));
                generatedFiles.add(baseLexerFactory().file(generatedJavaSourcesDirectory()));
                generatedFiles.add(baseSyntaxHighlighterFactory().file(generatedJavaSourcesDirectory()));
                generatedFiles.add(baseParserDefinition().file(generatedJavaSourcesDirectory()));
            }
            return generatedFiles;
        }


        /// Automatically provided sub-inputs

        Shared shared();

        AdapterProjectCompiler.Input adapterProjectCompilerInput();


        // TODO: add check
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends IntellijProjectCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
