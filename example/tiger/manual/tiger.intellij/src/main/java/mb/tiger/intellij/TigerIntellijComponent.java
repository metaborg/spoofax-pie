package mb.tiger.intellij;

import dagger.Component;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.intellij.IntellijLanguageComponent;
import mb.spoofax.intellij.IntellijPlatformComponent;
import mb.spoofax.intellij.log.IntellijLoggerComponent;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerModule;
import mb.tiger.spoofax.TigerResourcesComponent;
import mb.tiger.spoofax.TigerScope;

@TigerScope
@Component(
    modules = {
        TigerModule.class,
        TigerIntellijModule.class
    },
    dependencies = {
        IntellijLoggerComponent.class,
        TigerResourcesComponent.class,
        ResourceServiceComponent.class,
        IntellijPlatformComponent.class
    }
)
public interface TigerIntellijComponent extends IntellijLanguageComponent, TigerComponent {
    @Override TigerLanguage getLanguage();

    @Override TigerFileType getFileType();

    @Override TigerFileElementType getFileElementType();
}
