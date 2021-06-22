package mb.spoofax.core.resource;

import mb.common.text.Text;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ResourceTextSupplier implements Supplier<Text> {
    public final ResourceKey key;
    public final @Nullable ResourceStamper<ReadableResource> resourceStamper;
    public final String charsetName;
    private transient Charset charset;

    public ResourceTextSupplier(
        ResourceKey key,
        @Nullable ResourceStamper<ReadableResource> resourceStamper,
        Charset charset
    ) {
        this.key = key;
        this.resourceStamper = resourceStamper;
        this.charsetName = charset.name();
        this.charset = charset;
    }

    public ResourceTextSupplier(ResourceKey key, @Nullable ResourceStamper<ReadableResource> resourceStamper) {
        this(key, resourceStamper, StandardCharsets.UTF_8);
    }

    public ResourceTextSupplier(ResourceKey key, Charset charset) {
        this(key, null, charset);
    }

    public ResourceTextSupplier(ResourceKey key) {
        this(key, (ResourceStamper<ReadableResource>)null);
    }


    @Override public Text get(ExecContext context) {
        try {
            final ReadableResource resource = context.getReadableResource(key);
            if(resource instanceof TextResource) {
                final TextResource textResource = (TextResource)resource;
                context.require(textResource, new TextResourceStamper());
                return textResource.getText();
            } else {
                return Text.string(context.require(key, resourceStamper != null ? resourceStamper : context.getDefaultRequireReadableResourceStamper()).readString(charset));
            }
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ResourceTextSupplier that = (ResourceTextSupplier)o;
        return key.equals(that.key) &&
            Objects.equals(resourceStamper, that.resourceStamper) &&
            charset.equals(that.charset);
    }

    @Override public int hashCode() {
        return Objects.hash(key, resourceStamper, charset);
    }

    @Override public String toString() {
        return "ResourceTextSupplier{" +
            "key=" + key +
            ", stamper=" + resourceStamper +
            ", charset=" + charset +
            '}';
    }


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        this.charset = Charset.forName(charsetName);
    }
}
