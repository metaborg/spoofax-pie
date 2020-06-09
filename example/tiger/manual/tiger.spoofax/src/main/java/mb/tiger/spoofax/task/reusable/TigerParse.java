package mb.tiger.spoofax.task.reusable;

import mb.common.result.MessagesError;
import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.jsglr1.pie.JSGLR1ParseTaskDef;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerParser;

import javax.inject.Inject;

@LanguageScope
public class TigerParse extends JSGLR1ParseTaskDef {
    private final javax.inject.Provider<TigerParser> parserProvider;

    @Inject public TigerParse(javax.inject.Provider<TigerParser> parserProvider) {
        this.parserProvider = parserProvider;
    }

    @Override public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerParse";
    }

    @Override protected Result<JSGLR1ParseOutput, MessagesError> parse(String text) throws InterruptedException {
        final TigerParser parser = parserProvider.get();
        return parser.parse(text, "Module");
    }
}
