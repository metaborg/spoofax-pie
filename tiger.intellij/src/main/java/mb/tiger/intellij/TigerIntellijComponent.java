package mb.tiger.intellij;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.tree.IFileElementType;
import dagger.Component;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.intellij.SpoofaxIntellijComponent;
import mb.spoofax.intellij.editor.SpoofaxLexer;
import mb.spoofax.intellij.editor.SpoofaxSyntaxHighlighter;
import mb.spoofax.intellij.psi.SpoofaxTokenTypeManager;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerModule;


@LanguageScope
@Component(modules = {
        TigerModule.class,
        TigerIntellijModule.class
}, dependencies = SpoofaxIntellijComponent.class)
public interface TigerIntellijComponent extends TigerComponent {
    LanguageFileType getFileType();
    IFileElementType getFileElementType();
    SpoofaxTokenTypeManager getTokenTypeManager();


    SpoofaxLexer.Factory getLexerFactory();
    SpoofaxSyntaxHighlighter.Factory getHighlighterFactory();
}
