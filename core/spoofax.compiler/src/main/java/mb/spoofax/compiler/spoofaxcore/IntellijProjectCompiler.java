package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.Coordinate;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleProject;
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
    private final TemplateWriter buildGradleTemplate;
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
    private final TemplateWriter syntaxHighlighterFactoryTemplate;
    private final TemplateWriter parserDefinitionTemplate;

    @Inject public IntellijProjectCompiler(TemplateCompiler templateCompiler) {
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("intellij_project/build.gradle.kts.mustache");
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
        this.syntaxHighlighterFactoryTemplate = templateCompiler.getOrCompileToWriter("intellij_project/SyntaxHighlighterFactory.java.mustache");
        this.parserDefinitionTemplate = templateCompiler.getOrCompileToWriter("intellij_project/ParserDefinition.java.mustache");
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        final Shared shared = input.shared();

        // IntelliJ files
        pluginXmlTemplate.write(context, input.pluginXmlFile(), input);

        // Class files
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        packageInfoTemplate.write(context, input.genPackageInfo().file(classesGenDirectory), input);
        moduleTemplate.write(context, input.genModule().file(classesGenDirectory), input);
        componentTemplate.write(context, input.genComponent().file(classesGenDirectory), input);
        pluginTemplate.write(context, input.genPlugin().file(classesGenDirectory), input);
        loaderTemplate.write(context, input.genLoader().file(classesGenDirectory), input);
        languageTemplate.write(context, input.genLanguage().file(classesGenDirectory), input);
        fileTypeTemplate.write(context, input.genFileType().file(classesGenDirectory), input);
        fileElementTypeTemplate.write(context, input.genFileElementType().file(classesGenDirectory), input);
        fileTypeFactoryTemplate.write(context, input.genFileTypeFactory().file(classesGenDirectory), input);
        syntaxHighlighterFactoryTemplate.write(context, input.syntaxHighlighterFactory().file(classesGenDirectory), input);
        parserDefinitionTemplate.write(context, input.parserDefinition().file(classesGenDirectory), input);

        return Output.builder().build();
    }


//    public void generateInitial(Input input) throws IOException {
//        buildGradleTemplate.write(input.buildGradleKtsFile(), input);
//    }

    public ArrayList<GradleConfiguredDependency> getDependencies(Input input) {
        final Shared shared = input.shared();
        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.apiPlatform(shared.spoofaxDependencyConstraintsDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessorPlatform(shared.spoofaxDependencyConstraintsDep()));
        // HACK: exclude adapter project dependency, as slf4j must be excluded from it for the IntelliJ plugin to work, which is not possible with 'GradleConfiguredDependency'.
        //dependencies.add(GradleConfiguredDependency.implementation(input.adapterProjectDependency()));
        dependencies.add(GradleConfiguredDependency.implementation(shared.spoofaxIntellijDep()));
        dependencies.add(GradleConfiguredDependency.implementation(shared.daggerDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));
        return dependencies;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends IntellijProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Project

        @Value.Default default String defaultProjectSuffix() { return ".intellij"; }

        @Value.Default default GradleProject project() {
            final String artifactId = shared().defaultArtifactId() + defaultProjectSuffix();
            return GradleProject.builder()
                .coordinate(Coordinate.of(shared().defaultGroupId(), artifactId, shared().defaultVersion()))
                .baseDirectory(shared().baseDirectory().appendSegment(artifactId))
                .build();
        }

        @Value.Default default String packageId() { return shared().defaultBasePackageId() + defaultProjectSuffix(); }


        /// Gradle configuration

        @Value.Default default GradleDependency adapterProjectDependency() {
            return adapterProjectCompilerInput().adapterProject().project().asProjectDependency();
        }

        List<GradleConfiguredDependency> additionalDependencies();


        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return project().baseDirectory().appendRelativePath("build.gradle.kts");
        }


        /// IntelliJ files

        default ResourcePath pluginXmlFile() {
            return project().genSourceSpoofaxResourcesDirectory().appendRelativePath("META-INF/plugin.xml");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return project().genSourceSpoofaxJavaDirectory();
        }


        /// IntelliJ project classes

        // package-info

        @Value.Default default TypeInfo genPackageInfo() {
            return TypeInfo.of(packageId(), "package-info");
        }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return genPackageInfo();
        }

        // IntelliJ module

        @Value.Default default TypeInfo genModule() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "IntellijModule");
        }

        Optional<TypeInfo> manualModule();

        default TypeInfo module() {
            if(classKind().isManual() && manualModule().isPresent()) {
                return manualModule().get();
            }
            return genModule();
        }

        // IntelliJ component

        @Value.Default default TypeInfo genComponent() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "IntellijComponent");
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

        // Loader

        @Value.Default default TypeInfo genLoader() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Loader");
        }

        Optional<TypeInfo> manualLoader();

        default TypeInfo loader() {
            if(classKind().isManual() && manualLoader().isPresent()) {
                return manualLoader().get();
            }
            return genLoader();
        }

        // Language

        @Value.Default default TypeInfo genLanguage() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "Language");
        }

        Optional<TypeInfo> manualLanguage();

        default TypeInfo language() {
            if(classKind().isManual() && manualLanguage().isPresent()) {
                return manualLanguage().get();
            }
            return genLanguage();
        }

        // File type

        @Value.Default default TypeInfo genFileType() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "FileType");
        }

        Optional<TypeInfo> manualFileType();

        default TypeInfo fileType() {
            if(classKind().isManual() && manualFileType().isPresent()) {
                return manualFileType().get();
            }
            return genFileType();
        }

        // File type

        @Value.Default default TypeInfo genFileElementType() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "FileElementType");
        }

        Optional<TypeInfo> manualFileElementType();

        default TypeInfo fileElementType() {
            if(classKind().isManual() && manualFileElementType().isPresent()) {
                return manualFileElementType().get();
            }
            return genFileElementType();
        }

        // File type factory

        @Value.Default default TypeInfo genFileTypeFactory() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "FileTypeFactory");
        }

        Optional<TypeInfo> manualFileTypeFactory();

        default TypeInfo fileTypeFactory() {
            if(classKind().isManual() && manualFileTypeFactory().isPresent()) {
                return manualFileTypeFactory().get();
            }
            return genFileTypeFactory();
        }

        // Syntax highlighter factory

        @Value.Default default TypeInfo genSyntaxHighlighterFactory() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "SyntaxHighlighterFactory");
        }

        Optional<TypeInfo> manualSyntaxHighlighterFactory();

        default TypeInfo syntaxHighlighterFactory() {
            if(classKind().isManual() && manualSyntaxHighlighterFactory().isPresent()) {
                return manualSyntaxHighlighterFactory().get();
            }
            return genSyntaxHighlighterFactory();
        }

        // Parser definition

        @Value.Default default TypeInfo genParserDefinition() {
            return TypeInfo.of(packageId(), shared().defaultClassPrefix() + "ParserDefinition");
        }

        Optional<TypeInfo> manualParserDefinition();

        default TypeInfo parserDefinition() {
            if(classKind().isManual() && manualParserDefinition().isPresent()) {
                return manualParserDefinition().get();
            }
            return genParserDefinition();
        }


        /// Provided files

        default ArrayList<ResourcePath> providedFiles() {
            final ArrayList<ResourcePath> generatedFiles = new ArrayList<>();
            generatedFiles.add(pluginXmlFile());
            if(classKind().isGenerating()) {
                generatedFiles.add(genPackageInfo().file(classesGenDirectory()));
                generatedFiles.add(genModule().file(classesGenDirectory()));
                generatedFiles.add(genComponent().file(classesGenDirectory()));
                generatedFiles.add(genPlugin().file(classesGenDirectory()));
                generatedFiles.add(genLoader().file(classesGenDirectory()));
                generatedFiles.add(genFileType().file(classesGenDirectory()));
                generatedFiles.add(genFileElementType().file(classesGenDirectory()));
                generatedFiles.add(genFileTypeFactory().file(classesGenDirectory()));
                generatedFiles.add(genSyntaxHighlighterFactory().file(classesGenDirectory()));
                generatedFiles.add(genParserDefinition().file(classesGenDirectory()));
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
