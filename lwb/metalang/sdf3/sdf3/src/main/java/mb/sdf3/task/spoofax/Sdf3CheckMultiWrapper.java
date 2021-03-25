package mb.sdf3.task.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.spec.Sdf3CheckSpec;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

@Sdf3Scope
public class Sdf3CheckMultiWrapper implements TaskDef<Sdf3CheckMultiWrapper.Input, KeyedMessages> {
    public static class Input implements Serializable {
        public final ResourcePath root;
        public final ResourceWalker walker;
        public final ResourceMatcher matcher;

        public Input(
            ResourcePath root,
            ResourceWalker walker,
            ResourceMatcher matcher
        ) {
            this.root = root;
            this.walker = walker;
            this.matcher = matcher;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!root.equals(input.root)) return false;
            if(!walker.equals(input.walker)) return false;
            return matcher.equals(input.matcher);
        }

        @Override public int hashCode() {
            int result = root.hashCode();
            result = 31 * result + walker.hashCode();
            result = 31 * result + matcher.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Input{" +
                "root=" + root +
                ", walker=" + walker +
                ", matcher=" + matcher +
                '}';
        }
    }


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

    @Override public KeyedMessages exec(ExecContext context, Input input) throws IOException {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        return configFunctionWrapper.get().apply(context, input.root).mapOrElse(
            o -> o.mapOrElse(
                c -> checkWithConfig(context, c),
                KeyedMessages::of // SDF3 is not configured, do not need to check.
            ),
            e -> KeyedMessages.of(ListView.of(new Message("Cannot check SDF3 files; reading configuration failed unexpectedly", e)), input.root)
        );
    }

    private KeyedMessages checkWithConfig(ExecContext context, Sdf3SpecConfig config) {
        return context.require(checkSpec, config);
    }

    private KeyedMessages checkDefault(ExecContext context, Sdf3CheckMultiWrapper.Input input) {
        return context.require(baseCheckMulti, new BaseSdf3CheckMulti.Input(input.root, input.walker, input.matcher));
    }
}
