package mb.sdf3.task.spoofax;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.spec.Sdf3Config;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@Sdf3Scope
public class Sdf3ConfigSupplierWrapper {
    private @Nullable Supplier<? extends Result<Option<Sdf3Config>, ?>> supplier = null;


    @Inject public Sdf3ConfigSupplierWrapper() {}


    public Supplier<? extends Result<Option<Sdf3Config>, ?>> get() {
        if(supplier == null) {
            supplier = Sdf3DefaultSpecConfigSupplier.instance;
        }
        return supplier;
    }

    public void set(Supplier<? extends Result<Option<Sdf3Config>, ?>> supplier) {
        if(this.supplier != null) {
            throw new IllegalStateException("Supplier in Sdf3ConfigFunctionWrapper was already set or used. After setting or using the supplier, it may not be changed any more to guarantee sound incrementality");
        }
        this.supplier = supplier;
    }


    private static class Sdf3DefaultSpecConfigSupplier implements Supplier<Result<Option<Sdf3Config>, ?>> {
        private static final Sdf3DefaultSpecConfigSupplier instance = new Sdf3DefaultSpecConfigSupplier();

        private Sdf3DefaultSpecConfigSupplier() {}

        @Override
        public Result<Option<Sdf3Config>, ?> get(ExecContext context) {
            return Result.ofOk(Option.ofSome(Sdf3Config.createDefault()));
        }


        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() { return 0; }

        @Override public String toString() { return "Sdf3DefaultSpecConfigFunction()"; }

        private Object readResolve() { return instance; }
    }
}
