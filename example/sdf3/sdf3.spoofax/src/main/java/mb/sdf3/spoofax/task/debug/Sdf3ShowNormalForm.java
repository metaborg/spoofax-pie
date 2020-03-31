package mb.sdf3.spoofax.task.debug;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.sdf3.spoofax.task.Sdf3DesugarTemplates;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ToNormalForm;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;

@LanguageScope
public class Sdf3ShowNormalForm implements TaskDef<Sdf3ShowNormalForm.Args, CommandOutput> {
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
    private final Sdf3ToNormalForm toNormalForm;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public Sdf3ShowNormalForm(
        Sdf3Parse parse,
        Sdf3DesugarTemplates desugarTemplates,
        Sdf3ToNormalForm toNormalForm,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        this.parse = parse;
        this.desugarTemplates = desugarTemplates;
        this.toNormalForm = toNormalForm;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public CommandOutput exec(ExecContext context, Args args) throws Exception {
        final @Nullable IStrategoTerm normalFormAst = context.require(toNormalForm.createTask(desugarTemplates.createSupplier(parse.createAstSupplier(args.file))));
        if(normalFormAst == null) {
            throw new RuntimeException("Parse -> desugar -> transform to normal-form failed (returned null)");
        }

        if(args.concrete) {
            final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get();
            final @Nullable IStrategoTerm normalFormText = strategoRuntime.invoke("pp-SDF3-string", normalFormAst);
            if(normalFormText == null) {
                throw new RuntimeException("Pretty-printing normalized SDF3 AST to SDF3 text failed (returned null)");
            }
            return CommandOutput.of(CommandFeedback.showText(StrategoUtil.toString(normalFormText), "Normal form (concrete) of '" + args.file + "'"));
        } else {
            return CommandOutput.of(CommandFeedback.showText(StrategoUtil.toString(normalFormAst), "Normal form (abstract) of '" + args.file + "'"));
        }
    }
}
