package mb.tiger;

import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.resource.DefaultResourceKey;
import mb.resource.ResourceKey;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TigerConstraintAnalyzerTest {
    private final TigerParser parser = new TigerParser(TigerParseTable.fromClassLoaderResources());
    private final StrategoRuntime runtime = TigerStrategoRuntimeBuilder.fromClassLoaderResources().build();
    private final TigerConstraintAnalyzer analyzer = new TigerConstraintAnalyzer(runtime);

    TigerConstraintAnalyzerTest() throws IOException, JSGLR1ParseTableException, StrategoException {}

    @Test void analyze() throws InterruptedException, ConstraintAnalyzerException {
        final String str = "1 + nil";
        final ResourceKey resource = new DefaultResourceKey(0, 0);
        final JSGLR1ParseResult parsed = parser.parse(str, "Module", resource);
        assertNotNull(parsed.ast);
        final SingleFileResult result = analyzer.analyze(resource, parsed.ast, new ConstraintAnalyzerContext());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.containsError());
    }
}
