package mb.str.task.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.str.StrategoClassLoaderResources;
import mb.str.StrategoScope;
import mb.str.config.StrategoAnalyzeConfig;
import mb.str.task.StrategoCheck;

import javax.inject.Inject;
import java.io.IOException;

@StrategoScope
public class StrategoCheckMultiWrapper implements TaskDef<ResourcePath, KeyedMessages> {
    private final StrategoClassLoaderResources classLoaderResources;
    private final StrategoAnalyzeConfigFunctionWrapper configFunctionWrapper;
    private final StrategoCheck check;
    private final BaseStrategoCheckMulti baseCheckMulti;

    @Inject public StrategoCheckMultiWrapper(
        StrategoClassLoaderResources classLoaderResources,
        StrategoAnalyzeConfigFunctionWrapper configFunctionWrapper,
        StrategoCheck check,
        BaseStrategoCheckMulti baseCheckMulti
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
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        return configFunctionWrapper.get().apply(context, rootDirectory).mapOrElse(
            o -> o.mapOrElse(
                c -> checkWithConfig(context, c),
                () -> checkDefault(context, rootDirectory)
            ),
            e -> KeyedMessages.of(ListView.of(new Message("Cannot check Stratego files; reading configuration failed unexpectedly", e)), rootDirectory)
        );
    }

    private KeyedMessages checkWithConfig(ExecContext context, StrategoAnalyzeConfig config) {
        return context.require(check, config);
    }

    private KeyedMessages checkDefault(ExecContext context, ResourcePath rootDirectory) {
        return context.require(baseCheckMulti, rootDirectory);
    }
}
