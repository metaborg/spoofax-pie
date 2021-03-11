package mb.sdf3.task.spec;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.Supplier;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;

public class Sdf3Spec implements Serializable {
    public final ParseTableConfiguration parseTableConfig;
    public final Supplier<? extends Result<IStrategoTerm, ?>> mainModuleAstSupplier;
    public final ListView<Supplier<? extends Result<IStrategoTerm, ?>>> modulesAstSuppliers;

    public Sdf3Spec(
        ParseTableConfiguration parseTableConfig,
        Supplier<? extends Result<IStrategoTerm, ?>> mainModuleAstSupplier,
        ListView<Supplier<? extends Result<IStrategoTerm, ?>>> modulesAstSuppliers
    ) {
        this.parseTableConfig = parseTableConfig;
        this.mainModuleAstSupplier = mainModuleAstSupplier;
        this.modulesAstSuppliers = modulesAstSuppliers;
    }

    @SafeVarargs
    public Sdf3Spec(
        ParseTableConfiguration parseTableConfig,
        Supplier<? extends Result<IStrategoTerm, ?>> mainModuleAstSupplier,
        Supplier<? extends Result<IStrategoTerm, ?>>... modulesAstSuppliers
    ) {
        this(parseTableConfig, mainModuleAstSupplier, ListView.of(modulesAstSuppliers));
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Sdf3Spec sdf3Spec = (Sdf3Spec)o;
        if(!parseTableConfig.equals(sdf3Spec.parseTableConfig)) return false;
        if(!mainModuleAstSupplier.equals(sdf3Spec.mainModuleAstSupplier)) return false;
        return modulesAstSuppliers.equals(sdf3Spec.modulesAstSuppliers);
    }

    @Override public int hashCode() {
        int result = parseTableConfig.hashCode();
        result = 31 * result + mainModuleAstSupplier.hashCode();
        result = 31 * result + modulesAstSuppliers.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Sdf3Spec{" +
            "parseTableConfig=" + parseTableConfig +
            ", mainModuleAstSupplier=" + mainModuleAstSupplier +
            ", modulesAstSuppliers=" + modulesAstSuppliers +
            '}';
    }
}
