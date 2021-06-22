package mb.spoofax.core.resource;

import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.resource.ValueResourceStamp;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

public class TextResourceStamper implements ResourceStamper<TextResource> {
    @Override public ResourceStamp<TextResource> stamp(TextResource resource) throws IOException {
        return new ValueResourceStamp<>(resource.getText(), this);
    }

    @Override public boolean equals(@Nullable Object o) {
        return this == o || o != null && this.getClass() == o.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "TextResourceStamper()";
    }
}
