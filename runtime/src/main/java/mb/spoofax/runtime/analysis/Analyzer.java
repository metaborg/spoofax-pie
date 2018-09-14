package mb.spoofax.runtime.analysis;

import mb.pie.vfs.path.PPath;
import mb.spoofax.api.SpoofaxEx;
import mb.spoofax.api.message.*;
import mb.spoofax.runtime.nabl.ScopeGraphPrimitiveLibrary;
import mb.spoofax.runtime.stratego.StrategoRuntime;
import mb.spoofax.runtime.stratego.StrategoRuntimeBuilder;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.*;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

public class Analyzer implements Serializable {
    private static final long serialVersionUID = 1L;

    public final PPath strategoCtree;
    public final String strategyName;


    public Analyzer(PPath strategoCtree, String strategyName) {
        this.strategoCtree = strategoCtree;
        this.strategyName = strategyName;
    }


    public StrategoRuntime createSuitableRuntime(StrategoRuntimeBuilder strategoRuntimeBuilder, ScopeGraphPrimitiveLibrary primitiveLibrary) throws SpoofaxEx {
        strategoRuntimeBuilder.addCtree(strategoCtree);
        strategoRuntimeBuilder.addLibrary(primitiveLibrary);
        final StrategoRuntime runtime = strategoRuntimeBuilder.build();
        return runtime;
    }


    public static class ContainerOutput implements Serializable {
        private static final long serialVersionUID = 1L;

        public final IStrategoTerm analysis;
        public final PPath source;

        public ContainerOutput(IStrategoTerm analysis, PPath source) {
            this.analysis = analysis;
            this.source = source;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final ContainerOutput that = (ContainerOutput) o;
            if(!analysis.equals(that.analysis)) return false;
            return source.equals(that.source);
        }

        @Override public int hashCode() {
            int result = analysis.hashCode();
            result = 31 * result + source.hashCode();
            return result;
        }

        @Override public String toString() {
            return source.toString();
        }
    }

    public ContainerOutput analyzeContainer(PPath source, StrategoRuntime runtime) throws SpoofaxEx {
        final IOAgent ioAgent = new IOAgent();
        final ITermFactory termFactory = runtime.termFactory();
        final IStrategoTerm containerTerm = termFactory.makeString(source.toString());
        final IStrategoTerm action = build(termFactory, "AnalyzeInitial", containerTerm);
        final List<IStrategoTerm> result = match(runtime.invoke(strategyName, action, ioAgent), "InitialResult", 1);
        if(result == null) {
            throw new SpoofaxEx("Initial container analysis failed");
        }
        final IStrategoTerm analysis = result.get(0);
        return new ContainerOutput(analysis, source);
    }


    public static class DocumentOutput implements Serializable {
        private static final long serialVersionUID = 1L;

        public final IStrategoTerm ast;
        public final IStrategoTerm analysis;
        public final ArrayList<Message> messages;
        public final PPath source;

        public DocumentOutput(IStrategoTerm ast, IStrategoTerm analysis, ArrayList<Message> messages, PPath source) {
            this.ast = ast;
            this.analysis = analysis;
            this.messages = messages;
            this.source = source;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final DocumentOutput that = (DocumentOutput) o;
            if(!ast.equals(that.ast)) return false;
            if(!analysis.equals(that.analysis)) return false;
            if(!messages.equals(that.messages)) return false;
            return source.equals(that.source);
        }

        @Override public int hashCode() {
            int result = ast.hashCode();
            result = 31 * result + analysis.hashCode();
            result = 31 * result + messages.hashCode();
            result = 31 * result + source.hashCode();
            return result;
        }

        @Override public String toString() {
            return ast.toString();
        }
    }

    public DocumentOutput analyzeDocument(IStrategoTerm ast, PPath source, ContainerOutput containerOutput, StrategoRuntime runtime)
        throws SpoofaxEx {
        final IOAgent ioAgent = new IOAgent();
        final ITermFactory termFactory = runtime.termFactory();
        final IStrategoTerm sourceTerm = termFactory.makeString(source.toString());
        final IStrategoTerm action = build(termFactory, "AnalyzeUnit", sourceTerm, ast, containerOutput.analysis);
        final List<IStrategoTerm> result = match(runtime.invoke(strategyName, action, ioAgent), "UnitResult", 2);
        if(result == null) {
            throw new SpoofaxEx("Analysis of document " + source + " failed");
        }
        final IStrategoTerm analyzedAST = result.get(0);
        final IStrategoTerm analysis = result.get(1);
        final ArrayList<Message> messages = TermMessages.ambiguityMessagesFromAst(ast, Severity.Warn);
        return new DocumentOutput(analyzedAST, analysis, messages, source);
    }


    public static class FinalOutput implements Serializable {
        private static final long serialVersionUID = 1L;

        public final IStrategoTerm analysis;
        public final MessageCollection messages;

        public FinalOutput(IStrategoTerm analysis, MessageCollection messages) {
            this.analysis = analysis;
            this.messages = messages;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final FinalOutput that = (FinalOutput) o;
            if(!analysis.equals(that.analysis)) return false;
            return messages.equals(that.messages);
        }

        @Override public int hashCode() {
            int result = analysis.hashCode();
            result = 31 * result + messages.hashCode();
            return result;
        }

        @Override public String toString() {
            return messages.toString();
        }
    }

    public FinalOutput analyzeFinal(ContainerOutput containerOutput, Collection<DocumentOutput> documentOutputs, StrategoRuntime runtime) throws SpoofaxEx {
        final IOAgent ioAgent = new IOAgent();
        final ITermFactory termFactory = runtime.termFactory();
        final IStrategoTerm containerTerm = termFactory.makeString(containerOutput.source.toString());
        final ArrayList<IStrategoTerm> documentAnalysisTerms = new ArrayList<>();
        final MessageCollection messages = new MessageCollection();
        for(DocumentOutput documentOutput : documentOutputs) {
            documentAnalysisTerms.add(documentOutput.analysis);
            messages.addDocumentMessages(documentOutput.source, documentOutput.messages);
        }
        final IStrategoList documentAnalysisList = termFactory.makeList(documentAnalysisTerms);
        final IStrategoTerm action = build(termFactory, "AnalyzeFinal", containerTerm, containerOutput.analysis, documentAnalysisList);
        final List<IStrategoTerm> result = match(runtime.invoke(strategyName, action, ioAgent), "FinalResult", 4);
        if(result == null) {
            throw new SpoofaxEx("Final analysis failed");
        }

        // Result term = (analysis, errors, warnings, notes)
        final IStrategoTerm analysis = result.get(0);
        TermMessages.addMessagesFromResultTerm(messages, result.get(1), Severity.Error);
        TermMessages.addMessagesFromResultTerm(messages, result.get(2), Severity.Warn);
        TermMessages.addMessagesFromResultTerm(messages, result.get(3), Severity.Info);

        return new FinalOutput(analysis, messages);
    }


    private IStrategoTerm build(ITermFactory termFactory, String op, IStrategoTerm... subterms) {
        return termFactory.makeAppl(termFactory.makeConstructor(op, subterms.length), subterms);
    }

    private @Nullable List<IStrategoTerm> match(IStrategoTerm term, String op, int n) {
        if(term == null || !Tools.isTermAppl(term) || !Tools.hasConstructor((IStrategoAppl) term, op, n)) {
            return null;
        }
        return Arrays.asList(term.getAllSubterms());
    }
}
