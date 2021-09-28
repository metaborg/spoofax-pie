package mb.spoofax.core.language.taskdef;

import mb.common.codecompletion.CodeCompletionResult;
import mb.common.region.Region;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Objects;

public class NoneCodeCompletionTaskDef extends NoneTaskDef<NoneCodeCompletionTaskDef.Args, CodeCompletionResult> {
    public static class Args implements Serializable {
        /** The primary selection at which to complete. */
        public final Region primarySelection;
        /** The file being completed. */
        public final ResourceKey file;
        /** The root directory of the project; or {@code null} when not specified. */
        public final @Nullable ResourcePath rootDirectoryHint;

        /**
         * Initializes a new instance of the {@link NoneCodeCompletionTaskDef.Args} class.
         *
         * @param primarySelection the primary selection at which completion is invoked
         * @param file      the key of the resource in which completion is invoked
         * @param rootDirectoryHint the root directory of the project; or {@code null} when not specified
         */
        public Args(Region primarySelection, ResourceKey file, @Nullable ResourcePath rootDirectoryHint) {
            this.primarySelection = primarySelection;
            this.file = file;
            this.rootDirectoryHint = rootDirectoryHint;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            return equals((NoneCodeCompletionTaskDef.Args)o);
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
        protected boolean equals(NoneCodeCompletionTaskDef.Args that) {
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

    @Inject public NoneCodeCompletionTaskDef(@Named("packageId") String packageId) { super(packageId); }
}
