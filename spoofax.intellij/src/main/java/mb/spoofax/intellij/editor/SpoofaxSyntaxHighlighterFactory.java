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

/**
 * IntelliJ requires its own factory implementation for {@link SpoofaxSyntaxHighlighter}.
 */
public abstract class SpoofaxSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    private final IntellijResourceRegistry resourceRegistry;
    private final SpoofaxLexer.Factory lexerFactory;
    private final SpoofaxSyntaxHighlighter.Factory highlighterFactory;

    protected SpoofaxSyntaxHighlighterFactory(
        IntellijResourceRegistry resourceRegistry,
        SpoofaxLexer.Factory lexerBuilder,
        SpoofaxSyntaxHighlighter.Factory highlighterBuilder
    ) {
        this.resourceRegistry = resourceRegistry;
        this.lexerFactory = lexerBuilder;
        this.highlighterFactory = highlighterBuilder;
    }

    @Override
    public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile file) {
        if (file == null) {
            throw new RuntimeException("Cannot get syntax highlighter; file is null");
        }
        final Resource resource = resourceRegistry.getResource(file);
        final Lexer lexer = lexerFactory.create(resource.getKey());
        return highlighterFactory.create(lexer);
    }
}
