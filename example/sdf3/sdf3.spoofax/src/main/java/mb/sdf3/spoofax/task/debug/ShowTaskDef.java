package mb.sdf3.spoofax.task.debug;

import mb.common.util.StringUtil;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.sdf3.spoofax.task.Sdf3DesugarTemplates;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;

public abstract class ShowTaskDef implements TaskDef<ShowTaskDef.Args, CommandOutput> {
    public static class Args implements Serializable {
        public final ResourceKey file;
        public final boolean concrete;

        public Args(ResourceKey file, boolean concrete) {
            this.file = file;
            this.concrete = concrete;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Args args = (Args)o;
            return concrete == args.concrete &&
                file.equals(args.file);
        }

        @Override public int hashCode() {
            return Objects.hash(file, concrete);
        }

        @Override public String toString() {
            return "Args{file=" + file + ", concrete=" + concrete + '}';
        }
    }

    private final Sdf3Parse parse;
    private final Sdf3DesugarTemplates desugarTemplates;
    private final TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> operation;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final String prettyPrintStrategy;
    private final String resultName;

    public ShowTaskDef(
        Sdf3Parse parse,
        Sdf3DesugarTemplates desugarTemplates,
        TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> operation,
        Provider<StrategoRuntime> strategoRuntimeProvider,
        String prettyPrintStrategy,
        String resultName
    ) {
        this.parse = parse;
        this.desugarTemplates = desugarTemplates;
        this.operation = operation;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.prettyPrintStrategy = prettyPrintStrategy;
        this.resultName = resultName;
    }

    @Override
    public CommandOutput exec(ExecContext context, Args args) throws Exception {
        final @Nullable IStrategoTerm normalFormAst = context.require(operation.createTask(desugarTemplates.createSupplier(parse.createAstSupplier(args.file))));
        if(normalFormAst == null) {
            throw new RuntimeException("Parse -> desugar -> transform to " + resultName + " failed (returned null)");
        }

        if(args.concrete) {
            final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
            final @Nullable IStrategoTerm normalFormText = strategoRuntime.invoke(prettyPrintStrategy, normalFormAst);
            if(normalFormText == null) {
                throw new RuntimeException("Pretty-printing " + resultName + " AST failed (returned null)");
            }
            return CommandOutput.of(CommandFeedback.showText(StrategoUtil.toString(normalFormText), StringUtil.capitalize(resultName) + " (concrete) of '" + args.file + "'"));
        } else {
            return CommandOutput.of(CommandFeedback.showText(StrategoUtil.toString(normalFormAst), StringUtil.capitalize(resultName) + " (abstract) of '" + args.file + "'"));
        }
    }
}
