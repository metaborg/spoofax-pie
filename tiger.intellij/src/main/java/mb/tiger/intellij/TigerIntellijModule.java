package mb.tiger.intellij;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.tree.IFileElementType;
import dagger.Module;
import dagger.Provides;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.editor.SpoofaxLexer;
import mb.spoofax.intellij.psi.SpoofaxAstBuilder;
import mb.spoofax.intellij.psi.SpoofaxFileElementType;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;


@Module
public class TigerIntellijModule {

    @Provides @LanguageScope
    Language provideLanguage() { return TigerLanguage.INSTANCE; }

    @Provides @LanguageScope
    LanguageFileType provideLanguageFileType() { return TigerFileType.INSTANCE; }

    @Provides @LanguageScope
    IFileElementType provideFileElementType(SpoofaxFileElementType fileElementType) { return fileElementType; }

}
