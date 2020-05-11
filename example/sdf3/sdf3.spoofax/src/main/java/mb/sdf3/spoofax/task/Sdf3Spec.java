package mb.sdf3.spoofax.task;

import mb.common.util.ListView;
import mb.pie.api.Supplier;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.Objects;

public class Sdf3Spec implements Serializable {
    public final Supplier<@Nullable IStrategoTerm> mainModuleAstSupplier;
    public final ListView<Supplier<@Nullable IStrategoTerm>> modulesAstSuppliers;

    public Sdf3Spec(Supplier<@Nullable IStrategoTerm> mainModuleAstSupplier, ListView<Supplier<@Nullable IStrategoTerm>> modulesAstSuppliers) {
        this.mainModuleAstSupplier = mainModuleAstSupplier;
        this.modulesAstSuppliers = modulesAstSuppliers;
    }

    @SafeVarargs
    public Sdf3Spec(Supplier<@Nullable IStrategoTerm> mainModuleAstSupplier, Supplier<@Nullable IStrategoTerm>... modulesAstSuppliers) {
        this(mainModuleAstSupplier, ListView.of(modulesAstSuppliers));
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Sdf3Spec sdf3Spec = (Sdf3Spec)o;
        return mainModuleAstSupplier.equals(sdf3Spec.mainModuleAstSupplier) &&
            modulesAstSuppliers.equals(sdf3Spec.modulesAstSuppliers);
    }

    @Override public int hashCode() {
        return Objects.hash(mainModuleAstSupplier, modulesAstSuppliers);
    }

    @Override public String toString() {
        return "Sdf3Spec{" +
            "mainModuleAstSupplier=" + mainModuleAstSupplier +
            ", modulesAstSuppliers=" + modulesAstSuppliers +
            '}';
    }
}
