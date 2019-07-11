package mb.spoofax.core.language;

import mb.common.message.KeyedMessages;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Set;

public interface LanguageInstance {
    String getDisplayName();

    Set<String> getFileExtensions();


    Task<AstResult> createGetAstTask(ResourceKey resourceKey);

    Task<KeyedMessages> createCheckTask(ResourceKey resourceKey);

    Task<@Nullable Styling> createStyleTask(ResourceKey resourceKey);

    Task<@Nullable ArrayList<Token>> createTokenizeTask(ResourceKey resourceKey);
}
