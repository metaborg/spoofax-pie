package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class IntellijProject {
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


    public IntellijProject(TemplateCompiler templateCompiler) {
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

    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        // Gradle files
        {
            final HashMap<String, Object> map = new HashMap<>();

            final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
            dependencies.add(GradleConfiguredDependency.implementation(shared.spoofaxIntellijDep()));
            dependencies.add(GradleConfiguredDependency.implementation(shared.daggerDep()));
            dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
            dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));
            map.put("dependencyCodes", dependencies.stream().map(GradleConfiguredDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));

            buildGradleTemplate.write(input, map, input.buildGradleKtsFile());
        }

        // IntelliJ files
        pluginXmlTemplate.write(input, input.genPluginXmlFile());

        // Class files
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        packageInfoTemplate.write(input, input.genPackageInfo().file(classesGenDirectory));
        moduleTemplate.write(input, input.genModule().file(classesGenDirectory));
        componentTemplate.write(input, input.genComponent().file(classesGenDirectory));
        pluginTemplate.write(input, input.genPlugin().file(classesGenDirectory));
        loaderTemplate.write(input, input.genLoader().file(classesGenDirectory));
        languageTemplate.write(input, input.genLanguage().file(classesGenDirectory));
        fileTypeTemplate.write(input, input.genFileType().file(classesGenDirectory));
        fileElementTypeTemplate.write(input, input.genFileElementType().file(classesGenDirectory));
        fileTypeFactoryTemplate.write(input, input.genFileTypeFactory().file(classesGenDirectory));
        syntaxHighlighterFactoryTemplate.write(input, input.syntaxHighlighterFactory().file(classesGenDirectory));
        parserDefinitionTemplate.write(input, input.parserDefinition().file(classesGenDirectory));

        return Output.builder().fromInput(input).build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends IntellijProjectData.Input.Builder {}

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


        /// IntelliJ files

        default ResourcePath genPluginXmlFile() {
            return shared().intellijProject().genSourceSpoofaxResourcesDirectory().appendRelativePath("plugin.xml");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return shared().intellijProject().genSourceSpoofaxJavaDirectory();
        }


        /// IntelliJ project classes

        // package-info

        @Value.Default default TypeInfo genPackageInfo() {
            return TypeInfo.of(shared().intellijPackage(), "package-info");
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
            return TypeInfo.of(shared().intellijPackage(), shared().defaultClassPrefix() + "IntellijModule");
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
            return TypeInfo.of(shared().intellijPackage(), shared().defaultClassPrefix() + "IntellijComponent");
        }

        Optional<TypeInfo> manualComponent();

        default TypeInfo component() {
            if(classKind().isManual() && manualComponent().isPresent()) {
                return manualComponent().get();
            }
            return genComponent();
        }

        // Plugin

        @Value.Default default TypeInfo genPlugin() {
            return TypeInfo.of(shared().intellijPackage(), shared().defaultClassPrefix() + "Plugin");
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
            return TypeInfo.of(shared().intellijPackage(), shared().defaultClassPrefix() + "Loader");
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
            return TypeInfo.of(shared().intellijPackage(), shared().defaultClassPrefix() + "Language");
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
            return TypeInfo.of(shared().intellijPackage(), shared().defaultClassPrefix() + "FileType");
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
            return TypeInfo.of(shared().intellijPackage(), shared().defaultClassPrefix() + "FileElementType");
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
            return TypeInfo.of(shared().intellijPackage(), shared().defaultClassPrefix() + "FileTypeFactory");
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
            return TypeInfo.of(shared().intellijPackage(), shared().defaultClassPrefix() + "SyntaxHighlighterFactory");
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
            return TypeInfo.of(shared().intellijPackage(), shared().defaultClassPrefix() + "ParserDefinition");
        }

        Optional<TypeInfo> manualParserDefinition();

        default TypeInfo parserDefinition() {
            if(classKind().isManual() && manualParserDefinition().isPresent()) {
                return manualParserDefinition().get();
            }
            return genParserDefinition();
        }


        // TODO: add check
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends IntellijProjectData.Output.Builder {
            public Builder fromInput(Input input) {
                return this;
            }
        }

        static Builder builder() {
            return new Builder();
        }
    }
}
