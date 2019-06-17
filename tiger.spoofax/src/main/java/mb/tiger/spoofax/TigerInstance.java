package mb.tiger.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.AstResult;
import mb.spoofax.core.language.LanguageInstance;
import mb.tiger.spoofax.taskdef.AstTaskDef;
import mb.tiger.spoofax.taskdef.MessagesTaskDef;
import mb.tiger.spoofax.taskdef.StylingTaskDef;
import mb.tiger.spoofax.taskdef.TokenizerTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;

public class TigerInstance implements LanguageInstance {
    private final AstTaskDef astTaskDef;
    private final MessagesTaskDef messagesTaskDef;
    private final StylingTaskDef stylingTaskDef;
    private final TokenizerTaskDef tokenizerTaskDef;


    @Inject public TigerInstance(
        AstTaskDef astTaskDef,
        MessagesTaskDef messagesTaskDef,
        TokenizerTaskDef tokenizerTaskDef,
        StylingTaskDef stylingTaskDef
    ) {
        this.astTaskDef = astTaskDef;
        this.messagesTaskDef = messagesTaskDef;
        this.tokenizerTaskDef = tokenizerTaskDef;
        this.stylingTaskDef = stylingTaskDef;
    }


    @Override public Task<AstResult> createAstTask(ResourceKey resourceKey) {
        return astTaskDef.createTask(resourceKey);
    }

    @Override public Task<KeyedMessages> createMessagesTask(ResourceKey resourceKey) {
        return messagesTaskDef.createTask(resourceKey);
    }

    @Override public Task<@Nullable Styling> createStylingTask(ResourceKey resourceKey) {
        return stylingTaskDef.createTask(resourceKey);
    }

    @Override public Task<@Nullable ArrayList<Token>> createTokenizerTask(ResourceKey resourceKey) {
        return tokenizerTaskDef.createTask(resourceKey);
    }


    @Override public String getDisplayName() {
        return "Tiger";
    }
}
