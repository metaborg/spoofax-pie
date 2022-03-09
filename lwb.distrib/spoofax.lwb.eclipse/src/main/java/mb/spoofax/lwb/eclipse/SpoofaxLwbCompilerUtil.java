package mb.spoofax.lwb.eclipse;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.SetView;
import mb.pie.api.OutTransient;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.definition.CompileLanguageDefinition;
import mb.spoofax.lwb.compiler.definition.CompileLanguageDefinitionException;
import mb.spoofax.lwb.dynamicloading.DynamicLoad;
import mb.spoofax.lwb.dynamicloading.DynamicLoadException;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponent;
import mb.spoofax.lwb.eclipse.util.ClassPathUtil;

public class SpoofaxLwbCompilerUtil {
    public static CompileLanguageDefinition.Args createCompileLanguageDefinitionArgs(ResourcePath rootDirectory) {
        return CompileLanguageDefinition.Args.builder()
            .rootDirectory(rootDirectory)
            .addJavaClassPathSuppliers(ClassPathUtil.getClassPathSupplier())
            .addJavaAnnotationProcessorPathSuppliers(ClassPathUtil.getClassPathSupplier())
            .build();

    }

    public static Task<Result<CompileLanguageDefinition.Output, CompileLanguageDefinitionException>> createCompileLanguageDefinitionTask(ResourcePath rootDirectory) {
        return SpoofaxLwbPlugin.getSpoofaxLwbCompilerComponent().getCompileLanguageDefinition().createTask(createCompileLanguageDefinitionArgs(rootDirectory));
    }


    public static Supplier<Result<DynamicLoad.SupplierOutput, ?>> dynamicLoadSupplierOutputSupplier(ResourcePath rootDirectory) {
        return createCompileLanguageDefinitionTask(rootDirectory).toSupplier().map(new ToDynamicLoadSupplierOutput());
    }

    public static Task<OutTransient<Result<DynamicComponent, DynamicLoadException>>> createDynamicLoadTask(ResourcePath rootDirectory) {
        return SpoofaxLwbPlugin.getDynamicLoadingComponent().getDynamicLoad().createTask(dynamicLoadSupplierOutputSupplier(rootDirectory));
    }

    public static class ToDynamicLoadSupplierOutput extends StatelessSerializableFunction<Result<CompileLanguageDefinition.Output, CompileLanguageDefinitionException>, Result<DynamicLoad.SupplierOutput, ?>> {
        @Override
        public Result<DynamicLoad.SupplierOutput, ?> apply(Result<CompileLanguageDefinition.Output, CompileLanguageDefinitionException> r) {
            return r.map(o -> new DynamicLoad.SupplierOutput(o.rootDirectory(), SetView.of(o.javaClassPaths()), o.participantClassQualifiedId()));
        }
    }


    public static Task<KeyedMessages> createCheckLanguageDefinitionTask(ResourcePath rootDirectory) {
        return SpoofaxLwbPlugin.getSpoofaxLwbCompilerComponent().getCheckLanguageDefinition().createTask(rootDirectory);
    }
}
