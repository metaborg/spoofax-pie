package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassInfo;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.NameType;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.UniqueNamer;
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
    private final Template checkTaskDefTemplate;
    private final Template componentTemplate;
    private final Template moduleTemplate;
    private final Template instanceTemplate;
    private final ResourceService resourceService;
    private final Charset charset;
    private final Parser parserCompiler;
    private final Styler stylerCompiler;
    private final StrategoRuntime strategoRuntimeCompiler;
    private final ConstraintAnalyzer constraintAnalyzerCompiler;

    private AdapterProject(
        Template buildGradleTemplate,
        Template settingsGradleTemplate,
        Template checkTaskDefTemplate,
        Template componentTemplate,
        Template moduleTemplate,
        Template instanceTemplate,
        ResourceService resourceService,
        Charset charset,
        Parser parserCompiler,
        Styler stylerCompiler,
        StrategoRuntime strategoRuntimeCompiler,
        ConstraintAnalyzer constraintAnalyzerCompiler
    ) {
        this.settingsGradleTemplate = settingsGradleTemplate;
        this.componentTemplate = componentTemplate;
        this.moduleTemplate = moduleTemplate;
        this.instanceTemplate = instanceTemplate;
        this.checkTaskDefTemplate = checkTaskDefTemplate;
        this.resourceService = resourceService;
        this.buildGradleTemplate = buildGradleTemplate;
        this.charset = charset;
        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
    }

    public static AdapterProject fromClassLoaderResources(
        ResourceService resourceService,
        Charset charset,
        Parser parserCompiler,
        Styler stylerCompiler,
        StrategoRuntime strategoRuntimeCompiler,
        ConstraintAnalyzer constraintAnalyzerCompiler
    ) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(AdapterProject.class);
        return new AdapterProject(
            templateCompiler.compile("adapter_project/build.gradle.kts.mustache"),
            templateCompiler.compile("gradle_project/settings.gradle.kts.mustache"),
            templateCompiler.compile("adapter_project/CheckTaskDef.java.mustache"),
            templateCompiler.compile("adapter_project/Component.java.mustache"),
            templateCompiler.compile("adapter_project/Module.java.mustache"),
            templateCompiler.compile("adapter_project/Instance.java.mustache"),
            resourceService,
            charset,
            parserCompiler,
            stylerCompiler,
            strategoRuntimeCompiler,
            constraintAnalyzerCompiler
        );
    }


    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        resourceService.getHierarchicalResource(input.gradleGenDirectory()).ensureDirectoryExists();

        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.api(input.languageProjectDependency()));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCoreDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.pieApiDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.pieDaggerDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.daggerDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));
        dependencies.add(GradleConfiguredDependency.annotationProcessor(shared.daggerCompilerDep()));

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.buildGradleKtsFile()), charset)) {
            final HashMap<String, Object> map = new HashMap<>();
            map.put("dependencyCodes", dependencies.stream().map(GradleConfiguredDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
            buildGradleTemplate.execute(input, map, writer);
            writer.flush();
        }

        try {
            input.settingsGradleKtsFile().ifPresent((f) -> {
                try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(f), charset)) {
                    settingsGradleTemplate.execute(input, writer);
                    writer.flush();
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        final Parser.AdapterProjectOutput parserOutput = parserCompiler.compileAdapterProject(input.parser());
        final Optional<Styler.AdapterProjectOutput> stylerOutput;
        final Optional<StrategoRuntime.AdapterProjectOutput> strategoRuntimeOutput;
        final Optional<ConstraintAnalyzer.AdapterProjectOutput> constraintAnalyzerOutput;
        try {
            stylerOutput = input.styler().map((i) -> {
                try {
                    return stylerCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            strategoRuntimeOutput = input.strategoRuntime().map((i) -> {
                try {
                    return strategoRuntimeCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            constraintAnalyzerOutput = input.constraintAnalyzer().map((i) -> {
                try {
                    return constraintAnalyzerCompiler.compileAdapterProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        resourceService.getHierarchicalResource(classesGenDirectory).ensureDirectoryExists();

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.checkTaskDef().file(classesGenDirectory)).createParents(), charset)) {
            checkTaskDefTemplate.execute(input, writer);
            writer.flush();
        }

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genComponent().file(classesGenDirectory)).createParents(), charset)) {
            componentTemplate.execute(input, writer);
            writer.flush();
        }

        final UniqueNamer uniqueNamer = new UniqueNamer();
        final HashMap<String, Object> map = new HashMap<>();
        map.put("tokenizeTaskDefNameType", new NameType(uniqueNamer.makeUnique(input.parser().tokenizeTaskDef().asVariableId()), input.parser().tokenizeTaskDef().qualifiedId()));
        map.put("checkTaskDefNameType", new NameType(uniqueNamer.makeUnique(input.checkTaskDef().asVariableId()), input.checkTaskDef().qualifiedId()));
        if(input.styler().isPresent()) {
            final ClassInfo styleTaskDef = input.styler().get().styleTaskDef();
            map.put("styleTaskDefNameType", new NameType(uniqueNamer.makeUnique(styleTaskDef.asVariableId()), styleTaskDef.qualifiedId()));
        } else {
            map.put("styleTaskDefNameType", new NameType(uniqueNamer.makeUnique("nullStyler"), "mb.spoofax.core.language.taskdef.NullStyler"));
        }
        final ArrayList<NameType> additionalTaskDefs = new ArrayList<>();
        parserOutput.additionalTaskDefs().forEach((ci) -> additionalTaskDefs.add(new NameType(uniqueNamer.makeUnique(ci.asVariableId()), ci.qualifiedId())));
        stylerOutput.ifPresent(o -> o.additionalTaskDefs().forEach((ci) -> additionalTaskDefs.add(new NameType(uniqueNamer.makeUnique(ci.asVariableId()), ci.qualifiedId()))));
        strategoRuntimeOutput.ifPresent(o -> o.additionalTaskDefs().forEach((ci) -> additionalTaskDefs.add(new NameType(uniqueNamer.makeUnique(ci.asVariableId()), ci.qualifiedId()))));
        constraintAnalyzerOutput.ifPresent(o -> o.additionalTaskDefs().forEach((ci) -> additionalTaskDefs.add(new NameType(uniqueNamer.makeUnique(ci.asVariableId()), ci.qualifiedId()))));
        map.put("additionalTaskDefs", additionalTaskDefs);

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genModule().file(classesGenDirectory)).createParents(), charset)) {
            moduleTemplate.execute(input, map, writer);
            writer.flush();
        }

        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genInstance().file(classesGenDirectory)).createParents(), charset)) {
            instanceTemplate.execute(input, map, writer);
            writer.flush();
        }

        return Output.builder()
            .parser(parserOutput)
            .styler(stylerOutput)
            .strategoRuntime(strategoRuntimeOutput)
            .constraintAnalyzer(constraintAnalyzerOutput)
            .build();
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


        /// Configuration

        GradleDependency languageProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();


        /// Gradle files

        default ResourcePath gradleGenDirectory() {
            return shared().adapterProject().baseDirectory();
        }

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return gradleGenDirectory().appendRelativePath("build.gradle.kts");
        }

        @Value.Default default boolean standaloneProject() {
            return false;
        }

        @Value.Default @SuppressWarnings("immutables:untype") default Optional<ResourcePath> settingsGradleKtsFile() {
            if(standaloneProject()) {
                return Optional.of(gradleGenDirectory().appendRelativePath("settings.gradle.kts"));
            } else {
                return Optional.empty();
            }
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Adapter project classes

        default ResourcePath classesGenDirectory() {
            return shared().adapterProject().genSourceSpoofaxJavaDirectory();
        }

        default String genPackage() {
            return shared().adapterProject().packageId();
        }

        // Dagger component

        @Value.Default default ClassInfo genComponent() {
            return ClassInfo.of(genPackage(), shared().classSuffix() + "Component");
        }

        Optional<ClassInfo> manualComponent();

        default ClassInfo component() {
            if(classKind().isManual() && manualComponent().isPresent()) {
                return manualComponent().get();
            }
            return genComponent();
        }

        // Dagger module

        @Value.Default default ClassInfo genModule() {
            return ClassInfo.of(genPackage(), shared().classSuffix() + "Module");
        }

        Optional<ClassInfo> manualModule();

        default ClassInfo module() {
            if(classKind().isManual() && manualModule().isPresent()) {
                return manualModule().get();
            }
            return genModule();
        }

        // Language instance

        @Value.Default default ClassInfo genInstance() {
            return ClassInfo.of(genPackage(), shared().classSuffix() + "Instance");
        }

        Optional<ClassInfo> manualInstance();

        default ClassInfo instance() {
            if(classKind().isManual() && manualInstance().isPresent()) {
                return manualInstance().get();
            }
            return genInstance();
        }


        /// Adapter project task definitions

        default String taskDefGenPackage() {
            return shared().adapterProject().packageId() + ".taskdef";
        }

        // Check task definition

        @Value.Default default ClassInfo genCheckTaskDef() {
            return ClassInfo.of(taskDefGenPackage(), shared().classSuffix() + "Check");
        }

        Optional<ClassInfo> manualCheckTaskDef();

        default ClassInfo checkTaskDef() {
            if(classKind().isManual() && manualCheckTaskDef().isPresent()) {
                return manualCheckTaskDef().get();
            }
            return genCheckTaskDef();
        }


        @Value.Check default void check() {
            final ClassKind kind = classKind();
            final boolean manual = kind.isManual();
            if(!manual) return;
            if(!manualComponent().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualComponent' has not been set");
            }
            if(!manualModule().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualModule' has not been set");
            }
            if(!manualInstance().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualInstance' has not been set");
            }
            if(!manualCheckTaskDef().isPresent()) {
                throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualCheckTaskDef' has not been set");
            }
        }
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends AdapterProjectData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Parser.AdapterProjectOutput parser();

        Optional<Styler.AdapterProjectOutput> styler();

        Optional<StrategoRuntime.AdapterProjectOutput> strategoRuntime();

        Optional<ConstraintAnalyzer.AdapterProjectOutput> constraintAnalyzer();
    }
}
