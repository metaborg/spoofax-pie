package mb.spoofax.compiler.adapter;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import mb.spoofax.compiler.util.Shared;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

/**
 * Tego runtime adapter compiler.
 */
@Value.Enclosing
public class TegoRuntimeAdapterCompiler {

    @Inject public TegoRuntimeAdapterCompiler() { }


    public None compile(ExecContext context, Input input) throws IOException {
        return None.instance; // Nothing to generate: return.
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.shared().tegoRuntimeDep())
        );
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends TegoRuntimeAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Kinds of classes (generated/extended/manual)

        @Value.Default default ClassKind classKind() { return ClassKind.Generated; }


        /// Adapter project classes

        default ResourcePath generatedJavaSourcesDirectory() {
            return adapterProject().generatedJavaSourcesDirectory();
        }


        /// Files information, known up-front for build systems with static dependencies such as Gradle.

        default ListView<ResourcePath> javaSourceFiles() {
            return ListView.of();
        }


        /// Automatically provided sub-inputs

        @Value.Auxiliary Shared shared();

        AdapterProject adapterProject();

        ClassLoaderResourcesCompiler.Input classLoaderResourcesInput();
    }
}
