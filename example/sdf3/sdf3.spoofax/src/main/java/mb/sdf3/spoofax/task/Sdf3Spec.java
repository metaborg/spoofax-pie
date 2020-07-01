package mb.sdf3.spoofax.task;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.Supplier;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.Objects;

public class Sdf3Spec implements Serializable {
    public final Supplier<? extends Result<IStrategoTerm, ?>> mainModuleAstSupplier;
    public final ListView<Supplier<? extends Result<IStrategoTerm, ?>>> modulesAstSuppliers;

    public Sdf3Spec(Supplier<? extends Result<IStrategoTerm, ?>> mainModuleAstSupplier, ListView<Supplier<? extends Result<IStrategoTerm, ?>>> modulesAstSuppliers) {
        this.mainModuleAstSupplier = mainModuleAstSupplier;
        this.modulesAstSuppliers = modulesAstSuppliers;
    }

    @SafeVarargs
    public Sdf3Spec(Supplier<? extends Result<IStrategoTerm, ?>> mainModuleAstSupplier, Supplier<? extends Result<IStrategoTerm, ?>>... modulesAstSuppliers) {
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
