package mb.tiger.intellij;

import dagger.Component;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.IntellijLanguageComponent;
import mb.spoofax.intellij.SpoofaxIntellijComponent;

@LanguageScope
@Component(modules = {TigerModule.class, TigerIntellijModule.class}, dependencies = SpoofaxIntellijComponent.class)
public interface TigerIntellijComponent extends IntellijLanguageComponent, TigerComponent {
    @Override TigerLanguage getLanguage();

    @Override TigerFileType getFileType();

    @Override TigerFileElementType getFileElementType();
}
