package mb.statix.task.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;
import mb.statix.task.StatixCheck;
import mb.statix.task.StatixConfig;

import javax.inject.Inject;
import java.io.IOException;

@StatixScope
public class StatixCheckMultiWrapper implements TaskDef<ResourcePath, KeyedMessages> {
    private final StatixClassLoaderResources classLoaderResources;
    private final StatixConfigFunctionWrapper configFunctionWrapper;
    private final StatixCheck check;
    private final BaseStatixCheckMulti baseCheckMulti;

    @Inject public StatixCheckMultiWrapper(
        StatixClassLoaderResources classLoaderResources,
        StatixConfigFunctionWrapper configFunctionWrapper,
        StatixCheck check,
        BaseStatixCheckMulti baseCheckMulti
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
            e -> KeyedMessages.of(ListView.of(new Message("Cannot check Statix files; reading configuration failed unexpectedly", e)), rootDirectory)
        );
    }

    private KeyedMessages checkWithConfig(ExecContext context, StatixConfig config) {
        return context.require(check, config);
    }

    private KeyedMessages checkDefault(ExecContext context, ResourcePath rootDirectory) {
        return context.require(baseCheckMulti, rootDirectory);
    }
}
