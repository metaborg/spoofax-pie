package mb.spoofax.core.language;

import mb.common.message.MessageCollection;
import mb.common.style.Styling;
import mb.pie.api.Task;
import mb.resource.ResourceKey;

public interface LanguageInstance {
    Task<AstResult> createAstTask(ResourceKey resourceKey);

    Task<MessageCollection> createMessagesTask(ResourceKey resourceKey);

    Task<Styling> createStylingTask(ResourceKey resourceKey);
}
