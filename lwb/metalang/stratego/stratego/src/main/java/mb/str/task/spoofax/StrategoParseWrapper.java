package mb.str.task.spoofax;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.jsglr1.pie.JSGLR1ParseTaskInput;
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
    public Result<JSGLR1ParseOutput, JSGLR1ParseException> exec(ExecContext context, JSGLR1ParseTaskInput input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
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
                e -> Result.ofErr(JSGLR1ParseException.otherFail(e, input.startSymbol(), input.fileHint().toOptional(), input.rootDirectoryHint())) // Stratego configuration failed -> fail.
            ),
            () -> super.exec(context, input) // No directory hint is given, cannot get configuration -> parse normally.
        );
    }
}
