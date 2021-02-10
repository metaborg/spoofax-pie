package mb.spoofax.intellij.editor;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import mb.resource.Resource;
import mb.spoofax.intellij.IntellijLanguageComponent;
import mb.spoofax.intellij.SpoofaxPlugin;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * IntelliJ requires its own factory implementation for {@link SpoofaxSyntaxHighlighter}.
 */
public abstract class SpoofaxSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    private final IntellijResourceRegistry resourceRegistry;
    private final SpoofaxLexer.Factory lexerFactory;
    private final SpoofaxSyntaxHighlighter.Factory highlighterFactory;


    protected SpoofaxSyntaxHighlighterFactory(IntellijLanguageComponent languageComponent) {
        this.resourceRegistry = SpoofaxPlugin.getResourceServiceComponent().getResourceRegistry();
        this.lexerFactory = languageComponent.getLexerFactory();
        this.highlighterFactory = languageComponent.getHighlighterFactory();
    }


    @Override public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile file) {
        if(file == null) {
            throw new RuntimeException("Cannot get syntax highlighter; file is null");
        }
        final Resource resource = resourceRegistry.getResource(file);
        final Lexer lexer = lexerFactory.create(resource.getKey());
        return highlighterFactory.create(lexer);
    }
}
