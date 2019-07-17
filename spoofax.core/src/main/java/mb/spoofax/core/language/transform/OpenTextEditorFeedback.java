package mb.spoofax.core.language.transform;

import mb.common.region.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class OpenTextEditorFeedback implements TransformFeedback {
    private final String text;
    private final @Nullable Region region;


    public OpenTextEditorFeedback(String text, @Nullable Region region) {
        this.text = text;
        this.region = region;
    }

    public OpenTextEditorFeedback(String text) {
        this(text, null);
    }


    public String getText() {
        return text;
    }

    public @Nullable Region getRegion() {
        return region;
    }


    @Override public void accept(TransformFeedbackVisitor visitor) {
        visitor.openEditor(text, region);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final OpenTextEditorFeedback that = (OpenTextEditorFeedback) o;
        return text.equals(that.text) && Objects.equals(region, that.region);
    }

    @Override public int hashCode() {
        return Objects.hash(text, region);
    }

    @Override public String toString() {
        return text + (region != null ? "@" + region.toString() : "");
    }
}
