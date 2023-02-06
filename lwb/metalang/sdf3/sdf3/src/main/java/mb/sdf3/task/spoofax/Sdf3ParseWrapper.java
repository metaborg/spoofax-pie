package mb.sdf3.task.spoofax;

import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3Parser;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3Parse;

import javax.inject.Inject;
import javax.inject.Provider;

@Sdf3Scope
public class Sdf3ParseWrapper extends Sdf3Parse {
    private final Sdf3ClassLoaderResources classLoaderResources;
    private final Sdf3SpecConfigFunctionWrapper configFunctionWrapper;

    @Inject public Sdf3ParseWrapper(
        Sdf3ClassLoaderResources classLoaderResources,
        Provider<Sdf3Parser> parserProvider,
        Sdf3SpecConfigFunctionWrapper configFunctionWrapper
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
