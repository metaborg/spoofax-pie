package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
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

        resourceService.getHierarchicalResource(input.genDirectory()).ensureDirectoryExists();

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

        // TODO: enable when working
//        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genComponentFile()), charset)) {
//            componentTemplate.execute(input, writer);
//            writer.flush();
//        }
//
//        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genModuleFile()), charset)) {
//            moduleTemplate.execute(input, writer);
//            writer.flush();
//        }
//
//        try(final ResourceWriter writer = new ResourceWriter(resourceService.getHierarchicalResource(input.genInstanceFile()), charset)) {
//            instanceTemplate.execute(input, writer);
//            writer.flush();
//        }

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

        @Value.Derived default ResourcePath genDirectory() {
            final GradleProject adapterProject = shared().adapterProject();
            return adapterProject.genSourceSpoofaxJavaDirectory().appendRelativePath(adapterProject.packagePath());
        }


        Optional<String> manualInstanceClass();

        @Value.Default default String genInstanceClass() {
            return shared().classSuffix() + "Instance";
        }

        @Value.Derived default String genInstanceFileName() {
            return genInstanceClass() + ".java";
        }

        @Value.Derived default ResourcePath genInstanceFile() {
            return genDirectory().appendSegment(genInstanceFileName());
        }


        Optional<String> manualModuleClass();

        @Value.Default default String genModuleClass() {
            return shared().classSuffix() + "Module";
        }

        @Value.Derived default String genModuleFileName() {
            return genModuleClass() + ".java";
        }

        @Value.Derived default ResourcePath genModuleFile() {
            return genDirectory().appendSegment(genModuleFileName());
        }


        Optional<String> manualComponentClass();

        @Value.Default default String genComponentClass() {
            return shared().classSuffix() + "Component";
        }

        @Value.Derived default String genComponentFileName() {
            return genComponentClass() + ".java";
        }

        @Value.Derived default ResourcePath genComponentFile() {
            return genDirectory().appendSegment(genComponentFileName());
        }


        GradleDependency languageProjectDependency();

        List<GradleConfiguredDependency> additionalDependencies();
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
