package mb.esv.task.spoofax;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.esv.EsvScope;
import mb.esv.task.EsvConfig;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@EsvScope
public class EsvConfigFunctionWrapper {
    private @Nullable Function<ResourcePath, ? extends Result<Option<EsvConfig>, ?>> function = null;


    @Inject public EsvConfigFunctionWrapper() {}


    public Function<ResourcePath, ? extends Result<Option<EsvConfig>, ?>> get() {
        if(function == null) {
            function = EsvDefaultConfigFunction.instance;
        }
        return function;
    }

    public void set(Function<ResourcePath, ? extends Result<Option<EsvConfig>, ?>> function) {
        if(this.function != null) {
            throw new IllegalStateException("Function in EsvConfigFunctionWrapper was already set or used. After setting or using the function, it may not be changed any more to guarantee sound incrementality");
        }
        this.function = function;
    }


    private static class EsvDefaultConfigFunction implements Function<ResourcePath, Result<Option<EsvConfig>, ?>> {
        private static final EsvDefaultConfigFunction instance = new EsvDefaultConfigFunction();

        private EsvDefaultConfigFunction() {}


        @Override public Result<Option<EsvConfig>, ?> apply(ExecContext context, ResourcePath rootDirectory) {
            return Result.ofOk(Option.ofSome(EsvConfig.createDefault(rootDirectory)));
        }


        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() { return 0; }

        @Override public String toString() { return "EsvDefaultConfigFunction()"; }

        private Object readResolve() { return instance; }
    }
}
