package mb.spoofax.intellij.editor;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;


public abstract class SpoofaxSyntaxHighlighterFactory extends SyntaxHighlighterFactory {

    private final IntelliJResourceManager resourceManager;
    private final SpoofaxLexer.IFactory lexerFactory;
    private final SpoofaxSyntaxHighlighter.IFactory highlighterFactory;

    @Inject
    public SpoofaxSyntaxHighlighterFactory(
            IntelliJResourceManager resourceManager,
            SpoofaxLexer.IFactory lexerFactory,
            SpoofaxSyntaxHighlighter.IFactory highlighterFactory
    ) {
        this.resourceManager = resourceManager;
        this.lexerFactory = lexerFactory;
        this.highlighterFactory = highlighterFactory;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
        String documentUri = this.resourceManager.getUri(virtualFile, project);
        Lexer lexer = this.lexerFactory.create(documentUri);
        return this.highlighterFactory.create(lexer);
    }

}
