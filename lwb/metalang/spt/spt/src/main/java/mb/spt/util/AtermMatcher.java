package mb.spt.util;

import mb.spt.fromterm.InvalidAstShapeException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.StringUtils;
import org.spoofax.terms.util.TermUtils;

import java.util.Iterator;

public class AtermMatcher {
    private static final String ANNO_CONS = "Anno";
    private static final String LIST_CONS = "List";
    private static final String APPL_CONS = "Appl";
    private static final String TUPLE_CONS = "Tuple";
    private static final String INT_CONS = "Int";
    private static final String STRING_CONS = "String";
    private static final String WLD_CONS = "Wld";

    /**
     * Check if the given AST matches the given SPT ATerm match pattern.
     *
     * @param ast     the AST to compare to the pattern.
     * @param match   the SPT pattern to match the AST against.
     * @param factory required to make SPT constructors.
     * @return true iff the AST matched against the given pattern.
     */
    public static boolean check(IStrategoTerm ast, IStrategoTerm match, ITermFactory factory) {
        final IStrategoAppl matchAppl = TermUtils.asAppl(match)
            .orElseThrow(() -> new InvalidAstShapeException("term application", match));
        switch(matchAppl.getConstructor().getName()) {
            case ANNO_CONS:
                // Anno(Match, [AnnoMatch, ...])
                // check the term, and then check the annotations of the term
                return check(ast, match.getSubterm(0), factory) &&
                    check(ast.getAnnotations(), factory.makeAppl(factory.makeConstructor("List", 1), match.getSubterm(1)), factory);
            case LIST_CONS: {
                // List([Match, ...])
                if(!TermUtils.isList(ast)) {
                    return false;
                }
                final IStrategoList list = (IStrategoList)ast;
                final IStrategoList matchList = (IStrategoList)match.getSubterm(0);
                if(matchList.size() != list.size()) {
                    return false;
                }
                final Iterator<IStrategoTerm> matchIt = matchList.iterator();
                final Iterator<IStrategoTerm> listIt = list.iterator();
                boolean allSubtermsMatch = true;
                while(matchIt.hasNext()) {
                    if(!check(listIt.next(), matchIt.next(), factory)) {
                        allSubtermsMatch = false;
                        break;
                    }
                }
                return allSubtermsMatch;
            }
            case APPL_CONS: {
                // Appl("ConsName", [KidMatch, ...])
                // we ignore any annotations on the AST
                if(!TermUtils.isAppl(ast)) {
                    return false;
                }
                final IStrategoAppl astAppl = TermUtils.asAppl(ast)
                    .orElseThrow(() -> new InvalidAstShapeException("term application", ast));
                if(!astAppl.getConstructor().getName().equals(TermUtils.toJavaString(match.getSubterm(0)))) {
                    return false;
                }
                final IStrategoList matchList = (IStrategoList)match.getSubterm(1);
                if(ast.getSubtermCount() != matchList.size()) {
                    return false;
                }
                final Iterator<IStrategoTerm> matchIt = matchList.iterator();
                boolean allSubtermsMatch = true;
                for(int i = 0; i < ast.getSubtermCount(); i++) {
                    if(!check(ast.getSubterm(i), matchIt.next(), factory)) {
                        allSubtermsMatch = false;
                        break;
                    }
                }
                return allSubtermsMatch;
            }
            case TUPLE_CONS: {
                // Tuple([KidMatch, ...])
                // we ignore any annotations on the AST
                if(!TermUtils.isTuple(ast)) {
                    return false;
                }
                final IStrategoList matchList = (IStrategoList)match.getSubterm(0);
                if(ast.getSubtermCount() != matchList.size()) {
                    return false;
                }
                final Iterator<IStrategoTerm> matchIt = matchList.iterator();
                boolean allSubtermsMatch = true;
                for(int i = 0; i < ast.getSubtermCount(); i++) {
                    if(!check(ast.getSubterm(i), matchIt.next(), factory)) {
                        allSubtermsMatch = false;
                        break;
                    }
                }
                return allSubtermsMatch;
            }
            case INT_CONS:
                // Int("n")
                return TermUtils.isInt(ast) && Integer.parseInt(TermUtils.toJavaString(match.getSubterm(0))) == TermUtils.toJavaInt(ast);
            case STRING_CONS:
                // String("some string")
                return TermUtils.isString(ast) && TermUtils.toJavaString(match.getSubterm(0)).equals(TermUtils.toJavaString(ast));
            case WLD_CONS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Pretty print the given SPT ATerm pattern.
     *
     * @param match the SPT ATerm pattern to pretty print to a 'normal' ATerm.
     * @return the pretty printed result.
     */
    public static String prettyPrint(IStrategoTerm match) {
        return prettyPrint(match, new StringBuilder()).toString();
    }

    /**
     * Append a pretty printed version of the given SPT pattern to the given builder.
     *
     * @param match the SPT ATerm pattern to pretty print to a 'normal' ATerm.
     * @param b     the builder to append to.
     * @return the given builder.
     */
    public static StringBuilder prettyPrint(IStrategoTerm match, StringBuilder b) {
        final IStrategoAppl matchAppl = TermUtils.asAppl(match)
            .orElseThrow(() -> new InvalidAstShapeException("term application", match));
        switch(matchAppl.getConstructor().getName()) {
            case ANNO_CONS:
                // Anno(Match, [AnnoMatch, ...])
                prettyPrint(match.getSubterm(0), b).append("{");
                prettyPrintListOfMatches((IStrategoList)match.getSubterm(1), ", ", b);
                b.append('}');
                return b;
            case LIST_CONS:
                // List([Match, ...])
                b.append('[');
                prettyPrintListOfMatches((IStrategoList)match.getSubterm(0), ", ", b);
                b.append(']');
                return b;
            case APPL_CONS:
                // Appl("ConsName", [KidMatch, ...])
                b.append(TermUtils.toJavaString(match.getSubterm(0))).append('(');
                prettyPrintListOfMatches((IStrategoList)match.getSubterm(1), ", ", b);
                b.append(')');
                return b;
            case TUPLE_CONS:
                // Tuple([KidMatch, ...])
                b.append('(');
                prettyPrintListOfMatches((IStrategoList)match.getSubterm(0), ", ", b);
                b.append(')');
                return b;
            case INT_CONS:
                // Int("n")
                b.append(TermUtils.toJavaString(match.getSubterm(0)));
                return b;
            case STRING_CONS:
                // String("some string")
                b.append(StringUtils.escape(TermUtils.toJavaStringAt(match, 0)));
                return b;
            case WLD_CONS:
                b.append('_');
                return b;
            default:
                throw new InvalidAstShapeException("SPT match term", match);
        }
    }

    private static void prettyPrintListOfMatches(IStrategoList matches, String join, StringBuilder b) {
        Iterator<IStrategoTerm> matchIt = matches.iterator();
        for(int i = 0; i < matches.size(); i++) {
            prettyPrint(matchIt.next(), b);
            if(i < matches.size() - 1) {
                b.append(join);
            }
        }
    }
}
