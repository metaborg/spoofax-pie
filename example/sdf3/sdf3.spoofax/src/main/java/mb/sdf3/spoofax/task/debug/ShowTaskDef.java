package mb.sdf3.spoofax.task.debug;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;

public abstract class ShowTaskDef extends ProvideOutputShared implements TaskDef<ShowTaskDef.Args, CommandOutput> {
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
    private final TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> desugar;
    private final TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> operation;

    public ShowTaskDef(
        Sdf3Parse parse,
        TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> desugar,
        TaskDef<Supplier<@Nullable IStrategoTerm>, @Nullable IStrategoTerm> operation,
        Provider<StrategoRuntime> strategoRuntimeProvider,
        String prettyPrintStrategy,
        String resultName
    ) {
        super(strategoRuntimeProvider, prettyPrintStrategy, resultName);
        this.parse = parse;
        this.desugar = desugar;
        this.operation = operation;
    }

    @Override public CommandOutput exec(ExecContext context, Args args) throws Exception {
        final @Nullable IStrategoTerm normalFormAst = context.require(operation.createTask(desugar.createSupplier(parse.createAstSupplier(args.file).map(Result::get)))); // TODO: use Result
        if(normalFormAst == null)
            throw new RuntimeException("Parse -> desugar -> transform to " + resultName + " failed (returned null)");
        return provideOutput(args.concrete, normalFormAst, args.file);
    }
}
