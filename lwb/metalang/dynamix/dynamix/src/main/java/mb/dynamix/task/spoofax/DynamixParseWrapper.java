package mb.dynamix.task.spoofax;

import mb.common.result.Result;
import mb.dynamix.DynamixClassLoaderResources;
import mb.dynamix.DynamixParser;
import mb.dynamix.DynamixScope;
import mb.dynamix.task.DynamixParse;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;

import javax.inject.Inject;
import javax.inject.Provider;

@DynamixScope
public class DynamixParseWrapper extends DynamixParse {
    private final DynamixClassLoaderResources classLoaderResources;
    private final DynamixConfigFunctionWrapper configFunctionWrapper;

    @Inject public DynamixParseWrapper(
        DynamixClassLoaderResources classLoaderResources,
        Provider<DynamixParser> parserProvider,
        DynamixConfigFunctionWrapper configFunctionWrapper
    ) {
        super(classLoaderResources, parserProvider);
        this.classLoaderResources = classLoaderResources;
        this.configFunctionWrapper = configFunctionWrapper;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<JsglrParseOutput, JsglrParseException> exec(ExecContext context, JsglrParseTaskInput input) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        // TODO: instead of requiring all origins for each file to parse, only require the origins that corresponds to a certain file?
        input.rootDirectoryHint().ifPresent(d -> configFunctionWrapper.get().apply(context, d).ifOk(o -> o.ifSome(c -> c.sourceFileOrigins.forEach((origin -> context.require(origin, OutputStampers.inconsequential()))))));
        return super.exec(context, input);
    }
}
