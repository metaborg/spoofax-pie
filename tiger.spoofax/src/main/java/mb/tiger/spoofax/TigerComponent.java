package mb.tiger.spoofax;

import dagger.Component;
import mb.pie.dagger.TaskDefsComponent;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.tiger.spoofax.taskdef.AstTaskDef;
import mb.tiger.spoofax.taskdef.MessagesTaskDef;
import mb.tiger.spoofax.taskdef.StylingTaskDef;
import mb.tiger.spoofax.taskdef.TokenizerTaskDef;

/**
 * A {@link LanguageComponent} that contributes Tiger task definitions, and also contributes them as
 * {@link TaskDefsComponent}. All objects are provided by a {@link TigerModule}, which may inject objects from a
 * {@link PlatformComponent}. Task definitions are overridden to concrete types so that Dagger can use constructor
 * injection on those types.
 */
@LanguageScope @Component(modules = TigerModule.class, dependencies = PlatformComponent.class)
public interface TigerComponent extends LanguageComponent, TaskDefsComponent {
    @Override MessagesTaskDef getMessagesTaskDef();

    @Override AstTaskDef getAstTaskDef();

    @Override TokenizerTaskDef getTokenizerTaskDef();

    @Override StylingTaskDef getStylingTaskDef();
}
