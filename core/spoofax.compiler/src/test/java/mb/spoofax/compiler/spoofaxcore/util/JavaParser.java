package mb.spoofax.compiler.spoofaxcore.util;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class JavaParser {
    private final ASTParser parser;

    public JavaParser() {
        this.parser = ASTParser.newParser(AST.JLS12);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
    }

    public void assertParses(String code) {
        parser.setSource(code.toCharArray());
        final HashMap<String, String> options = new HashMap<>();
        JavaCore.setComplianceOptions(JavaCore.VERSION_11, options);
        parser.setCompilerOptions(options);
        final CompilationUnit result = (CompilationUnit)parser.createAST(null);
        for(IProblem problem : result.getProblems()) {
            assertFalse(problem.isError(), problem.getMessage());
        }
    }
}
