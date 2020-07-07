package mb.tiger;

import mb.common.message.Severity;
import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzer.MultiFileResult;
import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.resource.DefaultResourceKey;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class TigerConstraintAnalyzerTest extends TigerTestBase {
    @Test void analyzeSingleErrors() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource = new DefaultResourceKey(qualifier, "a.tig");
        final JSGLR1ParseOutput parsed = parser.parse("1 + nil", "Module", resource);
        assertTrue(parsed.isOk());
        final SingleFileResult result =
            analyzer.analyze(resource, parsed.getAst().get(), new ConstraintAnalyzerContext());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.containsError());
    }

    @Test void analyzeSingleSuccess() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource = new DefaultResourceKey(qualifier, "a.tig");
        final JSGLR1ParseOutput parsed = parser.parse("1 + 2", "Module", resource);
        assertTrue(parsed.isOk());
        final SingleFileResult result =
            analyzer.analyze(resource, parsed.getAst().get(), new ConstraintAnalyzerContext());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.isEmpty());
    }

    @Test void analyzeMultipleErrors() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource1 = new DefaultResourceKey(qualifier, "a.tig");
        final JSGLR1ParseOutput parsed1 = parser.parse("1 + 1", "Module", resource1);
        assertTrue(parsed1.getAst().isPresent());
        final ResourceKey resource2 = new DefaultResourceKey(qualifier, "b.tig");
        final JSGLR1ParseOutput parsed2 = parser.parse("1 + 2", "Module", resource2);
        assertTrue(parsed2.getAst().isPresent());
        final ResourceKey resource3 = new DefaultResourceKey(qualifier, "c.tig");
        final JSGLR1ParseOutput parsed3 = parser.parse("1 + nil", "Module", resource3);
        assertTrue(parsed3.getAst().isPresent());
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(resource1, parsed1.getAst().get());
        asts.put(resource2, parsed2.getAst().get());
        asts.put(resource3, parsed3.getAst().get());
        final MultiFileResult result = analyzer.analyze(null, asts, new ConstraintAnalyzerContext());
        final ConstraintAnalyzer.@Nullable Result result1 = result.getResult(resource1);
        assertNotNull(result1);
        assertNotNull(result1.ast);
        assertNotNull(result1.analysis);
        final ConstraintAnalyzer.@Nullable Result result2 = result.getResult(resource2);
        assertNotNull(result2);
        assertNotNull(result2.ast);
        assertNotNull(result2.analysis);
        final ConstraintAnalyzer.@Nullable Result result3 = result.getResult(resource3);
        assertNotNull(result3);
        assertNotNull(result3.ast);
        assertNotNull(result3.analysis);
        assertEquals(1, result.messages.size());
        assertTrue(result.messages.containsError());
        boolean foundCorrectMessage = result.messages.getAllMessages().stream()
            .filter(msg -> resource3.equals(msg.getKey()))
            .flatMap(msg -> msg.getValue().stream())
            .anyMatch(msg -> msg.severity.equals(Severity.Error));
        assertTrue(foundCorrectMessage);
    }

    @Test void analyzeMultipleSuccess() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource1 = new DefaultResourceKey(qualifier, "a.tig");
        final JSGLR1ParseOutput parsed1 = parser.parse("1 + 1", "Module", resource1);
        assertTrue(parsed1.getAst().isPresent());
        final ResourceKey resource2 = new DefaultResourceKey(qualifier, "b.tig");
        final JSGLR1ParseOutput parsed2 = parser.parse("1 + 2", "Module", resource2);
        assertTrue(parsed2.getAst().isPresent());
        final ResourceKey resource3 = new DefaultResourceKey(qualifier, "c.tig");
        final JSGLR1ParseOutput parsed3 = parser.parse("1 + 3", "Module", resource3);
        assertTrue(parsed3.getAst().isPresent());
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(resource1, parsed1.getAst().get());
        asts.put(resource2, parsed2.getAst().get());
        asts.put(resource3, parsed3.getAst().get());
        final MultiFileResult result = analyzer.analyze(null, asts, new ConstraintAnalyzerContext());
        final ConstraintAnalyzer.@Nullable Result result1 = result.getResult(resource1);
        assertNotNull(result1);
        assertNotNull(result1.ast);
        assertNotNull(result1.analysis);
        final ConstraintAnalyzer.@Nullable Result result2 = result.getResult(resource2);
        assertNotNull(result2);
        assertNotNull(result2.ast);
        assertNotNull(result2.analysis);
        final ConstraintAnalyzer.@Nullable Result result3 = result.getResult(resource3);
        assertNotNull(result3);
        assertNotNull(result3.ast);
        assertNotNull(result3.analysis);
        assertTrue(result.messages.isEmpty());
    }
}
