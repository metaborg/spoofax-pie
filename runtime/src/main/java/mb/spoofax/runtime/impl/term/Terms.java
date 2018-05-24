package mb.spoofax.runtime.impl.term;

import static org.spoofax.interpreter.terms.IStrategoTerm.*;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoReal;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;

public class Terms {
    public static boolean hasType(IStrategoTerm t, int type) {
        return t.getTermType() == type;
    }

    public static boolean hasArity(IStrategoTerm t, int arity) {
        return t.getSubtermCount() == arity;
    }

    public static boolean hasCons(IStrategoAppl t, String name) {
        return t.getConstructor().getName().equals(name);
    }
    
    public static boolean hasCons(IStrategoTerm t, String name) {
        return isAppl(t) && asAppl(t).getConstructor().getName().equals(name);
    }

    public static boolean hasCons(IStrategoAppl t, int arity) {
        return t.getConstructor().getArity() == arity;
    }
    
    public static boolean hasCons(IStrategoTerm t, int arity) {
        return isAppl(t) && asAppl(t).getConstructor().getArity() == arity;
    }

    public static boolean hasCons(IStrategoAppl t, int arity, String name) {
        final IStrategoConstructor cons = t.getConstructor();
        return cons.getArity() == arity && cons.getName().equals(name);
    }
    
    public static boolean hasCons(IStrategoTerm t, int arity, String name) {
        if(!isAppl(t))
            return false;
        final IStrategoConstructor cons = asAppl(t).getConstructor();
        return cons.getArity() == arity && cons.getName().equals(name);
    }


    public static boolean isInt(IStrategoTerm t) {
        return hasType(t, INT);
    }

    public static boolean isReal(IStrategoTerm t) {
        return hasType(t, REAL);
    }

    public static boolean isString(IStrategoTerm t) {
        return hasType(t, STRING);
    }

    public static boolean isList(IStrategoTerm t) {
        return hasType(t, LIST);
    }

    public static boolean isTuple(IStrategoTerm t) {
        return hasType(t, TUPLE);
    }

    public static boolean isTuple(IStrategoTerm t, int arity) {
        return hasType(t, TUPLE) && hasArity(t, arity);
    }

    public static boolean isAppl(IStrategoTerm t) {
        return hasType(t, APPL);
    }

    public static boolean isAppl(IStrategoTerm t, int arity) {
        return hasType(t, APPL) && hasCons(t, arity);
    }

    public static boolean isAppl(IStrategoTerm t, String cons) {
        return hasType(t, APPL) && hasCons(t, cons);
    }

    public static boolean isAppl(IStrategoTerm t, int arity, String cons) {
        return hasType(t, APPL) && hasCons(t, arity, cons);
    }


    public static int asInt(IStrategoTerm t) {
        return ((IStrategoInt) t).intValue();
    }

    public static double asReal(IStrategoTerm t) {
        return ((IStrategoReal) t).realValue();
    }

    public static String asString(IStrategoTerm t) {
        return ((IStrategoString) t).stringValue();
    }

    public static IStrategoList asList(IStrategoTerm t) {
        return (IStrategoList) t;
    }

    public static IStrategoTuple asTuple(IStrategoTerm t) {
        return (IStrategoTuple) t;
    }

    public static IStrategoAppl asAppl(IStrategoTerm t) {
        return (IStrategoAppl) t;
    }


    public static Stream<IStrategoTerm> stream(IStrategoTerm t) {
        return StreamSupport.stream(t.spliterator(), false);
    }
}
