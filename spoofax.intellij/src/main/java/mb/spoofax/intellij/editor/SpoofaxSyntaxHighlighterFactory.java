package mb.spoofax.intellij.editor;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import mb.resource.Resource;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import org.jetbrains.annotations.Nullable;

public abstract class SpoofaxSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    private final LanguageComponent languageComponent;
    private final IntellijResourceRegistry resourceRegistry;
    private final SpoofaxLexer.Factory lexerFactory;
    private final SpoofaxSyntaxHighlighter.Factory highlighterFactory;

    public SpoofaxSyntaxHighlighterFactory(
        LanguageComponent languageComponent,
        IntellijResourceRegistry resourceRegistry,
        SpoofaxLexer.Factory lexerBuilder,
        SpoofaxSyntaxHighlighter.Factory highlighterBuilder
    ) {
        this.languageComponent = languageComponent;
        this.resourceRegistry = resourceRegistry;
        this.lexerFactory = lexerBuilder;
        this.highlighterFactory = highlighterBuilder;
    }

    @Override
    public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile file) {
        if(file == null) {
            throw new RuntimeException("Cannot get syntax highlighter; file is null");
        }
        final Resource resource = resourceRegistry.getResource(file);
        final Lexer lexer = lexerFactory.create(languageComponent, resource.getKey());
        return highlighterFactory.create(lexer);
    }
}
