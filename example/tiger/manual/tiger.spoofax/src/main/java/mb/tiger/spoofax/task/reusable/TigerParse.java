package mb.tiger.spoofax.task.reusable;

import mb.common.result.Result;
import mb.common.text.Text;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseInput;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.pie.JsglrParseTaskDef;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.tiger.TigerParser;
import mb.tiger.spoofax.TigerScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;

@TigerScope
public class TigerParse extends JsglrParseTaskDef {
    private final Provider<TigerParser> parserProvider;

    @Inject public TigerParse(Provider<TigerParser> parserProvider) {
        this.parserProvider = parserProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override protected Result<JsglrParseOutput, JsglrParseException> parse(
        ExecContext context,
        Text text,
        @Nullable String startSymbol,
        @Nullable ResourceKey fileHint,
        @Nullable ResourcePath rootDirectoryHint,
        boolean codeCompletionMode,
        int cursorOffset
    ) throws InterruptedException {
        final TigerParser parser = parserProvider.get();
        try {
            return Result.ofOk(parser.parse(new JsglrParseInput(
                text,
                startSymbol != null ? startSymbol : "Module",
                fileHint,
                rootDirectoryHint,
                codeCompletionMode,
                cursorOffset
            )));
        } catch(JsglrParseException e) {
            return Result.ofErr(e);
        }
    }
}
