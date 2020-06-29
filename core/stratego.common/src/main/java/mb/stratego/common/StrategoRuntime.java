package mb.stratego.common;

import org.spoofax.interpreter.core.InterpreterException;
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


    public IStrategoTerm invoke(String strategy, IStrategoTerm input) throws StrategoException {
        hybridInterpreter.setCurrent(input);
        hybridInterpreter.setIOAgent(ioAgent);
        hybridInterpreter.getContext().setContextObject(contextObject);
        hybridInterpreter.getCompiledContext().setContextObject(contextObject);
        try {
            final boolean success = hybridInterpreter.invoke(strategy);
            if(!success) {
                throw StrategoException.strategyFail(strategy, input, hybridInterpreter.getCompiledContext().getTrace());
            }
            return hybridInterpreter.current();
        } catch(InterpreterException e) {
            throw StrategoException.fromInterpreterException(strategy, input, hybridInterpreter.getCompiledContext().getTrace(), e);
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
}
