package mb.stlcrec;

import mb.common.message.Severity;
import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzer.MultiFileResult;
import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.resource.DefaultResourceKey;
import mb.resource.ResourceKey;
import mb.statix.common.StatixPrimitiveLibrary;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Statix does not work outside of Spoofax Core yet")
class STLCRecConstraintAnalyzerTest {
    private final STLCRecParser parser = new STLCRecParser(STLCRecParseTable.fromClassLoaderResources());
    private final StrategoRuntime runtime =
        STLCRecStrategoRuntimeBuilder.fromClassLoaderResources().addLibrary(new StatixPrimitiveLibrary()).build();
    private final STLCRecConstraintAnalyzer analyzer = new STLCRecConstraintAnalyzer(runtime);

    STLCRecConstraintAnalyzerTest() throws IOException, JSGLR1ParseTableException, StrategoException {}

    @Test void analyzeSingleErrors() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource = new DefaultResourceKey(0, 0);
        final JSGLR1ParseResult parsed = parser.parse("1 + nil", "Start", resource);
        assertNotNull(parsed.ast);
        final SingleFileResult result = analyzer.analyze(resource, parsed.ast, new ConstraintAnalyzerContext());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.containsError());
    }

    @Test void analyzeSingleSuccess() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource = new DefaultResourceKey(0, 0);
        final JSGLR1ParseResult parsed = parser.parse("1 + 2", "Start", resource);
        assertNotNull(parsed.ast);
        final SingleFileResult result = analyzer.analyze(resource, parsed.ast, new ConstraintAnalyzerContext());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.isEmpty());
    }

    @Test void analyzeMultipleErrors() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource1 = new DefaultResourceKey(0, 1);
        final JSGLR1ParseResult parsed1 = parser.parse("1 + 1", "Start", resource1);
        assertNotNull(parsed1.ast);
        final ResourceKey resource2 = new DefaultResourceKey(0, 2);
        final JSGLR1ParseResult parsed2 = parser.parse("1 + 2", "Start", resource2);
        assertNotNull(parsed2.ast);
        final ResourceKey resource3 = new DefaultResourceKey(0, 3);
        final JSGLR1ParseResult parsed3 = parser.parse("1 + nil", "Start", resource3);
        assertNotNull(parsed3.ast);
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(resource1, parsed1.ast);
        asts.put(resource2, parsed2.ast);
        asts.put(resource3, parsed3.ast);
        final MultiFileResult result = analyzer.analyze(null, asts, new ConstraintAnalyzerContext());
        final ConstraintAnalyzer.@Nullable Result result1 = result.results.get(resource1);
        assertNotNull(result1);
        assertNotNull(result1.ast);
        assertNotNull(result1.analysis);
        final ConstraintAnalyzer.@Nullable Result result2 = result.results.get(resource2);
        assertNotNull(result2);
        assertNotNull(result2.ast);
        assertNotNull(result2.analysis);
        final ConstraintAnalyzer.@Nullable Result result3 = result.results.get(resource3);
        assertNotNull(result3);
        assertNotNull(result3.ast);
        assertNotNull(result3.analysis);
        assertEquals(1, result.messages.size());
        assertTrue(result.messages.containsError());
        final boolean[] foundCorrectMessage = {false};
        result.messages.accept((text, exception, severity, resource, region) -> {
            if(resource3.equals(resource) && severity.equals(Severity.Error)) {
                foundCorrectMessage[0] = true;
                return false;
            }
            return true;
        });
        assertTrue(foundCorrectMessage[0]);
    }

    @Test void analyzeMultipleSuccess() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource1 = new DefaultResourceKey(0, 1);
        final JSGLR1ParseResult parsed1 = parser.parse("1 + 1", "Start", resource1);
        assertNotNull(parsed1.ast);
        final ResourceKey resource2 = new DefaultResourceKey(0, 2);
        final JSGLR1ParseResult parsed2 = parser.parse("1 + 2", "Start", resource2);
        assertNotNull(parsed2.ast);
        final ResourceKey resource3 = new DefaultResourceKey(0, 3);
        final JSGLR1ParseResult parsed3 = parser.parse("1 + 3", "Start", resource3);
        assertNotNull(parsed3.ast);
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(resource1, parsed1.ast);
        asts.put(resource2, parsed2.ast);
        asts.put(resource3, parsed3.ast);
        final MultiFileResult result = analyzer.analyze(null, asts, new ConstraintAnalyzerContext());
        final ConstraintAnalyzer.@Nullable Result result1 = result.results.get(resource1);
        assertNotNull(result1);
        assertNotNull(result1.ast);
        assertNotNull(result1.analysis);
        final ConstraintAnalyzer.@Nullable Result result2 = result.results.get(resource2);
        assertNotNull(result2);
        assertNotNull(result2.ast);
        assertNotNull(result2.analysis);
        final ConstraintAnalyzer.@Nullable Result result3 = result.results.get(resource3);
        assertNotNull(result3);
        assertNotNull(result3.ast);
        assertNotNull(result3.analysis);
        assertTrue(result.messages.isEmpty());
    }
}
