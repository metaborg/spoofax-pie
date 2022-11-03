package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

/**
 * Compiles the reference retention task.
 */
@Value.Enclosing
public class ReferenceRetentionAdapterCompiler {
    private final TemplateWriter referenceRetentionTaskDefTemplate;

    /**
     * Initializes a new instance of the {@link ReferenceRetentionAdapterCompiler} class.
     *
     * @param templateCompiler the template compiler to use
     */
    @Inject public ReferenceRetentionAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.referenceRetentionTaskDefTemplate = templateCompiler.getOrCompileToWriter("reference_retention/ReferenceRetentionTaskDef.java.mustache");
    }

    public None compile(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();
        // TODO: Enable
        referenceRetentionTaskDefTemplate.write(context, input.baseReferenceRetentionTaskDef().file(generatedJavaSourcesDirectory), input);
        return None.instance;
    }

    public ListView<GradleConfiguredDependency> getDependencies(ReferenceRetentionAdapterCompiler.Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().atermCommonDep()),
            GradleConfiguredDependency.api(input.shared().jsglrPieDep()),
            GradleConfiguredDependency.api(input.shared().constraintPieDep()),
            GradleConfiguredDependency.api(input.shared().statixPieDep()),
            GradleConfiguredDependency.api(input.shared().statixReferenceRetentionPieDep())
        );
    }

    @Value.Immutable public interface Input extends Serializable {
        class Builder extends ReferenceRetentionAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Reference retention task definition

        @Value.Default default TypeInfo baseReferenceRetentionTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "ReferenceRetentionTaskDef");
        }

        Optional<TypeInfo> extendReferenceRetentionTaskDef();

        default TypeInfo referenceRetentionTaskDef() {
            return extendReferenceRetentionTaskDef().orElseGet(this::baseReferenceRetentionTaskDef);
        }

        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                baseReferenceRetentionTaskDef().file(generatedJavaSourcesDirectory)
            );
        }


        /// Automatically provided sub-inputs.

        Shared shared();

        AdapterProject adapterProject();

        ParserAdapterCompiler.Input parserInput();

        ConstraintAnalyzerAdapterCompiler.Input constraintAnalyzerInput();

        StrategoRuntimeAdapterCompiler.Input strategoRuntimeInput();

        TegoRuntimeAdapterCompiler.Input tegoRuntimeInput();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();

        @Value.Check default void check() {

        }
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends ReferenceRetentionAdapterCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
