package mb.spoofax.intellij;

import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.intellij.editor.ScopeManager;
import mb.spoofax.intellij.editor.SpoofaxLexerFactory;
import mb.spoofax.intellij.editor.SpoofaxSyntaxHighlighter;
import mb.spoofax.intellij.psi.SpoofaxTokenTypeManager;

import javax.swing.*;

public interface IntellijLanguageComponent extends LanguageComponent {
    IntellijLanguage getLanguage();

    IntellijLanguageFileType getFileType();

    IntellijFileElementType getFileElementType();

    Icon getFileIcon();

    SpoofaxLexerFactory getLexerFactory();

    SpoofaxSyntaxHighlighter.Factory getHighlighterFactory();

    ScopeManager getScopeManager();

    SpoofaxTokenTypeManager getTokenTypeManager();
}
