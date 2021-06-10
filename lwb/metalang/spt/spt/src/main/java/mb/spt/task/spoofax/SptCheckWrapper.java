package mb.spt.task.spoofax;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spt.SptScope;
import mb.spt.task.SptCheck;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;

@SptScope
public class SptCheckWrapper implements TaskDef<SptCheckWrapper.Input, KeyedMessages> {
    public static class Input implements Serializable {
        public final ResourceKey file;
        public final @Nullable ResourcePath rootDirectoryHint;

        public Input(ResourceKey file, @Nullable ResourcePath rootDirectoryHint) {
            this.file = file;
            this.rootDirectoryHint = rootDirectoryHint;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!file.equals(input.file)) return false;
            return rootDirectoryHint != null ? rootDirectoryHint.equals(input.rootDirectoryHint) : input.rootDirectoryHint == null;
        }

        @Override public int hashCode() {
            int result = file.hashCode();
            result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
            return result;
        }

        @Override public String toString() {
            return "SptCheckWrapper$Input{" +
                "file=" + file +
                ", rootDirectoryHint=" + rootDirectoryHint +
                '}';
        }
    }


    private final SptCheck check;
    private final BaseSptCheck baseCheck;


    @Inject public SptCheckWrapper(SptCheck check, BaseSptCheck baseCheck) {
        this.check = check;
        this.baseCheck = baseCheck;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) throws Exception {
        // TODO: use check.
        return context.require(baseCheck, new BaseSptCheck.Input(input.file, input.rootDirectoryHint));
    }
}
