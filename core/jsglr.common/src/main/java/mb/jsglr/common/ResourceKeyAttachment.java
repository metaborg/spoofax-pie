package mb.jsglr.common;

import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.terms.attachments.AbstractTermAttachment;
import org.spoofax.terms.attachments.ParentAttachment;
import org.spoofax.terms.attachments.TermAttachmentType;
import org.spoofax.terms.attachments.VolatileTermAttachmentType;

public class ResourceKeyAttachment extends AbstractTermAttachment {
    private static final long serialVersionUID = 1;
    public static final TermAttachmentType<ResourceKeyAttachment> TYPE =
        new VolatileTermAttachmentType<>(ResourceKeyAttachment.class);

    public final ResourceKey resourceKey;


    public ResourceKeyAttachment(ResourceKey resourceKey) {
        this.resourceKey = resourceKey;
    }


    /**
     * Gets the resource key for given {@code term}, or {@code null} if none exists.
     */
    public static @Nullable ResourceKey getResourceKey(ISimpleTerm term) {
        final @Nullable ISimpleTerm root = ParentAttachment.getRoot(term);
        if(root == null) {
            return null;
        }
        final @Nullable ResourceKeyAttachment attachment = root.getAttachment(TYPE);
        if(attachment == null) {
            return null;
        }
        return attachment.resourceKey;
    }

    /**
     * Sets the resource key for given root {@code term}.
     *
     * @throws RuntimeException When {@code term} is not the root of the tree.
     */
    public static void setResourceKey(ISimpleTerm term, ResourceKey resourceKey) {
        final @Nullable ISimpleTerm root = ParentAttachment.getRoot(term);
        if(term != root) {
            throw new RuntimeException(
                "Attempted to set resource key for term " + term + " to " + resourceKey + ", but the term is not the root term. Resource key attachments are only supported for the root term");
        }
        root.putAttachment(new ResourceKeyAttachment(resourceKey));
    }


    @Override public TermAttachmentType<ResourceKeyAttachment> getAttachmentType() {
        return TYPE;
    }
}
