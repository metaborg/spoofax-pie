package mb.sdf3.task.spoofax;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@Sdf3Scope
public class Sdf3SpecConfigFunctionWrapper {
    private @Nullable Function<ResourcePath, ? extends Result<Option<Sdf3SpecConfig>, ?>> function = null;


    @Inject public Sdf3SpecConfigFunctionWrapper() {}


    public Function<ResourcePath, ? extends Result<Option<Sdf3SpecConfig>, ?>> get() {
        if(function == null) {
            function = Sdf3DefaultSpecConfigFunction.instance;
        }
        return function;
    }

    public void set(Function<ResourcePath, ? extends Result<Option<Sdf3SpecConfig>, ?>> function) {
        if(this.function != null) {
            throw new IllegalStateException("Function in Sdf3SpecConfigFunctionWrapper was already set or used. After setting or using the function, it may not be changed any more to guarantee sound incrementality");
        }
        this.function = function;
    }


    private static class Sdf3DefaultSpecConfigFunction implements Function<ResourcePath, Result<Option<Sdf3SpecConfig>, ?>> {
        private static final Sdf3DefaultSpecConfigFunction instance = new Sdf3DefaultSpecConfigFunction();

        private Sdf3DefaultSpecConfigFunction() {}


        @Override public Result<Option<Sdf3SpecConfig>, ?> apply(ExecContext context, ResourcePath rootDirectory) {
            return Result.ofOk(Option.ofSome(Sdf3SpecConfig.createDefault(rootDirectory)));
        }


        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() { return 0; }

        @Override public String toString() { return "Sdf3DefaultSpecConfigFunction()"; }

        private Object readResolve() { return instance; }
    }
}
