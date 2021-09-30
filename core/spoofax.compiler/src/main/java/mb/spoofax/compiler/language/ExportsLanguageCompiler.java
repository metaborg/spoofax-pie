package mb.spoofax.compiler.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.data.Export;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.StringUtil;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.compiler.util.UniqueNamer;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class ExportsLanguageCompiler {
    private final TemplateWriter exportsTemplate;

    @Inject public ExportsLanguageCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.exportsTemplate = templateCompiler.getOrCompileToWriter("exports/Exports.java.mustache");
    }


    public None compile(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        { // Exports
            final HashMap<String, CombinedExport> combinedExports = new HashMap<>();
            for(Export export : input.exports()) {
                final CombinedExport combinedExport = combinedExports.computeIfAbsent(export.languageId(), CombinedExport::new);
                combinedExport.relativePaths.add(export.relativePath());
            }
            final UniqueNamer uniqueNamer = new UniqueNamer();
            final HashMap<String, Object> map = new HashMap<>();
            map.put("combinedExports", combinedExports.values());
            exportsTemplate.write(context, input.baseExports().file(generatedJavaSourcesDirectory), input, map);
        }
        return None.instance;
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().completionsCommonDep())
        );
    }

    public ListView<String> getCopyResources(Input input) {
        return ListView.of();
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends ExportsLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Configuration

        List<Export> exports();


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return languageProject().generatedJavaSourcesDirectory();
        }

        // Completer

        @Value.Default default TypeInfo baseExports() {
            return TypeInfo.of(languageProject().packageId(), shared().defaultClassPrefix() + "Exports");
        }

        Optional<TypeInfo> extendExports();

        default TypeInfo exportsClass() {
            return extendExports().orElseGet(this::baseExports);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseExports().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs.

        Shared shared();

        LanguageProject languageProject();


        @Value.Check default void check() {

        }
    }

    private static class CombinedExport {
        public final String languageIdCapitalized;
        public final HashSet<String> relativePaths = new HashSet<>();

        private CombinedExport(String languageId) {
            this.languageIdCapitalized = StringUtil.capitalizeFirstCharacter(languageId);
        }
    }
}
