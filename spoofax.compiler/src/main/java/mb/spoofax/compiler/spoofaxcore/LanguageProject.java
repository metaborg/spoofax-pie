package mb.spoofax.compiler.spoofaxcore;

import com.samskivert.mustache.Template;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.GradleAddDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleProject;
import mb.spoofax.compiler.util.ResourceWriter;
import mb.spoofax.compiler.util.StringUtil;
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

import static mb.spoofax.compiler.util.StringUtil.doubleQuote;

@Value.Enclosing
public class LanguageProject {
    private final Template buildGradleTemplate;
    private final Template settingsGradleTemplate;
    private final ResourceService resourceService;
    private final Charset charset;
    private final Parser parserCompiler;
    private final Styler stylerCompiler;
    private final StrategoRuntime strategoRuntimeCompiler;
    private final ConstraintAnalyzer constraintAnalyzerCompiler;

    private LanguageProject(
        Template buildGradleTemplate,
        Template settingsGradleTemplate, ResourceService resourceService,
        Charset charset,
        Parser parserCompiler,
        Styler stylerCompiler,
        StrategoRuntime strategoRuntimeCompiler,
        ConstraintAnalyzer constraintAnalyzerCompiler
    ) {
        this.settingsGradleTemplate = settingsGradleTemplate;
        this.resourceService = resourceService;
        this.charset = charset;
        this.buildGradleTemplate = buildGradleTemplate;
        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
    }

    public static LanguageProject fromClassLoaderResources(
        ResourceService resourceService,
        Charset charset,
        Parser parserCompiler,
        Styler stylerCompiler,
        StrategoRuntime strategoRuntimeCompiler,
        ConstraintAnalyzer constraintAnalyzerCompiler
    ) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(LanguageProject.class);
        return new LanguageProject(
            templateCompiler.compile("language_project/build.gradle.kts.mustache"),
            templateCompiler.compile("gradle_project/settings.gradle.kts.mustache"),
            resourceService,
            charset,
            parserCompiler,
            stylerCompiler,
            strategoRuntimeCompiler,
            constraintAnalyzerCompiler
        );
    }


    public Output compile(Input input) throws IOException {
        final GradleProject languageProject = input.shared().languageProject();

        final HierarchicalResource baseDirectory = resourceService.getHierarchicalResource(languageProject.baseDirectory());
        baseDirectory.ensureDirectoryExists();

        final Parser.LanguageProjectOutput parserOutput = parserCompiler.compileLanguageProject(input.parser());
        final ArrayList<GradleAddDependency> dependencies = new ArrayList<>(parserOutput.dependencies());
        final ArrayList<String> copyResources = new ArrayList<>(parserOutput.copyResources());

        final Optional<Styler.LanguageProjectOutput> stylerOutput;
        final Optional<StrategoRuntime.LanguageProjectOutput> strategoRuntimeOutput;
        final Optional<ConstraintAnalyzer.LanguageProjectOutput> constraintAnalyzerOutput;
        try {
            stylerOutput = input.styler().map((i) -> {
                try {
                    return stylerCompiler.compileLanguageProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            stylerOutput.ifPresent((o) -> {
                dependencies.addAll(o.dependencies());
                copyResources.addAll(o.copyResources());
            });

            strategoRuntimeOutput = input.strategoRuntime().map((i) -> {
                try {
                    return strategoRuntimeCompiler.compileLanguageProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            strategoRuntimeOutput.ifPresent((o) -> {
                dependencies.addAll(o.dependencies());
                copyResources.addAll(o.copyResources());
            });

            constraintAnalyzerOutput = input.constraintAnalyzer().map((i) -> {
                try {
                    return constraintAnalyzerCompiler.compileLanguageProject(i);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            constraintAnalyzerOutput.ifPresent((o) -> {
                dependencies.addAll(o.dependencies());
                copyResources.addAll(o.copyResources());
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        final HierarchicalResource buildGradleKtsFile = resourceService.getHierarchicalResource(input.buildGradleKtsFile());
        try(final ResourceWriter writer = new ResourceWriter(buildGradleKtsFile, charset)) {
            final HashMap<String, Object> map = new HashMap<>();

            final String languageDependencyCode = input.languageSpecificationDependency().caseOf()
                .project((projectPath) -> "createProjectDependency(\"" + projectPath + "\")")
                .module((coordinate) -> "createModuleDependency(\"" + coordinate.toGradleNotation() + "\")")
                .files((filePaths) -> "createFilesDependency(" + filePaths.stream().map((s) -> "\"" + s + "\"").collect(Collectors.joining(", ")) + ")");
            map.put("languageDependencyCode", languageDependencyCode);

            final ArrayList<String> dependencyCodes = dependencies.stream().map(GradleAddDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new));
            final ArrayList<String> copyResourceCodes = copyResources.stream().map(StringUtil::doubleQuote).collect(Collectors.toCollection(ArrayList::new));
            input.additionalCopyResources().forEach((resource) -> copyResourceCodes.add(doubleQuote(resource)));
            map.put("dependencyCodes", dependencyCodes);
            map.put("copyResourceCodes", copyResourceCodes);

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

        return Output.builder()
            .from(input)
            .parser(parserOutput)
            .styler(stylerOutput)
            .strategoRuntime(strategoRuntimeOutput)
            .constraintAnalyzer(constraintAnalyzerOutput)
            .build();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends LanguageProjectData.Input.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Shared shared();

        Parser.Input parser();

        Optional<Styler.Input> styler();

        Optional<StrategoRuntime.Input> strategoRuntime();

        Optional<ConstraintAnalyzer.Input> constraintAnalyzer();


        @Value.Default default ResourcePath buildGradleKtsFile() {
            return shared().languageProject().baseDirectory().appendRelativePath("build.gradle.kts");
        }

        @Value.Default default boolean standaloneProject() {
            return false;
        }

        @Value.Default @SuppressWarnings("immutables:untype") default Optional<ResourcePath> settingsGradleKtsFile() {
            if(standaloneProject()) {
                return Optional.of(shared().languageProject().baseDirectory().appendRelativePath("settings.gradle.kts"));
            } else {
                return Optional.empty();
            }
        }


        GradleDependency languageSpecificationDependency();

        List<String> additionalCopyResources();
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends LanguageProjectData.Output.Builder {
            public Builder from(Input input) {
                final GradleProject languageProject = input.shared().languageProject();
                final ResourcePath baseDirectory = languageProject.baseDirectory();
                return this
                    .baseDirectory(languageProject.baseDirectory())
                    .buildGradleKtsFile(input.buildGradleKtsFile())
                    .settingsGradleKtsFile(input.settingsGradleKtsFile())
                    ;
            }
        }

        static Builder builder() {
            return new Builder();
        }


        ResourcePath baseDirectory();

        ResourcePath buildGradleKtsFile();

        Optional<ResourcePath> settingsGradleKtsFile();


        Parser.LanguageProjectOutput parser();

        Optional<Styler.LanguageProjectOutput> styler();

        Optional<StrategoRuntime.LanguageProjectOutput> strategoRuntime();

        Optional<ConstraintAnalyzer.LanguageProjectOutput> constraintAnalyzer();
    }
}
