package mb.tiger.spoofax.task;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.jsglr.common.TermTracer;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.CommandFeedbacks;
import mb.spoofax.core.language.command.CommandInput;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.task.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

@LanguageScope
public class TigerShowParsedAstTaskDef implements TaskDef<CommandInput<TigerShowParsedAstTaskDef.Args>, CommandOutput> {
    public static class Args implements Serializable {
        public final ResourceKey key;
        public final @Nullable Region region;

        public Args(ResourceKey key, @Nullable Region region) {
            this.key = key;
            this.region = region;
        }

        @Override public boolean equals(@Nullable Object obj) {
            if(this == obj) return true;
            if(obj == null || getClass() != obj.getClass()) return false;
            final Args other = (Args)obj;
            return key.equals(other.key) && Objects.equals(region, other.region);
        }

        @Override public int hashCode() {
            return Objects.hash(key, region);
        }

        @Override public String toString() {
            return key.toString() + (region != null ? "@" + region : "");
        }
    }

    private final TigerParse parse;

    @Inject public TigerShowParsedAstTaskDef(TigerParse parse) {
        this.parse = parse;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandOutput exec(ExecContext context, CommandInput<Args> input) throws Exception {
        final ResourceKey key = input.args.key;
        final @Nullable Region region = input.args.region;

        final JSGLR1ParseResult parseResult = context.require(parse, key);
        final IStrategoTerm ast = parseResult.getAst()
            .orElseThrow(() -> new RuntimeException("Cannot show parsed AST, parsed AST for '" + key + "' is null"));

        final IStrategoTerm term;
        if(region != null) {
            term = TermTracer.getSmallestTermEncompassingRegion(ast, region);
        } else {
            term = ast;
        }

        final String formatted = StrategoUtil.toString(term);
        return new CommandOutput(ListView.of(CommandFeedbacks.showText(formatted, "Parsed AST for '" + key + "'", null)));
    }
}
