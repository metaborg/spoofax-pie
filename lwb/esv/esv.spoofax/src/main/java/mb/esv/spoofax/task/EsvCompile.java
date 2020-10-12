package mb.esv.spoofax.task;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.esv.spoofax.EsvScope;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@EsvScope
public class EsvCompile implements TaskDef<EsvCompile.Input, Result<IStrategoTerm, ?>> {
    public static class Input implements Serializable {
        public final Supplier<? extends Result<IStrategoTerm, ?>> mainAstSupplier;
        public final Function<String, ? extends Result<IStrategoTerm, ?>> importToAstFunction;
        public final ListView<STask<?>> originTasks;

        public Input(
            Supplier<? extends Result<IStrategoTerm, ?>> mainAstSupplier,
            Function<String, ? extends Result<IStrategoTerm, ?>> importToAstFunction,
            ListView<STask<?>> originTasks
        ) {
            this.mainAstSupplier = mainAstSupplier;
            this.importToAstFunction = importToAstFunction;
            this.originTasks = originTasks;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return mainAstSupplier.equals(input.mainAstSupplier) &&
                importToAstFunction.equals(input.importToAstFunction) &&
                originTasks.equals(input.originTasks);
        }

        @Override public int hashCode() {
            return Objects.hash(mainAstSupplier, importToAstFunction, originTasks);
        }

        @Override public String toString() {
            return "Input{" +
                "mainAstSupplier=" + mainAstSupplier +
                ", importToAstFunction=" + importToAstFunction +
                ", originTasks=" + originTasks +
                '}';
        }
    }

    @Inject public EsvCompile() {}

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<IStrategoTerm, ?> exec(ExecContext context, Input input) throws IOException {
        final Result<IStrategoTerm, ?> mainAstResult = context.require(input.mainAstSupplier);
        if(mainAstResult.isErr()) {
            return mainAstResult;
        }

        final ArrayList<IStrategoTerm> sections;
        try {
            sections = addToSectionsAndRecurseImports(context, input.importToAstFunction, mainAstResult.unwrapUnchecked());
        } catch(Exception e) {
            return Result.ofErr(e);
        }

        final TermFactory termFactory = new TermFactory();
        return Result.ofOk(termFactory.makeAppl("Module", termFactory.makeString("editor"), termFactory.makeAppl("NoImports"), termFactory.makeList(sections)));
    }


    private ArrayList<IStrategoTerm> addToSectionsAndRecurseImports(
        ExecContext context,
        Function<String, ? extends Result<IStrategoTerm, ?>> importToAstFunction,
        IStrategoTerm ast
    ) throws Exception {
        final ArrayList<IStrategoTerm> sections = new ArrayList<>();
        final HashSet<String> seenImports = new HashSet<>();
        addToSectionsAndRecurseImports(context, importToAstFunction, sections, seenImports, ast);
        return sections;
    }

    private void addToSectionsAndRecurseImports(
        ExecContext context,
        Function<String, ? extends Result<IStrategoTerm, ?>> importToAstFunction,
        ArrayList<IStrategoTerm> sections,
        HashSet<String> seenModules,
        IStrategoTerm ast
    ) throws Exception {
        if(!isModule(ast)) throw new Exception("AST '" + ast + "' is not a Module/3 term");
        seenModules.add(getNameFromModule(ast));
        sections.addAll(getSectionsFromModule(ast));
        final IStrategoTerm importsTerm = ast.getSubterm(1);
        if(isImports(importsTerm)) {
            for(IStrategoTerm imp : importsTerm.getSubterm(0)) {
                final String importName = TermUtils.toJavaStringAt(imp, 0);
                if(seenModules.contains(importName)) continue; // Short-circuit cyclic imports.
                final IStrategoTerm importedAst = context.require(importToAstFunction, importName).unwrap();
                addToSectionsAndRecurseImports(context, importToAstFunction, sections, seenModules, importedAst);
            }
        }
    }

    private boolean isModule(IStrategoTerm ast) {
        return TermUtils.isAppl(ast, "Module", 3);
    }

    private String getNameFromModule(IStrategoTerm ast) {
        return TermUtils.toJavaStringAt(ast, 0);
    }

    private List<IStrategoTerm> getSectionsFromModule(IStrategoTerm ast) {
        return ast.getSubterm(2).getSubterms();
    }

    private boolean isImports(IStrategoTerm ast) {
        return TermUtils.isAppl(ast, "Imports", 1);
    }
}
