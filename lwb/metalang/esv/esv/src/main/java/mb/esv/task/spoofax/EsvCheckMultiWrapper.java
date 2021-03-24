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
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

@EsvScope
public class EsvCheckMultiWrapper implements TaskDef<EsvCheckMultiWrapper.Input, KeyedMessages> {
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
            return root.equals(input.root) && walker.equals(input.walker) && matcher.equals(input.matcher);
        }

        @Override public int hashCode() {
            return Objects.hash(root, walker, matcher);
        }

        @Override public String toString() {
            return "Input{" +
                "root=" + root +
                ", walker=" + walker +
                ", matcher=" + matcher +
                '}';
        }
    }

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
        return "mb.esv.task.EsvCheckMulti";
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) throws IOException {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        return configFunctionWrapper.get().apply(context, input.root).mapOrElse(
            o -> o.mapOrElse(
                c -> checkWithConfig(context, c),
                KeyedMessages::of // ESV is not configured, do not need to check.
            ),
            e -> KeyedMessages.of(ListView.of(new Message("Cannot check ESV files; reading configuration failed unexpectedly", e)), input.root)
        );
    }

    private KeyedMessages checkWithConfig(ExecContext context, EsvConfig config) {
        return context.require(check, config);
    }

    private KeyedMessages checkDefault(ExecContext context, EsvCheckMultiWrapper.Input input) {
        return context.require(baseCheckMulti, new BaseEsvCheckMulti.Input(input.root, input.walker, input.matcher));
    }
}
