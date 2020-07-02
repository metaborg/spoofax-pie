package mb.sdf3.spoofax.task.debug;

import mb.common.util.StringUtil;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;

class ProvideOutputShared {
    final Provider<StrategoRuntime> strategoRuntimeProvider;
    final String prettyPrintStrategy;
    final String resultName;

    ProvideOutputShared(Provider<StrategoRuntime> strategoRuntimeProvider, String prettyPrintStrategy, String resultName) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.prettyPrintStrategy = prettyPrintStrategy;
        this.resultName = resultName;
    }

    CommandFeedback provideOutput(boolean concrete, IStrategoTerm ast, ResourceKey file) {
        if(concrete) {
            try {
                final IStrategoTerm text = strategoRuntimeProvider.get().invoke(prettyPrintStrategy, ast);
                return CommandFeedback.of(ShowFeedback.showText(StrategoUtil.toString(text), StringUtil.capitalize(resultName) + " (concrete) of '" + file + "'"));
            } catch(StrategoException e) {
                return CommandFeedback.of(new Exception("Pretty-printing '" + resultName + "' AST failed", e));
            }
        } else {
            return CommandFeedback.of(ShowFeedback.showText(StrategoUtil.toString(ast), StringUtil.capitalize(resultName) + " (abstract) of '" + file + "'"));
        }
    }
}
