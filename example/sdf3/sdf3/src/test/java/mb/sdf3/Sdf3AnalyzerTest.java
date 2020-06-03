package mb.sdf3;

import mb.common.message.Severity;
import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzer.MultiFileResult;
import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.resource.DefaultResourceKey;
import mb.resource.ResourceKey;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class Sdf3AnalyzerTest extends Sdf3TestBase {
    @Test void analyzeSingleErrors() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource = new DefaultResourceKey(qualifier, "a.sdf3");
        final JSGLR1ParseResult parsed = parser.parse("module a syntax A = B", startSymbol, resource);
        assertTrue(parsed.getAst().isPresent());
        final SingleFileResult result =
            analyzer.analyze(rootKey, resource, parsed.getAst().get(), new ConstraintAnalyzerContext());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.containsError());
    }

    @Test void analyzeSingleSuccess() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource = new DefaultResourceKey(qualifier, "a.sdf3");
        final JSGLR1ParseResult parsed = parser.parse("module a", startSymbol, resource);
        assertTrue(parsed.getAst().isPresent());
        final SingleFileResult result =
            analyzer.analyze(rootKey, resource, parsed.getAst().get(), new ConstraintAnalyzerContext());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.isEmpty());
    }

    @Test void analyzeMultipleErrors() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource1 = new DefaultResourceKey(qualifier, "a.sdf3");
        final JSGLR1ParseResult parsed1 = parser.parse("module a", startSymbol, resource1);
        assertTrue(parsed1.getAst().isPresent());
        final ResourceKey resource2 = new DefaultResourceKey(qualifier, "b.sdf3");
        final JSGLR1ParseResult parsed2 = parser.parse("module b syntax B = A", startSymbol, resource2);
        assertTrue(parsed2.getAst().isPresent());
        final ResourceKey resource3 = new DefaultResourceKey(qualifier, "c.sdf3");
        final JSGLR1ParseResult parsed3 = parser.parse("module c syntax C = A B", startSymbol, resource3);
        assertTrue(parsed3.getAst().isPresent());
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(resource1, parsed1.getAst().get());
        asts.put(resource2, parsed2.getAst().get());
        asts.put(resource3, parsed3.getAst().get());
        final MultiFileResult result = analyzer.analyze(rootKey, asts, new ConstraintAnalyzerContext());
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

        assertEquals(2, result.messages.size());
        assertTrue(result.messages.containsError());
        boolean foundCorrectMessage = result.messages
            .asStream()
            .anyMatch(msg -> resource3.equals(msg.getKey()) && msg.getValue().severity.equals(Severity.Error));
        assertTrue(foundCorrectMessage);
    }

    @Test void analyzeMultipleSuccess() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource1 = new DefaultResourceKey(qualifier, "a.sdf3");
        final JSGLR1ParseResult parsed1 = parser.parse("module a syntax A = \"\"", startSymbol, resource1);
        assertTrue(parsed1.getAst().isPresent());
        final ResourceKey resource2 = new DefaultResourceKey(qualifier, "b.sdf3");
        final JSGLR1ParseResult parsed2 = parser.parse("module b imports a syntax B = A", startSymbol, resource2);
        assertTrue(parsed2.getAst().isPresent());
        final ResourceKey resource3 = new DefaultResourceKey(qualifier, "c.sdf3");
        final JSGLR1ParseResult parsed3 = parser.parse("module c imports a b syntax C = A syntax C = B", startSymbol, resource3);
        assertTrue(parsed3.getAst().isPresent());
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(resource1, parsed1.getAst().get());
        asts.put(resource2, parsed2.getAst().get());
        asts.put(resource3, parsed3.getAst().get());
        final MultiFileResult result = analyzer.analyze(rootKey, asts, new ConstraintAnalyzerContext());
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

        assertFalse(result.messages.containsError());
    }

    @Test void showScopeGraph() throws InterruptedException, ConstraintAnalyzerException, StrategoException {
        final ResourceKey resource = new DefaultResourceKey(qualifier, "a.sdf3");
        final JSGLR1ParseResult parsed = parser.parse("module a", startSymbol, resource);
        assertTrue(parsed.getAst().isPresent());
        final ConstraintAnalyzerContext constraintAnalyzerContext = new ConstraintAnalyzerContext();
        final SingleFileResult result = analyzer.analyze(rootKey, resource, parsed.getAst().get(), constraintAnalyzerContext);
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.isEmpty());
        final IStrategoTerm input = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), result.ast, resource.toString(), rootKey.toString());
        final @Nullable IStrategoTerm output = strategoRuntime.addContextObject(constraintAnalyzerContext).invoke("stx--show-scopegraph", input);
        assertNotNull(output);
    }
}
