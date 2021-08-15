package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.util.*;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@Value.Enclosing
public class HoverAdapterCompiler implements TaskDef<HoverAdapterCompiler.Input, None> {
    private final TemplateWriter hoverTaskDefTemplate;

    @Inject public HoverAdapterCompiler(TemplateCompiler templateCompiler) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());

        this.hoverTaskDefTemplate = templateCompiler.getOrCompileToWriter("editor_services/HoverTaskDef.java.mustache");
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws IOException {
        if(input.classKind().isManual()) return None.instance; // Nothing to generate: return.
        final ResourcePath generatedJavaSourcesDirectory = input.generatedJavaSourcesDirectory();

        hoverTaskDefTemplate.write(context, input.hoverTaskDef().file(generatedJavaSourcesDirectory), input);

        return None.instance;
    }

    @Override public Serializable key(Input input) {
        return input.adapterProject().project().baseDirectory();
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends HoverAdapterCompilerData.Input.Builder {
        }

        static Builder builder() {
            return new Builder();
        }

        /// Configuration
        String hoverStrategy();


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() {
            return ClassKind.Generated;
        }


        /// Adapter project classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }

        // Resolve task definitions

        @Value.Default default TypeInfo baseHoverTaskDef() {
            return TypeInfo.of(adapterProject().taskPackageId(), shared().defaultClassPrefix() + "Hover");
        }

        Optional<TypeInfo> extendHoverTaskDef();

        default TypeInfo hoverTaskDef() {
            return extendHoverTaskDef().orElseGet(this::baseHoverTaskDef);
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            if(classKind().isManual()) {
                return ListView.of();
            }
            final ResourcePath generatedJavaSourcesDirectory = generatedJavaSourcesDirectory();
            return ListView.of(
                hoverTaskDef().file(generatedJavaSourcesDirectory)
            );
        }

        /// Automatically provided sub-inputs

        @Value.Auxiliary Shared shared();

        AdapterProject adapterProject();

        StrategoRuntimeAdapterCompiler.Input strategoRuntimeInput();

        ConstraintAnalyzerAdapterCompiler.Input constraintAnalyzerInput();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();
    }
}
