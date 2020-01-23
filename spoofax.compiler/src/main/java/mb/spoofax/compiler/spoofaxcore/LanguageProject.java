package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.StringUtil;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
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
    private final TemplateWriter buildGradleTemplate;
    private final TemplateWriter packageInfoTemplate;

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
        this.buildGradleTemplate = templateCompiler.getOrCompileToWriter("language_project/build.gradle.kts.mustache");
        this.packageInfoTemplate = templateCompiler.getOrCompileToWriter("language_project/package-info.Java.mustache");

        this.parserCompiler = parserCompiler;
        this.stylerCompiler = stylerCompiler;
        this.strategoRuntimeCompiler = strategoRuntimeCompiler;
        this.constraintAnalyzerCompiler = constraintAnalyzerCompiler;
    }

    public Output compile(Input input) throws IOException {
        final Shared shared = input.shared();

        // Class files
        final ResourcePath classesGenDirectory = input.classesGenDirectory();
        packageInfoTemplate.write(input, input.genPackageInfo().file(classesGenDirectory));

        // Files from other compilers.
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

        // Gradle files
        {
            final HashMap<String, Object> map = new HashMap<>();
            final String languageDependencyCode = input.languageSpecificationDependency().caseOf()
                .project((projectPath) -> "createProjectDependency(\"" + projectPath + "\")")
                .module((coordinate) -> "createModuleDependency(\"" + coordinate.toGradleNotation() + "\")")
                .files((filePaths) -> "createFilesDependency(" + filePaths.stream().map((s) -> "\"" + s + "\"").collect(Collectors.joining(", ")) + ")");
            map.put("languageDependencyCode", languageDependencyCode);
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


        /// Configuration

        GradleDependency languageSpecificationDependency();

        List<GradleConfiguredDependency> additionalDependencies();

        List<String> additionalCopyResources();


        /// Gradle files

        @Value.Default default ResourcePath buildGradleKtsFile() {
            return shared().languageProject().baseDirectory().appendRelativePath("build.gradle.kts");
        }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }

        default ResourcePath classesGenDirectory() {
            return shared().languageProject().genSourceSpoofaxJavaDirectory();
        }


        /// Language project classes

        // package-info

        @Value.Default default TypeInfo genPackageInfo() {
            return TypeInfo.of(shared().languagePackage(), "package-info");
        }

        Optional<TypeInfo> manualPackageInfo();

        default TypeInfo packageInfo() {
            if(classKind().isManual() && manualPackageInfo().isPresent()) {
                return manualPackageInfo().get();
            }
            return genPackageInfo();
        }


        // TODO: add check
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
