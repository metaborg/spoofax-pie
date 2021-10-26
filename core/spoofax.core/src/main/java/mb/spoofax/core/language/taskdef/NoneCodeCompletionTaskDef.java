package mb.spoofax.core.language.taskdef;

import mb.common.codecompletion.CodeCompletionResult;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Objects;

public class NoneCodeCompletionTaskDef implements TaskDef<NoneCodeCompletionTaskDef.Input, Result<CodeCompletionResult, ?>> {

    public static class Input implements Serializable {
        /** The primary selection at which to complete. */
        public final Region primarySelection;
        /** The file being completed. */
        public final ResourceKey file;
        /** The root directory of the project; or {@code null} when not specified. */
        public final @Nullable ResourcePath rootDirectoryHint;

        /**
         * Initializes a new instance of the {@link Input} class.
         *
         * @param primarySelection the primary selection at which completion is invoked
         * @param file      the key of the resource in which completion is invoked
         * @param rootDirectoryHint the root directory of the project; or {@code null} when not specified
         */
        public Input(Region primarySelection, ResourceKey file, @Nullable ResourcePath rootDirectoryHint) {
            this.primarySelection = primarySelection;
            this.file = file;
            this.rootDirectoryHint = rootDirectoryHint;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            return equals((Input)o);
        }

        /**
         * Determines whether this object is equal to the specified object.
         *
         * Note: this method does not check whether the type of the argument is exactly the same.
         *
         * @param that the object to compare to
         * @return {@code true} when this object is equal to the specified object;
         * otherwise, {@code false}
         */
        protected boolean equals(Input that) {
            if (this == that) return true;
            return this.primarySelection.equals(that.primarySelection)
                && this.file.equals(that.file)
                && Objects.equals(this.rootDirectoryHint, that.rootDirectoryHint);
        }

        @Override public int hashCode() {
            return Objects.hash(
                this.rootDirectoryHint,
                this.file,
                this.primarySelection
            );
        }

        @Override public String toString() {
            return "NoneCodeCompletionTaskDef.Args{" +
                "primarySelection=" + primarySelection + ", " +
                "rootDirectoryHint=" + rootDirectoryHint + ", " +
                "file=" + file +
                "}";
        }
    }
    private final String idPrefix;

    @Inject public NoneCodeCompletionTaskDef(@Named("packageId") String packageId) { this.idPrefix = packageId; }

    @Override
    public String getId() {
        return idPrefix + "-" + getClass().getName();
    }

    @Override
    public Result<CodeCompletionResult, ?> exec(ExecContext context, Input input) throws Exception {
        return Result.ofOk(CodeCompletionResult.getEmpty());
    }
}
