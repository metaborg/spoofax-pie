package mb.stratego.common;

import mb.common.util.StringFormatter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

public class StrategoRuntime {
    private final HybridInterpreter hybridInterpreter;
    private final StrategoIOAgent ioAgent;
    private final AdaptableContext contextObject;


    public StrategoRuntime(HybridInterpreter hybridInterpreter, StrategoIOAgent ioAgent, AdaptableContext contextObject) {
        this.hybridInterpreter = hybridInterpreter;
        this.ioAgent = ioAgent;
        this.contextObject = contextObject;
    }

    public StrategoRuntime(HybridInterpreter hybridInterpreter, StrategoIOAgent ioAgent) {
        this(hybridInterpreter, ioAgent, new AdaptableContext());
    }

    @SuppressWarnings("CopyConstructorMissesField") public StrategoRuntime(StrategoRuntime other) {
        this(other.hybridInterpreter, new StrategoIOAgent(other.ioAgent), new AdaptableContext(other.contextObject));
    }

    public StrategoRuntime(StrategoRuntime other, StrategoIOAgent ioAgent) {
        this(other.hybridInterpreter, ioAgent, new AdaptableContext(other.contextObject));
    }

    public StrategoRuntime(StrategoRuntime other, AdaptableContext contextObject) {
        this(other.hybridInterpreter, new StrategoIOAgent(other.ioAgent), contextObject);
    }

    public StrategoRuntime(StrategoRuntime other, StrategoIOAgent ioAgent, AdaptableContext contextObject) {
        this(other.hybridInterpreter, ioAgent, contextObject);
    }


    public @Nullable IStrategoTerm invoke(String strategy, IStrategoTerm input) throws StrategoException {
        hybridInterpreter.setCurrent(input);
        hybridInterpreter.setIOAgent(ioAgent);
        hybridInterpreter.getContext().setContextObject(contextObject);
        hybridInterpreter.getCompiledContext().setContextObject(contextObject);
        try {
            final boolean success = hybridInterpreter.invoke(strategy);
            if(!success) return null;
            return hybridInterpreter.current();
        } catch(InterpreterException e) {
            final ExceptionData exceptionData = handleException(e, strategy);
            throw new StrategoException(exceptionData.message, exceptionData.inner);
        }
    }


    public StrategoRuntime withIoAgent(StrategoIOAgent ioAgent) {
        return new StrategoRuntime(this, ioAgent);
    }

    public StrategoRuntime withContextObject(AdaptableContext contextObject) {
        return new StrategoRuntime(this, contextObject);
    }

    public StrategoRuntime addContextObject(Object contextObject) {
        final AdaptableContext newContextObject = new AdaptableContext(this.contextObject);
        newContextObject.put(contextObject);
        return new StrategoRuntime(this, newContextObject);
    }

    public StrategoRuntime addContextObjects(Object... contextObjects) {
        final AdaptableContext newContextObject = new AdaptableContext(this.contextObject);
        for(Object contextObject : contextObjects) {
            newContextObject.put(contextObject);
        }
        return new StrategoRuntime(this, newContextObject);
    }


    public HybridInterpreter getHybridInterpreter() {
        return hybridInterpreter;
    }

    public ITermFactory getTermFactory() {
        return hybridInterpreter.getFactory();
    }

    public StrategoIOAgent getIoAgent() {
        return ioAgent;
    }

    public AdaptableContext getContextObject() {
        return contextObject;
    }


    private static class ExceptionData {
        final String message;
        final @Nullable Throwable inner;

        ExceptionData(String message, @Nullable Throwable inner) {
            this.message = message;
            this.inner = inner;
        }
    }

    private ExceptionData handleException(InterpreterException ex, String strategy) {
        final String trace = traceToString(hybridInterpreter.getCompiledContext().getTrace());
        try {
            throw ex;
        } catch(InterpreterErrorExit e) {
            final String message;
            final @Nullable IStrategoTerm term = e.getTerm();
            final String innerTrace = e.getTrace() != null ? traceToString(e.getTrace()) : trace;
            if(term != null) {
                final String str = StrategoUtil.toString(term);
                message = StringFormatter.format("Invoking Stratego strategy {} failed at term:\n\t{}\n{}\n{}", strategy, str, innerTrace, e.getMessage());
            } else {
                message = StringFormatter.format("Invoking Stratego strategy {} failed.\n{}\n{}", strategy, innerTrace, e.getMessage());
            }
            return new ExceptionData(message, e);
        } catch(InterpreterExit e) {
            final String message = StringFormatter.format("Invoking Stratego strategy {} failed with exit code {}\n{}\n{}", strategy, e.getValue(), trace, e);
            return new ExceptionData(message, e);
        } catch(UndefinedStrategyException e) {
            final String message = StringFormatter.format("Invoking Stratego strategy {} failed, strategy is undefined\n{}\n{}", strategy, trace, e);
            return new ExceptionData(message, e);
        } catch(InterpreterException e) {
            final String message = StringFormatter.format("Invoking Stratego strategy {} failed unexpectedly\n{}\n{}", strategy, trace, e);
            return new ExceptionData(message, e);
        }
    }

    private static String traceToString(String[] trace) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Stratego trace:");
        final int depth = trace.length;
        for(int i = 0; i < depth; i++) {
            sb.append("\n\t");
            sb.append(trace[depth - i - 1]);
        }
        return sb.toString();
    }

    private static String traceToString(IStrategoList trace) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Stratego trace:");
        final int depth = trace.getSubtermCount();
        for(int i = 0; i < depth; i++) {
            final IStrategoTerm t = trace.getSubterm(depth - i - 1);
            sb.append("\n\t");
            sb.append(t.getTermType() == IStrategoTerm.STRING ? Tools.asJavaString(t) : t);
        }
        return sb.toString();
    }
}
