package mb.spoofax.core.language;

import mb.common.message.Messages;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;


public interface LanguageInstance {
    Task<AstResult> createAstTask(ResourceKey resourceKey);

    Task<Messages> createMessagesTask(ResourceKey resourceKey);

    Task<@Nullable Styling> createStylingTask(ResourceKey resourceKey);

    Task<@Nullable ArrayList<Token>> createTokenizerTask(ResourceKey resourceKey);

    String getDisplayName();
}
