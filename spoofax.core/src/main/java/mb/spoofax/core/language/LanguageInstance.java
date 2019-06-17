package mb.spoofax.core.language;

import mb.common.message.KeyedMessages;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;


public interface LanguageInstance {
    Task<AstResult> createGetAstTask(ResourceKey resourceKey);

    Task<KeyedMessages> createCheckTask(ResourceKey resourceKey);

    Task<@Nullable Styling> createStyleTask(ResourceKey resourceKey);

    Task<@Nullable ArrayList<Token>> createTokenizeTask(ResourceKey resourceKey);

    String getDisplayName();
}
