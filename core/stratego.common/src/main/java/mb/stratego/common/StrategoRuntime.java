package mb.stratego.common;

import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import java.util.Optional;

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

    /**
     * Invokes a Stratego strategy with no term arguments,
     * throwing {@link StrategoException} if the strategy fails.
     *
     * @param strategy        the name of the strategy to invoke
     * @param input           the input term
     * @return the resulting term
     * @throws StrategoException if the strategy or its invocation failed
     */
    public IStrategoTerm invoke(String strategy, IStrategoTerm input) throws StrategoException {
        @Nullable final IStrategoTerm result = invokeOrNull(strategy, input);
        if (result == null)
            throw StrategoException.strategyFail(strategy, input, hybridInterpreter.getCompiledContext().getTrace());
        return result;
    }

    /**
     * Invokes a Stratego strategy with the specified term arguments,
     * throwing {@link StrategoException} if the strategy fails.
     *
     * @param strategy        the name of the strategy to invoke
     * @param input           the input term
     * @param arguments       the term arguments
     * @return the resulting term
     * @throws StrategoException if the strategy or its invocation failed
     */
    public IStrategoTerm invoke(String strategy, IStrategoTerm input, ListView<IStrategoTerm> arguments) throws StrategoException {
        @Nullable final IStrategoTerm result = invokeOrNull(strategy, input, arguments);
        if (result == null)
            throw StrategoException.strategyFail(strategy, input, hybridInterpreter.getCompiledContext().getTrace());
        return result;
    }

    /**
     * Invokes a Stratego strategy with the specified term arguments,
     * throwing {@link StrategoException} if the strategy fails.
     *
     * @param strategy        the name of the strategy to invoke
     * @param input           the input term
     * @param arguments       the term arguments
     * @return the resulting term
     * @throws StrategoException if the strategy or its invocation failed
     */
    public IStrategoTerm invoke(String strategy, IStrategoTerm input, IStrategoTerm... arguments) throws StrategoException {
        @Nullable final IStrategoTerm result = invokeOrNull(strategy, input, arguments);
        if (result == null)
            throw StrategoException.strategyFail(strategy, input, hybridInterpreter.getCompiledContext().getTrace());
        return result;
    }

    /**
     * Invokes a Stratego strategy with no term arguments,
     * returning {@link Optional#empty()} if the strategy fails.
     *
     * @param strategy        the name of the strategy to invoke
     * @param input           the input term
     * @return an {@link Optional} with the resulting term; or {@link Optional#empty()} if the strategy failed
     * @throws StrategoException if the strategy invocation failed
     */
    public Optional<IStrategoTerm> tryInvoke(String strategy, IStrategoTerm input) throws StrategoException {
        return Optional.ofNullable(invokeOrNull(strategy, input));
    }

    /**
     * Invokes a Stratego strategy with the specified term arguments,
     * returning {@link Optional#empty()} if the strategy fails.
     *
     * @param strategy        the name of the strategy to invoke
     * @param input           the input term
     * @param arguments       the term arguments
     * @return an {@link Optional} with the resulting term; or {@link Optional#empty()} if the strategy failed
     * @throws StrategoException if the strategy invocation failed
     */
    public Optional<IStrategoTerm> tryInvoke(String strategy, IStrategoTerm input, ListView<IStrategoTerm> arguments) throws StrategoException {
        return Optional.ofNullable(invokeOrNull(strategy, input, arguments));
    }

    /**
     * Invokes a Stratego strategy with the specified term arguments,
     * returning {@link Optional#empty()} if the strategy fails.
     *
     * @param strategy        the name of the strategy to invoke
     * @param input           the input term
     * @param arguments       the term arguments
     * @return an {@link Optional} with the resulting term; or {@link Optional#empty()} if the strategy failed
     * @throws StrategoException if the strategy invocation failed
     */
    public Optional<IStrategoTerm> tryInvoke(String strategy, IStrategoTerm input, IStrategoTerm... arguments) throws StrategoException {
        return Optional.ofNullable(invokeOrNull(strategy, input, arguments));
    }

    /**
     * Invokes a Stratego strategy with no term arguments,
     * returning {@code null} if the strategy fails.
     *
     * @param strategy        the name of the strategy to invoke
     * @param input           the input term
     * @return the resulting term; or {@code null} if the strategy failed
     * @throws StrategoException if the strategy invocation failed
     */
    public @Nullable IStrategoTerm invokeOrNull(String strategy, IStrategoTerm input) throws StrategoException {
        return invokeOrNull(strategy, input, ListView.of());
    }

    /**
     * Invokes a Stratego strategy with the specified term arguments,
     * returning {@code null} if the strategy fails.
     *
     * @param strategy        the name of the strategy to invoke
     * @param input           the input term
     * @param arguments       the term arguments
     * @return the resulting term; or {@code null} if the strategy failed
     * @throws StrategoException if the strategy invocation failed
     */
    public @Nullable IStrategoTerm invokeOrNull(String strategy, IStrategoTerm input, ListView<IStrategoTerm> arguments) throws StrategoException {
        hybridInterpreter.setCurrent(input);
        hybridInterpreter.setIOAgent(ioAgent);
        hybridInterpreter.getContext().setContextObject(contextObject);
        hybridInterpreter.getCompiledContext().setContextObject(contextObject);

        try {
            final boolean success;
            if (arguments.isEmpty()) {
                success = hybridInterpreter.invoke(strategy);
            } else {
                ITermFactory termFactory = getTermFactory();
                IStrategoTerm strategyName = termFactory.makeString(Interpreter.cify(strategy) + "_0_" + arguments.size());
                IStrategoTerm strategyNameTerm = termFactory.makeAppl("SVar", strategyName);
                IStrategoAppl strategyCallTerm = termFactory.makeAppl(
                    "CallT",
                    strategyNameTerm,
                    termFactory.makeList(),
                    termFactory.makeList(arguments.asUnmodifiable())
                );
                success = hybridInterpreter.evaluate(strategyCallTerm);
            }
            if(!success) return null;
            return hybridInterpreter.current();
        } catch(InterpreterException e) {
            throw StrategoException.fromInterpreterException(strategy, input, hybridInterpreter.getCompiledContext().getTrace(), e);
        }
    }

    /**
     * Invokes a Stratego strategy with the specified term arguments,
     * returning {@code null} if the strategy fails.
     *
     * @param strategy        the name of the strategy to invoke
     * @param input           the input term
     * @param arguments       the term arguments
     * @return the resulting term; or {@code null} if the strategy failed
     * @throws StrategoException if the strategy invocation failed
     */
    public @Nullable IStrategoTerm invokeOrNull(String strategy, IStrategoTerm input, IStrategoTerm... arguments) throws StrategoException {
        return invoke(strategy, input, ListView.of(arguments));
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

    public <T> StrategoRuntime addContextObject(Class<T> clazz, T contextObject) {
        final AdaptableContext newContextObject = new AdaptableContext(this.contextObject);
        newContextObject.put(clazz, contextObject);
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
