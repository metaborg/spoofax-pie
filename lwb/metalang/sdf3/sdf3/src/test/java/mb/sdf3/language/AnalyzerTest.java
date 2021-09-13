package mb.sdf3.language;

import mb.common.message.Severity;
import mb.common.util.MapView;
import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzer.MultiFileResult;
import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.jsglr.common.JsglrParseOutput;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class AnalyzerTest extends TestBase {
    @Test void analyzeSingleErrors() throws Exception {
        final ReadableResource file = textFile("a.sdf3", "module a context-free sorts A context-free syntax A = B");
        final JsglrParseOutput parsed = parse(file);
        final SingleFileResult result = analyze(file, parsed.ast);
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.containsError());
    }

    @Test void analyzeSingleSuccess() throws Exception {
        final ReadableResource file = textFile("a.sdf3", "module a");
        final JsglrParseOutput parsed = parse(file);
        final SingleFileResult result =
            analyze(file, parsed.ast);
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.isEmpty());
    }

    @Test void analyzeMultipleErrors() throws Exception {
        final ReadableResource file1 = textFile("a.sdf3", "module a sorts A");
        final JsglrParseOutput parsed1 = parse(file1);
        final ReadableResource file2 = textFile("b.sdf3", "module b imports a context-free sorts B context-free syntax B = A");
        final JsglrParseOutput parsed2 = parse(file2);
        final ReadableResource file3 = textFile("c.sdf3", "module c imports a context-free context-free syntax C = A B");
        final JsglrParseOutput parsed3 = parse(file3);
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(file1.getKey(), parsed1.ast);
        asts.put(file2.getKey(), parsed2.ast);
        asts.put(file3.getKey(), parsed3.ast);
        final MultiFileResult result = analyze(MapView.of(asts));
        final ConstraintAnalyzer.@Nullable Result result1 = result.getResult(file1.getKey());
        assertNotNull(result1);
        assertNotNull(result1.ast);
        assertNotNull(result1.analysis);
        final ConstraintAnalyzer.@Nullable Result result2 = result.getResult(file2.getKey());
        assertNotNull(result2);
        assertNotNull(result2.ast);
        assertNotNull(result2.analysis);
        final ConstraintAnalyzer.@Nullable Result result3 = result.getResult(file3.getKey());
        assertNotNull(result3);
        assertNotNull(result3.ast);
        assertNotNull(result3.analysis);

        assertNumErrorsEqualsOrMore(result.messages, 2);
        assertTrue(result.messages.containsError());
        boolean foundCorrectMessage = result.messages.getMessagesWithKey().stream()
            .filter(msg -> file3.getKey().equals(msg.getKey()))
            .flatMap(msg -> msg.getValue().stream())
            .anyMatch(msg -> msg.severity.equals(Severity.Error));
        assertTrue(foundCorrectMessage);
    }

    @Test void analyzeMultipleSuccess() throws Exception {
        final ReadableResource file1 = textFile("a.sdf3", "module a context-free sorts A context-free syntax A = \"\"");
        final JsglrParseOutput parsed1 = parse(file1);
        final ReadableResource file2 = textFile("b.sdf3", "module b imports a context-free sorts B context-free syntax B = A");
        final JsglrParseOutput parsed2 = parse(file2);
        final ReadableResource file3 = textFile("c.sdf3", "module c imports a b context-free sorts C context-free syntax C = A context-free syntax C = B");
        final JsglrParseOutput parsed3 = parse(file3);
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(file1.getKey(), parsed1.ast);
        asts.put(file2.getKey(), parsed2.ast);
        asts.put(file3.getKey(), parsed3.ast);
        final MultiFileResult result = analyze(MapView.of(asts));
        final ConstraintAnalyzer.@Nullable Result result1 = result.getResult(file1.getKey());
        assertNotNull(result1);
        assertNotNull(result1.ast);
        assertNotNull(result1.analysis);
        final ConstraintAnalyzer.@Nullable Result result2 = result.getResult(file2.getKey());
        assertNotNull(result2);
        assertNotNull(result2.ast);
        assertNotNull(result2.analysis);
        final ConstraintAnalyzer.@Nullable Result result3 = result.getResult(file3.getKey());
        assertNotNull(result3);
        assertNotNull(result3.ast);
        assertNotNull(result3.analysis);

        assertFalse(result.messages.containsError());
    }

    @Test void showScopeGraph() throws Exception {
        final ReadableResource file = textFile("a.sdf3", "module a");
        final JsglrParseOutput parsed = parse(file);
        final ConstraintAnalyzerContext constraintAnalyzerContext = new ConstraintAnalyzerContext(true, rootPath);
        final SingleFileResult result = analyze(rootPath, file, parsed.ast, constraintAnalyzerContext);
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.isEmpty());
        final IStrategoTerm input = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), result.ast, file.toString(), rootPath.toString());
        final @Nullable IStrategoTerm output = strategoRuntime.addContextObject(constraintAnalyzerContext).invoke("stx--show-scopegraph", input);
        assertNotNull(output);
    }
}
