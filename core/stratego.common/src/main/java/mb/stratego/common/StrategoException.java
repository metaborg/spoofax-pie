package mb.stratego.common;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

@ADT
public abstract class StrategoException extends Exception {
    public interface Cases<R> {
        R strategyFail(String strategyName, IStrategoTerm input, String[] trace);

        R fatalFail(String strategyName, IStrategoTerm input, String[] trace);

        R fatalFailWithTerm(String strategyName, IStrategoTerm input, String[] trace, IStrategoTerm term);

        R exitFail(String strategyName, IStrategoTerm input, String[] trace, int exitCode);

        R strategyUndefined(String strategyName, IStrategoTerm input, String[] trace, String undefinedStrategyName);

        R exceptionalFail(String strategyName, IStrategoTerm input, String[] trace, InterpreterException cause);
    }

    public static StrategoException strategyFail(String strategyName, IStrategoTerm input, String[] trace) {
        return StrategoExceptions.strategyFail(strategyName, input, trace);
    }

    public static StrategoException fatalFail(String strategyName, IStrategoTerm input, String[] trace) {
        return StrategoExceptions.fatalFail(strategyName, input, trace);
    }

    public static StrategoException fatalFailWithTerm(String strategyName, IStrategoTerm input, String[] trace, IStrategoTerm term) {
        return StrategoExceptions.fatalFailWithTerm(strategyName, input, trace, term);
    }

    public static StrategoException exitFail(String strategyName, IStrategoTerm input, String[] trace, int exitCode) {
        return StrategoExceptions.exitFail(strategyName, input, trace, exitCode);
    }

    public static StrategoException strategyUndefined(String strategyName, IStrategoTerm input, String[] trace, String undefinedStrategyName) {
        return StrategoExceptions.strategyUndefined(strategyName, input, trace, undefinedStrategyName);
    }

    public static StrategoException exceptionalFail(String strategyName, IStrategoTerm input, String[] trace, InterpreterException cause) {
        return StrategoExceptions.exceptionalFail(strategyName, input, trace, cause);
    }

    public static StrategoException fromInterpreterException(String strategyName, IStrategoTerm input, String[] trace, InterpreterException interpreterException) {
        try {
            throw interpreterException;
        } catch(InterpreterErrorExit e) {
            final @Nullable IStrategoList innerTrace = e.getTrace();
            final String[] finalTrace;
            if(innerTrace != null) {
                final int count = innerTrace.getSubtermCount();
                finalTrace = new String[count];
                for(int i = 0; i < count; i++) {
                    finalTrace[i] = StrategoUtil.toString(innerTrace.getSubterm(i));
                }
            } else {
                finalTrace = trace;
            }
            final @Nullable IStrategoTerm term = e.getTerm();
            if(term != null) {
                return fatalFailWithTerm(strategyName, input, finalTrace, term);
            } else {
                return fatalFail(strategyName, input, finalTrace);
            }
        } catch(InterpreterExit e) {
            return exitFail(strategyName, input, trace, e.getValue());
        } catch(UndefinedStrategyException e) {
            return strategyUndefined(strategyName, input, trace, e.getStrategyName());
        } catch(InterpreterException e) {
            return exceptionalFail(strategyName, input, trace, e);
        }
    }


    public abstract <R> R match(Cases<R> cases);

    public StrategoExceptions.CaseOfMatchers.TotalMatcher_StrategyFail caseOf() {
        return StrategoExceptions.caseOf(this);
    }

    public String getStrategyName() {
        return StrategoExceptions.getStrategyName(this);
    }

    public IStrategoTerm getInput() {
        return StrategoExceptions.getInput(this);
    }

    public String[] getTrace() {
        return StrategoExceptions.getTrace(this);
    }


    @Override public String getMessage() {
        return caseOf()
            .strategyFail((strategyName, input, trace) -> createMessage(strategyName, input, trace, ""))
            .fatalFail((strategyName, input, trace) -> createMessage(strategyName, input, trace, ""))
            .fatalFailWithTerm((strategyName, input, trace, term) -> createMessage(strategyName, input, trace, " at term:\n\t" + StrategoUtil.toString(term)))
            .exitFail((strategyName, input, trace, exitCode) -> createMessage(strategyName, input, trace, " with exit code '" + exitCode + "'"))
            .strategyUndefined((strategyName, input, trace, undefinedStrategyName) -> createMessage(strategyName, input, trace, "; strategy '" + undefinedStrategyName + "' is undefined"))
            .exceptionalFail((strategyName, input, trace, cause) -> createMessage(strategyName, input, trace, "unexpectedly due to:\n\n" + cause.getMessage()))
            ;
    }

    @Override public synchronized Throwable getCause() {
        return caseOf()
            .strategyFail((strategyName, input, trace) -> (Throwable)null)
            .fatalFail((strategyName, input, trace) -> null)
            .fatalFailWithTerm((strategyName, input, trace, term) -> null)
            .exitFail((strategyName, input, trace, exitCode) -> null)
            .strategyUndefined((strategyName, input, trace, undefinedStrategyName) -> null)
            .exceptionalFail((strategyName, input, trace, cause) -> cause);
    }

    private String createMessage(String strategyName, IStrategoTerm input, String[] trace, String postfix) {
        final StringBuilder sb = new StringBuilder();
        sb
            .append("Invoking Stratego strategy '")
            .append(strategyName)
            .append("' failed on input:\n")
            .append(StrategoUtil.toString(input))
            .append("\n")
            .append(postfix)
        ;
        if(!postfix.isEmpty()) {
            sb.append('\n');
        }
        sb.append("Stratego stack trace:");
        for(String line : trace) { // TODO: should be printed in reverse?
            sb.append("\n\t");
            sb.append(line);
        }
        return sb.toString();
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();


    protected StrategoException() {
        super(null, null, true, false);
    }
}
