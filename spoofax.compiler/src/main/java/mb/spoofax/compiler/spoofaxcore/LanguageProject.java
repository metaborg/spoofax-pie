package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.GradleRepository;
import mb.spoofax.compiler.util.StringUtil;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Enclosing
public class LanguageProject {
    private final TemplateWriter settingsGradleTemplate;
    private final TemplateWriter buildGradleTemplate;

    private final Parser parserCompiler;
    private final Styler stylerCompiler;
    private final StrategoRuntime strategoRuntimeCompiler;
    private final ConstraintAnalyzer constraintAnalyzerCompiler;

    public LanguageProject(
        TemplateCompiler templateCompiler,
        Parser parserCompiler,
        Styler stylerCompiler,
        StrategoRuntime strategoRuntimeCompiler,
        ConstraintAnalyzer constraintAnalyzerCompiler
    ) {
        this.settingsGradleTemplate = templateCompiler.getOrCompileToWriter("gradle_project/settings.gradle.kts.mustache");
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("language_project/build.gradle.kts.mustache");

        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
    }

    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        final ArrayList<GradleRepository> repositories = new ArrayList<>(shared.defaultRepositories());

        final ArrayList<GradleConfiguredDependency> dependencies = new ArrayList<>(input.additionalDependencies());
        dependencies.add(GradleConfiguredDependency.api(shared.logApiDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.resourceDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.spoofaxCompilerInterfacesDep()));
        dependencies.add(GradleConfiguredDependency.api(shared.commonDep()));
        dependencies.add(GradleConfiguredDependency.compileOnly(shared.checkerFrameworkQualifiersDep()));

        final ArrayList<String> copyResources = new ArrayList<>(input.additionalCopyResources());

        final Parser.LanguageProjectOutput parserOutput = parserCompiler.compileLanguageProject(input.parser());
        dependencies.addAll(parserOutput.dependencies());
        copyResources.addAll(parserOutput.copyResources());

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

        try {
            input.settingsGradleKtsFile().ifPresent((f) -> {
                try {
                    settingsGradleTemplate.write(input, f);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
        {
            final HashMap<String, Object> map = new HashMap<>();
            final String languageDependencyCode = input.languageSpecificationDependency().caseOf()
                .project((projectPath) -> "createProjectDependency(\"" + projectPath + "\")")
                .module((coordinate) -> "createModuleDependency(\"" + coordinate.toGradleNotation() + "\")")
                .files((filePaths) -> "createFilesDependency(" + filePaths.stream().map((s) -> "\"" + s + "\"").collect(Collectors.joining(", ")) + ")");
            map.put("languageDependencyCode", languageDependencyCode);
            map.put("repositoryCodes", repositories.stream().map(GradleRepository::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
            map.put("dependencyCodes", dependencies.stream().map(GradleConfiguredDependency::toKotlinCode).collect(Collectors.toCollection(ArrayList::new)));
            map.put("copyResourceCodes", copyResources.stream().map(StringUtil::doubleQuote).collect(Collectors.toCollection(ArrayList::new)));
            buildGradleTemplate.write(input, map, input.buildGradleKtsFile());
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

        List<GradleConfiguredDependency> additionalDependencies();

        List<String> additionalCopyResources();
    }

    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends LanguageProjectData.Output.Builder {}

        static Builder builder() {
            return new Builder();
        }


        Parser.LanguageProjectOutput parser();

        Optional<Styler.LanguageProjectOutput> styler();

        Optional<StrategoRuntime.LanguageProjectOutput> strategoRuntime();

        Optional<ConstraintAnalyzer.LanguageProjectOutput> constraintAnalyzer();
    }
}
