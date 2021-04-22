package mb.sdf3.task.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.spec.Sdf3CheckSpec;
import mb.sdf3.task.spec.Sdf3SpecConfig;

import javax.inject.Inject;
import java.io.IOException;

@Sdf3Scope
public class Sdf3CheckMultiWrapper implements TaskDef<ResourcePath, KeyedMessages> {
    private final Sdf3ClassLoaderResources classLoaderResources;
    private final Sdf3SpecConfigFunctionWrapper configFunctionWrapper;
    private final Sdf3CheckSpec checkSpec;
    private final BaseSdf3CheckMulti baseCheckMulti;


    @Inject public Sdf3CheckMultiWrapper(
        Sdf3ClassLoaderResources classLoaderResources,
        Sdf3SpecConfigFunctionWrapper configFunctionWrapper,
        Sdf3CheckSpec checkSpec,
        BaseSdf3CheckMulti baseCheckMulti
    ) {
        this.classLoaderResources = classLoaderResources;
        this.configFunctionWrapper = configFunctionWrapper;
        this.checkSpec = checkSpec;
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
                KeyedMessages::of // SDF3 is not configured, do not need to check.
            ),
            e -> KeyedMessages.of(ListView.of(new Message("Cannot check SDF3 files; reading configuration failed unexpectedly", e)), rootDirectory)
        );
    }

    private KeyedMessages checkWithConfig(ExecContext context, Sdf3SpecConfig config) {
        return context.require(checkSpec, config);
    }

    private KeyedMessages checkDefault(ExecContext context, ResourcePath rootDirectory) {
        return context.require(baseCheckMulti, rootDirectory);
    }
}
