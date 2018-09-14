package mb.spoofax.runtime.jsglr;

import mb.pie.vfs.path.PPath;
import mb.pie.vfs.path.PPathImpl;
import mb.spoofax.api.SpoofaxRunEx;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.*;

import javax.annotation.Nullable;
import java.nio.file.Paths;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;
import static org.spoofax.terms.attachments.OriginAttachment.getOrigin;

/**
 * A tree-wide source resource and parse controller attachment.
 * <p>
 * Uses {@link ParentAttachment} to identify the root of a tree, where this attachment is stored.
 */
public class SourcePathAttachment extends AbstractTermAttachment {
    private static final long serialVersionUID = 1;
    public static final TermAttachmentType<SourcePathAttachment> TYPE = new VolatileTermAttachmentType<>(
        SourcePathAttachment.class);

    public final PPath path;


    public SourcePathAttachment(PPath path) {
        this.path = path;
    }


    @Override public TermAttachmentType<SourcePathAttachment> getAttachmentType() {
        return TYPE;
    }


    public static @Nullable PPath getPathForTerm(ISimpleTerm term) {
        final SourcePathAttachment attachment = ParentAttachment.getRoot(term).getAttachment(TYPE);
        if(attachment != null) {
            return attachment.path;
        }

        while(!hasImploderOrigin(term) && term.getSubtermCount() > 0) {
            term = term.getSubterm(0);
        }

        if(term.getAttachment(ImploderAttachment.TYPE) == null) {
            term = getOrigin(term);
        }
        if(term == null || term.getAttachment(ImploderAttachment.TYPE) == null) {
            return null;
        }

        final String fileName = ImploderAttachment.getFilename(term);
        if(fileName == null) {
            return null;
        }

        return new PPathImpl(Paths.get(fileName));
    }

    /**
     * Sets the resource for a term tree. Should only be applied to the root of a tree.
     */
    public static void setPathForTerm(ISimpleTerm term, PPath path) {
        final ISimpleTerm root = ParentAttachment.getRoot(term);
        if(term != root) {
            throw new SpoofaxRunEx(
                "Attempted to set source path for term " + term + " to " + path + ", but the term is not the root term. Source path attachments are only supported for the root term");
        }
        root.putAttachment(new SourcePathAttachment(path));
    }
}
