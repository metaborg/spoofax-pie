package mb.chars;

import mb.chars.task.CharsGetStrategoRuntimeProvider;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import java.io.IOException;

public class CharsRemoveA extends AstStrategoTransformTaskDef {
    private final CharsClassLoaderResources classloaderResources;

    @Inject
    public CharsRemoveA(
        CharsClassLoaderResources classloaderResources,
        CharsGetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        super(getStrategoRuntimeProvider, "remove-a");
        this.classloaderResources = classloaderResources;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override protected void createDependencies(ExecContext context) throws IOException {
        context.require(classloaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
    }
}
