package mb.esv.task.spoofax;

import mb.common.result.Result;
import mb.esv.EsvClassLoaderResources;
import mb.esv.EsvParser;
import mb.esv.task.EsvParse;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class EsvParseWrapper extends EsvParse {
    private final EsvClassLoaderResources classLoaderResources;
    private final EsvConfigFunctionWrapper configFunctionWrapper;

    @Inject public EsvParseWrapper(
        EsvClassLoaderResources classLoaderResources,
        Provider<EsvParser> parserProvider,
        @Named("completion") Provider<EsvParser> completionParserProvider,
        EsvConfigFunctionWrapper configFunctionWrapper
    ) {
        super(classLoaderResources, parserProvider, completionParserProvider);
        this.classLoaderResources = classLoaderResources;
        this.configFunctionWrapper = configFunctionWrapper;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<JsglrParseOutput, JsglrParseException> exec(ExecContext context, JsglrParseTaskInput input) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        // TODO: instead of requiring all origins for each file to parse, only require the origins that corresponds to a certain file.
        input.rootDirectoryHint().ifPresent(d -> configFunctionWrapper.get().apply(context, d).ifOk(o -> o.ifSome(c -> c.sourceFileOrigins.forEach(context::require))));
        return super.exec(context, input);
    }
}
