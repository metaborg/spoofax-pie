package mb.esv.common;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.ArrayList;
import java.util.List;

import static org.spoofax.interpreter.terms.IStrategoTerm.APPL;
import static org.spoofax.interpreter.terms.IStrategoTerm.STRING;
import static org.spoofax.terms.Term.asJavaString;
import static org.spoofax.terms.Term.termAt;
import static org.spoofax.terms.Term.tryGetName;

/**
 * Term reading utility class for ESV abstract syntax.
 */
public class ESVReader {
    public static @Nullable IStrategoAppl findTerm(IStrategoTerm term, String constructor) {
        if(term.getTermType() == IStrategoTerm.APPL && constructor.equals(constructorName(term))) {
            return (IStrategoAppl) term;
        }
        for(IStrategoTerm subterm : term) {
            final @Nullable IStrategoAppl result = findTerm(subterm, constructor);
            if(result != null) {
                return result;
            }
        }
        return null;
    }

    public static ArrayList<IStrategoAppl> collectTerms(IStrategoTerm term, String... constructors) {
        final ArrayList<IStrategoAppl> results = new ArrayList<>();
        for(String constructor : constructors) {
            collectTerms(term, constructor, results);
        }
        return results;
    }

    private static void collectTerms(IStrategoTerm term, String constructor, ArrayList<IStrategoAppl> results) {
        if(term.getTermType() == IStrategoTerm.APPL && constructor.equals(constructorName(term))) {
            results.add((IStrategoAppl) term);
        }
        for(IStrategoTerm subterm : term) {
            collectTerms(subterm, constructor, results);
        }
    }


    public static @Nullable String termContents(@Nullable IStrategoTerm t) {
        if(t == null) {
            return null;
        }

        String result;
        if(t.getTermType() == STRING) {
            result = asJavaString(t);
        } else if(t.getSubtermCount() == 1 && "Values".equals(tryGetName(t))) {
            return concatTermStrings((IStrategoList) t.getSubterm(0));
        } else if(t.getTermType() == APPL && t.getSubtermCount() == 1 && termAt(t, 0).getTermType() == STRING) {
            result = asJavaString(termAt(t, 0));
        } else if(t.getTermType() == APPL && t.getSubtermCount() == 1) {
            return termContents(termAt(t, 0));
        } else {
            return null;
        }

        if(result.startsWith("\"") && result.endsWith("\"") && result.length() > 1) {
            result = result.substring(1, result.length() - 1).replace("\\\\", "\"");
        }

        return result;
    }


    public static @Nullable List<String> termListContents(@Nullable IStrategoTerm t) {
        if(t == null) {
            return null;
        }

        final List<String> results = new ArrayList<>();
        if(t.getSubtermCount() == 1 && "Values".equals(tryGetName(t))) {
            final IStrategoList values = (IStrategoList) t.getSubterm(0);
            for(int i = 0; i < values.getSubtermCount(); i++) {
                results.add(termContents(termAt(values, i)));
            }
        } else {
            return null;
        }

        return results;
    }

    public static @Nullable String getProperty(IStrategoTerm document, String name) {
        return getProperty(document, name, null);
    }

    public static @Nullable String getProperty(IStrategoTerm document, String name, @Nullable String defaultValue) {
        final @Nullable IStrategoAppl result = findTerm(document, name);
        if(result == null) {
            return defaultValue;
        }
        return termContents(result);
    }

    public static String concatTermStrings(IStrategoList values) {
        final StringBuilder results = new StringBuilder();
        if(values.getSubtermCount() > 0) {
            results.append(termContents(termAt(values, 0)));
        }
        for(int i = 1; i < values.getSubtermCount(); i++) {
            results.append(',');
            results.append(termContents(termAt(values, i)));
        }
        return results.toString();
    }

    public static @Nullable String constructorName(@Nullable IStrategoTerm t) {
        if(t == null || t.getTermType() != APPL) {
            return null;
        }
        return ((IStrategoAppl) t).getConstructor().getName();
    }
}
