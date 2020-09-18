package mb.spoofax.intellij;

import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.intellij.editor.SpoofaxLexer;
import mb.spoofax.intellij.editor.SpoofaxSyntaxHighlighter;

import javax.swing.*;

public interface IntellijLanguageComponent extends LanguageComponent {
    IntellijLanguage getLanguage();

    IntellijLanguageFileType getFileType();

    IntellijFileElementType getFileElementType();

    Icon getFileIcon();

    SpoofaxLexer.Factory getLexerFactory();

    SpoofaxSyntaxHighlighter.Factory getHighlighterFactory();
}
