package mb.constraint.common;

import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.spoofax.terms.AbstractSimpleTerm;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.util.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class StrategoResourceKey extends AbstractSimpleTerm implements IStrategoTerm {
    private static final long serialVersionUID = 1L;

    public final ResourceKey value;

    public StrategoResourceKey(ResourceKey obj) {
        this.value = obj;
    }

    @Override public boolean isList() {
        return false;
    }

    @Override public Iterator<IStrategoTerm> iterator() {
        return new EmptyIterator<>();
    }

    @Override public int getSubtermCount() {
        return 0;
    }

    @Override public IStrategoTerm getSubterm(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override public IStrategoTerm[] getAllSubterms() {
        return new IStrategoTerm[0];
    }

    @Override public int getTermType() {
        return IStrategoTerm.BLOB;
    }

    @Override public int getStorageType() {
        return IStrategoTerm.IMMUTABLE;
    }

    @SuppressWarnings("deprecation") @Override public IStrategoList getAnnotations() {
        return AbstractTermFactory.EMPTY_LIST;
    }

    @Override public boolean match(@NonNull IStrategoTerm second) {
        return equals(second);
    }

    @Override public void prettyPrint(@NonNull ITermPrinter pp) {
        pp.print(toString());
    }

    @Override public String toString(int maxDepth) {
        return toString();
    }

    @Override public void writeAsString(@NonNull Appendable output, int maxDepth) throws IOException {
        output.append(toString());
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StrategoResourceKey that = (StrategoResourceKey) o;
        return value.equals(that.value);
    }

    @Override public int hashCode() {
        return value.hashCode();
    }

    @Override public String toString() {
        return value.toString();
    }
}
