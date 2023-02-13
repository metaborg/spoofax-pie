package mb.stratego.common;

import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.aterm_escape_strings_0_0;
import org.strategoxt.stratego_aterm.pp_aterm_box_0_0;
import org.strategoxt.stratego_gpp.box2text_string_0_1;

import java.util.function.Function;
import java.util.stream.Collectors;

public class StrategoUtil {
    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm term, IStrategoTerm ast, String fileString, String dirString) {
        return termFactory.makeTuple(term, termFactory.makeList(), ast, termFactory.makeString(fileString), termFactory.makeString(dirString));
    }

    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm ast, String fileString, String dirString) {
        return createLegacyBuilderInputTerm(termFactory, ast, ast, fileString, dirString);
    }

    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm ast, ResourcePath file, ResourcePath dir) {
        return createLegacyBuilderInputTerm(termFactory, ast, ast, dir.relativize(file), dir.toString());
    }

    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm ast, ResourcePath path) {
        return createLegacyBuilderInputTerm(termFactory, ast, ast, path);
    }

    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm term, IStrategoTerm ast, ResourcePath path) {
        final @Nullable ResourcePath parent = path.getParent();
        final @Nullable String leaf = path.getLeaf();
        if(parent != null && leaf != null) {
            return createLegacyBuilderInputTerm(termFactory, term, ast, leaf, parent.toString());
        } else if(leaf != null) {
            return createLegacyBuilderInputTerm(termFactory, term, ast, leaf, "");
        } else if(parent != null) {
            return createLegacyBuilderInputTerm(termFactory, term, ast, "", parent.toString());
        } else {
            return createLegacyBuilderInputTerm(termFactory, term, ast, "", "");
        }
    }

    public static IStrategoAppl toStrategyExpressionTerm(Strategy strategy, ITermFactory TF) {
        return strategy.match(Strategies.cases(
            () -> TF.makeAppl("Id"),
            () -> TF.makeAppl("Fail"),
            name -> toCallExpression(name, ListView.of(), ListView.of(), TF),
            (name, strategyArguments, termArguments) -> toCallExpression(name, strategyArguments, termArguments, TF)
        ));
    }

    private static IStrategoAppl toCallExpression(
        String name,
        ListView<Strategy> strategyArguments,
        ListView<IStrategoTerm> termArguments,
        ITermFactory TF
    ) {
        final String cifiedName = Interpreter.cify(name) + "_" + strategyArguments.size() + "_" + termArguments.size();

        return TF.makeAppl(
            "CallT",
            TF.makeAppl("SVar", TF.makeString(cifiedName)),
            TF.makeList(strategyArguments.stream()
                .map(str -> toStrategyExpressionTerm(str, TF))
                .collect(Collectors.toList())),
            TF.makeList(termArguments.asUnmodifiable())
        );
    }

    public static String name(Strategy strategy) {
        return strategy.match(Strategies.cases(
            () -> "id",
            () -> "fail",
            Function.identity(),
            (name, s, t) -> name
        ));
    }

}
