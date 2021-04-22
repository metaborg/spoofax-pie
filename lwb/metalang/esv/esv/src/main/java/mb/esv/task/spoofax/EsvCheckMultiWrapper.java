package mb.esv.task.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.esv.EsvClassLoaderResources;
import mb.esv.EsvScope;
import mb.esv.task.EsvCheck;
import mb.esv.task.EsvConfig;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;

import javax.inject.Inject;
import java.io.IOException;

@EsvScope
public class EsvCheckMultiWrapper implements TaskDef<ResourcePath, KeyedMessages> {
    private final EsvClassLoaderResources classLoaderResources;
    private final EsvConfigFunctionWrapper configFunctionWrapper;
    private final EsvCheck check;
    private final BaseEsvCheckMulti baseCheckMulti;

    @Inject public EsvCheckMultiWrapper(
        EsvClassLoaderResources classLoaderResources,
        EsvConfigFunctionWrapper configFunctionWrapper,
        EsvCheck check,
        BaseEsvCheckMulti baseCheckMulti
    ) {
        this.classLoaderResources = classLoaderResources;
        this.configFunctionWrapper = configFunctionWrapper;
        this.check = check;
        this.baseCheckMulti = baseCheckMulti;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        return configFunctionWrapper.get().apply(context, rootDirectory).mapOrElse(
            o -> o.mapOrElse(
                c -> checkWithConfig(context, c),
                () -> checkDefault(context, rootDirectory)
            ),
            e -> KeyedMessages.of(ListView.of(new Message("Cannot check ESV files; reading configuration failed unexpectedly", e)), rootDirectory)
        );
    }

    private KeyedMessages checkWithConfig(ExecContext context, EsvConfig config) {
        return context.require(check, config);
    }

    private KeyedMessages checkDefault(ExecContext context, ResourcePath rootDirectory) {
        return context.require(baseCheckMulti, rootDirectory);
    }
}
