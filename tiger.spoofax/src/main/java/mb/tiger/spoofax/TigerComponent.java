package mb.tiger.spoofax;

import dagger.Component;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.tiger.spoofax.taskdef.AstTaskDef;
import mb.tiger.spoofax.taskdef.MessagesTaskDef;
import mb.tiger.spoofax.taskdef.StylingTaskDef;
import mb.tiger.spoofax.taskdef.TokenizerTaskDef;

@LanguageScope @Component(modules = TigerModule.class, dependencies = PlatformComponent.class)
public interface TigerComponent extends LanguageComponent {
    @Override MessagesTaskDef messagesTaskDef();

    @Override AstTaskDef astTaskDef();

    @Override TokenizerTaskDef tokenizerTaskDef();

    @Override StylingTaskDef stylingTaskDef();
}
