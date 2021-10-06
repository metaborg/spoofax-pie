package mb.str.task.spoofax;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.str.StrategoClassLoaderResources;
import mb.str.StrategoParser;
import mb.str.StrategoScope;
import mb.str.task.StrategoParse;

import javax.inject.Inject;
import javax.inject.Provider;

@StrategoScope
public class StrategoParseWrapper extends StrategoParse {
    private final StrategoClassLoaderResources classLoaderResources;
    private final StrategoAnalyzeConfigFunctionWrapper configFunctionWrapper;

    @Inject public StrategoParseWrapper(
        StrategoClassLoaderResources classLoaderResources,
        Provider<StrategoParser> parserProvider,
        StrategoAnalyzeConfigFunctionWrapper configFunctionWrapper
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
        // TODO: instead of requiring all origins for each file to parse, only require the origins that corresponds to a certain file.
        return Option.ofOptional(input.rootDirectoryHint()).mapThrowingOrElseThrowing(
            d -> configFunctionWrapper.get().apply(context, d).mapThrowingOrElse(
                o -> o.mapThrowingOrElseThrowing(
                    c -> {
                        c.sourceFileOrigins.forEach(context::require);
                        return super.exec(context, input); // Parse normally after requiring source file origins.
                    },
                    () -> super.exec(context, input) // Stratego is not configured -> parse normally.
                ),
                e -> Result.ofErr(JsglrParseException.otherFail(e, Option.ofOptional(input.startSymbol()), input.fileHint(), Option.ofOptional(input.rootDirectoryHint()))) // Stratego configuration failed -> fail.
            ),
            () -> super.exec(context, input) // No directory hint is given, cannot get configuration -> parse normally.
        );
    }
}
