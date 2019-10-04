package mb.tiger.intellij;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.tree.IFileElementType;
import dagger.Module;
import dagger.Provides;
import mb.resource.ResourceRegistry;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.menu.EditorContextLanguageAction;
import mb.spoofax.intellij.menu.LanguageActionGroup;
import mb.spoofax.intellij.pie.PieRunner;
import mb.spoofax.intellij.psi.SpoofaxFileElementType;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import mb.tiger.intellij.menu.TigerEditorContextLanguageAction;
import mb.tiger.intellij.menu.TigerLanguageActionGroup;


@Module
public class TigerIntellijModule {
    @Provides @LanguageScope
    Language provideLanguage() { return TigerLanguage.instance; }

    @Provides @LanguageScope
    LanguageFileType provideLanguageFileType() { return TigerFileType.instance; }

    @Provides @LanguageScope
    IFileElementType provideFileElementType(SpoofaxFileElementType fileElementType) { return fileElementType; }

    @Provides
    LanguageActionGroup provideLanguageActionGroup() { return new TigerLanguageActionGroup(); }

    @Provides @LanguageScope
    EditorContextLanguageAction.Factory providesTigerEditorContextLanguageAction_Factory(IntellijResourceRegistry resourceRegistry, PieRunner pieRunner) {
        return new TigerEditorContextLanguageAction.Factory(resourceRegistry, pieRunner);
    }
}
