package mb.tiger.spoofax;

import mb.common.message.Messages;
import mb.common.style.Styling;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.AstResult;
import mb.spoofax.core.language.LanguageInstance;
import mb.tiger.spoofax.taskdef.AstTaskDef;
import mb.tiger.spoofax.taskdef.MessagesTaskDef;
import mb.tiger.spoofax.taskdef.StylingTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public class TigerInstance implements LanguageInstance {
    private final AstTaskDef astTaskDef;
    private final MessagesTaskDef messagesTaskDef;
    private final StylingTaskDef stylingTaskDef;


    @Inject
    public TigerInstance(AstTaskDef astTaskDef, MessagesTaskDef messagesTaskDef, StylingTaskDef stylingTaskDef) {
        this.astTaskDef = astTaskDef;
        this.messagesTaskDef = messagesTaskDef;
        this.stylingTaskDef = stylingTaskDef;
    }


    @Override public Task<AstResult> createAstTask(ResourceKey resourceKey) {
        return astTaskDef.createTask(resourceKey);
    }

    @Override public Task<Messages> createMessagesTask(ResourceKey resourceKey) {
        return messagesTaskDef.createTask(resourceKey);
    }

    @Override public Task<@Nullable Styling> createStylingTask(ResourceKey resourceKey) {
        return stylingTaskDef.createTask(resourceKey);
    }


    @Override public String getDisplayName() {
        return "Tiger";
    }
}
