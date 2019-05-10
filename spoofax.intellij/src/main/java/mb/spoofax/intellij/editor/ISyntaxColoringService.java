package mb.spoofax.intellij.editor;

import javax.annotation.Nullable;

/**
 * Colors (part of) a document.
 */
public interface ISyntaxColoringService {
    /**
     * Configures the service.
     */
    void configure(ISyntaxColoringConfiguration configuration);

    @Nullable
    ISyntaxColoringInfo getSyntaxColoringInfo(
            String document,
            Span span,
            @Nullable ICancellationToken cancellationToken);
}