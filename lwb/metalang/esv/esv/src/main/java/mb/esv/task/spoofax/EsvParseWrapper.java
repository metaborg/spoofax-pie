package mb.esv.task.spoofax;

import mb.common.result.Result;
import mb.esv.EsvClassLoaderResources;
import mb.esv.EsvParser;
import mb.esv.task.EsvParse;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.jsglr1.pie.JSGLR1ParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;

import javax.inject.Inject;
import javax.inject.Provider;

public class EsvParseWrapper extends EsvParse {
    private final EsvClassLoaderResources classLoaderResources;
    private final EsvConfigFunctionWrapper configFunctionWrapper;

    @Inject public EsvParseWrapper(
        EsvClassLoaderResources classLoaderResources,
        Provider<EsvParser> parserProvider,
        EsvConfigFunctionWrapper configFunctionWrapper
    ) {
        super(classLoaderResources, parserProvider);
        this.classLoaderResources = classLoaderResources;
        this.configFunctionWrapper = configFunctionWrapper;
    }

    @Override
    public Result<JSGLR1ParseOutput, JSGLR1ParseException> exec(ExecContext context, JSGLR1ParseTaskInput input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        input.rootDirectoryHint().ifPresent(d -> configFunctionWrapper.get().apply(context, d).ifOk(o -> o.ifSome(c -> c.sourceFileOrigins.forEach(context::require))));
        return super.exec(context, input);
    }
}
