package mb.spoofax.runtime.stratego;

import com.google.common.collect.Iterables;
import mb.fs.java.JavaFSPath;
import mb.spoofax.api.SpoofaxEx;
import mb.spoofax.runtime.util.MessageFormatter;
import org.spoofax.interpreter.core.*;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.terms.*;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.IncompatibleJarException;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class StrategoRuntime {
    private final HybridInterpreter hybridInterpreter;


    public StrategoRuntime(ITermFactory termFactory, Iterable<String> components, Iterable<IOperatorRegistry> libraries,
        Iterable<JavaFSPath> ctrees, Iterable<URI> jars, @Nullable ClassLoader jarClassLoader) throws SpoofaxEx {
        final HybridInterpreter hybridInterpreter = new HybridInterpreter(termFactory);
        for(String component : components) {
            hybridInterpreter.getCompiledContext().registerComponent(component);
        }

        for(IOperatorRegistry library : libraries) {
            hybridInterpreter.getCompiledContext().addOperatorRegistry(library);
        }

        for(JavaFSPath file : ctrees) {
            try {
                hybridInterpreter.load(new BufferedInputStream(file.toNode().newInputStream()));
            } catch(IOException | InterpreterException e) {
                throw new SpoofaxEx("Failed to create Stratego runtime; failed to load CTree file " + file, e);
            }
        }

        final int numJars = Iterables.size(jars);
        if(numJars > 0) {
            try {
                final URL[] classpath = new URL[numJars];
                int i = 0;
                for(URI uri : jars) {
                    classpath[i] = uri.toURL();
                    ++i;
                }
                hybridInterpreter.loadJars(jarClassLoader, classpath);
            } catch(IncompatibleJarException | IOException e) {
                throw new SpoofaxEx(
                    "Failed to create Stratego runtime; failed to load one or more JAR files from " + jars, e);
            }
        }

        hybridInterpreter.getCompiledContext().getExceptionHandler().setEnabled(false);
        hybridInterpreter.init();

        this.hybridInterpreter = hybridInterpreter;
    }


    public @Nullable IStrategoTerm invoke(String strategy, IStrategoTerm input, IOAgent ioAgent) throws SpoofaxEx {
        return invoke(strategy, input, ioAgent, null);
    }

    public @Nullable IStrategoTerm invoke(String strategy, IStrategoTerm input, IOAgent ioAgent,
        @Nullable Object contextObject) throws SpoofaxEx {
        hybridInterpreter.setCurrent(input);
        hybridInterpreter.setIOAgent(ioAgent);
        hybridInterpreter.getContext().setContextObject(contextObject);
        hybridInterpreter.getCompiledContext().setContextObject(contextObject);

        try {
            final boolean success = hybridInterpreter.invoke(strategy);
            if(!success)
                return null;
            return hybridInterpreter.current();
        } catch(InterpreterException e) {
            final ExceptionData exceptionData = handleException(e, strategy);
            throw new SpoofaxEx(exceptionData.message, exceptionData.inner);
        }
    }


    private class ExceptionData {
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
            final IStrategoTerm term = e.getTerm();
            final String innerTrace = e.getTrace() != null ? traceToString(e.getTrace()) : trace;
            if(term != null) {
                final String termString;
                final IStrategoString ppTerm = StrategoUtils.prettyPrintTerm(term);
                if(ppTerm != null) {
                    termString = ppTerm.stringValue();
                } else {
                    termString = term.toString();
                }
                message = MessageFormatter.format("Invoking Stratego strategy {} failed at term:\n\t{}\n{}\n{}",
                    strategy, termString, innerTrace, e.getMessage());
            } else {
                message = MessageFormatter.format("Invoking Stratego strategy {} failed.\n{}\n{}", strategy, innerTrace,
                    e.getMessage());
            }
            return new ExceptionData(message, e);
        } catch(InterpreterExit e) {
            final String message = MessageFormatter.format(
                "Invoking Stratego strategy {} failed with exit code {}\n{}\n{}", strategy, e.getValue(), trace, e);
            return new ExceptionData(message, e);
        } catch(UndefinedStrategyException e) {
            final String message = MessageFormatter
                .format("Invoking Stratego strategy {} failed, strategy is undefined\n{}\n{}", strategy, trace, e);
            return new ExceptionData(message, e);
        } catch(InterpreterException e) {
            final Throwable cause = e.getCause();
            if(cause != null && cause instanceof InterpreterException) {
                return handleException((InterpreterException) cause, strategy);
            } else {
                final String message = MessageFormatter
                    .format("Invoking Stratego strategy {} failed unexpectedly\n{}\n{}", strategy, trace, e);
                return new ExceptionData(message, null);
            }
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


    public ITermFactory termFactory() {
        return hybridInterpreter.getFactory();
    }

    public HybridInterpreter getHybridInterpreter() {
        return hybridInterpreter;
    }
}
