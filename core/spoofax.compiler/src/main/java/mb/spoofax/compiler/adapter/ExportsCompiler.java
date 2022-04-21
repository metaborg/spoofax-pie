package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.Named;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.StringUtil;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.Export;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Value.Enclosing
public class ExportsCompiler {
    private final TemplateWriter resourceExportsTemplate;

    @Inject public ExportsCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.resourceExportsTemplate = templateCompiler.getOrCompileToWriter("exports/ResourceExports.java.mustache");
    }


    public None compile(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        { // Exports
            final HashMap<String, CombinedExport> combinedExports = new HashMap<>();
            for(Named<Export> named : input.exports()) {
                final CombinedExport combinedExport = combinedExports.computeIfAbsent(named.name(), CombinedExport::new);
                combinedExport.exports.add(named.value().caseOf()
                    .file(relativePath -> "Export.file(\"" + relativePath + "\")")
                    .directory(relativePath -> "Export.directory(\"" + relativePath + "\")")
                );
            }
            final HashMap<String, Object> map = new HashMap<>();
            map.put("combinedExports", combinedExports.values());
            resourceExportsTemplate.write(context, input.baseResourceExports().file(generatedJavaSourcesDirectory), input, map);
        }
        return None.instance;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends ExportsCompilerData.Input.Builder {
            public Builder addFileExport(String id, String relativePath) {
                return addExports(Named.of(id, Export.file(relativePath)));
            }

            public Builder addDirectoryExport(String id, String relativePath) {
                return addExports(Named.of(id, Export.directory(relativePath)));
            }
        }

        static Builder builder() {return new Builder();}


        /// Configuration

        List<Named<Export>> exports();


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {return ClassKind.Generated;}


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Resource exports

        @Value.Default default TypeInfo baseResourceExports() {
            return TypeInfo.of(adapterProject().packageId(), shared().defaultClassPrefix() + "ResourceExports");
        }

        Optional<TypeInfo> extendResourceExports();

        default TypeInfo resourceExportsClass() {
            return extendResourceExports().orElseGet(this::baseResourceExports);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseResourceExports().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs.

        Shared shared();

        AdapterProject adapterProject();
    }

    private static class CombinedExport {
        public final String id;
        public final String idUncapitalized;
        public final ArrayList<String> exports = new ArrayList<>();

        private CombinedExport(String id) {
            this.id = id;
            this.idUncapitalized = StringUtil.uncapitalizeFirstCharacter(id);
        }
    }
}
