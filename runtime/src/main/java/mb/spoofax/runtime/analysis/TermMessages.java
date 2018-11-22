package mb.spoofax.runtime.analysis;

import mb.fs.java.JavaFSPath;
import mb.spoofax.api.message.*;
import mb.spoofax.api.region.Region;
import mb.spoofax.runtime.term.TermOrigin;
import org.spoofax.interpreter.terms.*;
import org.spoofax.terms.Term;
import org.spoofax.terms.TermVisitor;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class TermMessages {
    public static void addMessagesFromResultTerm(MessageCollection messages, IStrategoTerm messagesTerm, Severity severity) {
        for(IStrategoTerm term : messagesTerm.getAllSubterms()) {
            final IStrategoTerm originTerm;
            final String text;
            if(term.getSubtermCount() == 2) {
                originTerm = term.getSubterm(0);
                text = termMessageToString(term.getSubterm(1));
            } else {
                originTerm = term;
                text = termMessageToString(term) + " (no tree node indicated)";
            }
            final @Nullable Region region;
            final @Nullable JavaFSPath path;
            if(originTerm != null) {
                region = TermOrigin.region(originTerm);
                path = TermOrigin.sourcePath(originTerm);
            } else {
                region = null;
                path = null;
            }
            final Message message = new Message(text, severity, region);
            if(path == null) {
                messages.addGlobalMessage(message);
            } else if(path.toNode().isFile()) {
                messages.addDocumentMessage(path.toString(), message);
            } else {
                messages.addContainerMessage(path.toString(), message);
            }
        }
    }

    public static ArrayList<Message> ambiguityMessagesFromAst(IStrategoTerm ast, Severity severity) {
        final ArrayList<Message> messages = new ArrayList<>();
        final TermVisitor termVisitor = new TermVisitor() {
            private @Nullable IStrategoTerm ambStart;

            @Override public void preVisit(IStrategoTerm term) {
                if(ambStart == null && "amb".equals(Term.tryGetName(term))) {
                    final String text = "Fragment is ambiguous: " + ambToString(term);
                    final Region region = TermOrigin.region(term);
                    messages.add(new Message(text, severity, region));
                    ambStart = term;
                }
            }

            @Override public void postVisit(IStrategoTerm term) {
                if(term == ambStart) {
                    ambStart = null;
                }
            }

            private String ambToString(IStrategoTerm amb) {
                final String result = amb.toString();
                return result.length() > 5000 ? result.substring(0, 5000) + "..." : result;
            }
        };
        termVisitor.visit(ast);
        return messages;
    }


    private static String termMessageToString(IStrategoTerm term) {
        if(term instanceof IStrategoString) {
            final IStrategoString messageStringTerm = (IStrategoString) term;
            return messageStringTerm.stringValue();
        } else if(term instanceof IStrategoList) {
            final StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(IStrategoTerm subterm : term) {
                if(!first) {
                    sb.append(' ');
                }
                sb.append(termMessageToString(subterm));
                first = false;
            }
            return sb.toString();
        } else {
            return term.toString();
        }
    }
}
