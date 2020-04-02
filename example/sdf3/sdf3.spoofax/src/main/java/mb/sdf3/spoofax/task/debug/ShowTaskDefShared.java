package mb.sdf3.spoofax.task.debug;

import mb.common.util.StringUtil;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;

class ShowTaskDefShared {
    final Provider<StrategoRuntime> strategoRuntimeProvider;
    final String prettyPrintStrategy;
    final String resultName;

    ShowTaskDefShared(Provider<StrategoRuntime> strategoRuntimeProvider, String prettyPrintStrategy, String resultName) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.prettyPrintStrategy = prettyPrintStrategy;
        this.resultName = resultName;
    }

    CommandOutput provideOutput(boolean concrete, IStrategoTerm ast, ResourceKey file) throws StrategoException {
        if(concrete) {
            final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
            final @Nullable IStrategoTerm normalFormText = strategoRuntime.invoke(prettyPrintStrategy, ast);
            if(normalFormText == null) {
                throw new RuntimeException("Pretty-printing " + resultName + " AST failed (returned null)");
            }
            return CommandOutput.of(CommandFeedback.showText(StrategoUtil.toString(normalFormText), StringUtil.capitalize(resultName) + " (concrete) of '" + file + "'"));
        } else {
            return CommandOutput.of(CommandFeedback.showText(StrategoUtil.toString(ast), StringUtil.capitalize(resultName) + " (abstract) of '" + file + "'"));
        }
    }
}
