package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class ReferenceResolutionAdapterCompiler implements TaskDef<ReferenceResolutionAdapterCompiler.Input, None> {
    private final TemplateWriter resolveTaskDefTemplate;

    @Inject public ReferenceResolutionAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());

        this.resolveTaskDefTemplate = templateCompiler.getOrCompileToWriter("editor_services/ResolveTaskDef.java.mustache");
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();

        // only generate resolve if we have a strategy
        resolveTaskDefTemplate.write(context, input.resolveTaskDef().file(generatedJavaSourcesDirectory), input);

        return None.instance;
    }

    @Override public Serializable key(Input input) {
        return input.adapterProject().project().baseDirectory();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends ReferenceResolutionAdapterCompilerData.Input.Builder {
        }

        static Builder builder() {
            return new Builder();
        }

        /// Configuration
        String resolveStrategy();


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Adapter project classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Resolve task definitions

        @Value.Default default TypeInfo baseResolveTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Resolve");
        }

        Optional<TypeInfo> extendResolveTaskDef();

        default TypeInfo resolveTaskDef() {
            return extendResolveTaskDef().orElseGet(this::baseResolveTaskDef);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                resolveTaskDef().file(generatedJavaSourcesDirectory)
            );
        }

        /// Automatically computed values

        @Value.Derived default boolean isMultiFile() {
            return constraintAnalyzerInput().languageProjectInput().multiFile();
        }

        @Value.Derived default TypeInfo analyzeTaskDef() {
            if (this.isMultiFile()) {
                return constraintAnalyzerInput().analyzeMultiTaskDef();
            } else {
                return constraintAnalyzerInput().analyzeTaskDef();
            }
        }

        /// Automatically provided sub-inputs

        @Value.Auxiliary Shared shared();

        AdapterProject adapterProject();

        StrategoRuntimeAdapterCompiler.Input strategoRuntimeInput();

        ParserAdapterCompiler.Input parseInput();

        ConstraintAnalyzerAdapterCompiler.Input constraintAnalyzerInput();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();
    }
}
