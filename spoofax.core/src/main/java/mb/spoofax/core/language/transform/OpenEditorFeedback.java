package mb.spoofax.core.language.transform;

import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class OpenEditorFeedback implements TransformFeedback {
    private final ResourceKey file;
    private final @Nullable Region region;

    public OpenEditorFeedback(ResourceKey file, @Nullable Region region) {
        this.file = file;
        this.region = region;
    }

    public ResourceKey getFile() {
        return file;
    }

    public @Nullable Region getRegion() {
        return region;
    }

    @Override public void accept(TransformFeedbackVisitor visitor) {
        visitor.openEditor(file, region);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final OpenEditorFeedback that = (OpenEditorFeedback) o;
        return file.equals(that.file) && Objects.equals(region, that.region);
    }

    @Override public int hashCode() {
        return Objects.hash(file, region);
    }

    @Override public String toString() {
        return file.toString() + (region != null ? "@" + region.toString() : "");
    }
}
