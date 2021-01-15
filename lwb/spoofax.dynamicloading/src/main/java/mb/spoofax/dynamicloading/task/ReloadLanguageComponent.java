package mb.spoofax.dynamicloading.task;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.OutTransient;
import mb.pie.api.OutTransientImpl;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.dynamicloading.DynamicLanguageComponent;

public class ReloadLanguageComponent implements TaskDef<None, OutTransient<LanguageComponent>> {
    private final DynamicLanguageComponent dynamicLanguageComponent;
    private final CompileToJavaClassFiles compiler;
    private final CompileToJavaClassFiles.Input compilerInput;

    public ReloadLanguageComponent(
        DynamicLanguageComponent dynamicLanguageComponent,
        CompileToJavaClassFiles compiler,
        CompileToJavaClassFiles.Input compilerInput
    ) {
        this.dynamicLanguageComponent = dynamicLanguageComponent;
        this.compiler = compiler;
        this.compilerInput = compilerInput;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public OutTransient<LanguageComponent> exec(ExecContext context, None input) throws Exception {
        final Result<CompileToJavaClassFiles.Output, CompileToJavaClassFiles.CompileException> result = context.require(compiler, compilerInput);
        // TODO: properly handle error
        final CompileToJavaClassFiles.Output output = result.unwrap();
        for(ResourcePath path : output.classPath()) {
            context.require(path, ResourceStampers.hashDirRec());
        }
        final LanguageComponent actualLanguageComponent = dynamicLanguageComponent.reload(ListView.of(output.classPath()));
        return new OutTransientImpl<>(actualLanguageComponent, true);
    }
}
