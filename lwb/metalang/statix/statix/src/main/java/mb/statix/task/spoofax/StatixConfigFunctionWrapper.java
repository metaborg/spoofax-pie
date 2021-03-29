package mb.statix.task.spoofax;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixScope;
import mb.statix.task.StatixConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@StatixScope
public class StatixConfigFunctionWrapper {
    private @Nullable Function<ResourcePath, ? extends Result<Option<StatixConfig>, ?>> function = null;


    @Inject public StatixConfigFunctionWrapper() {}


    public Function<ResourcePath, ? extends Result<Option<StatixConfig>, ?>> get() {
        if(function == null) {
            function = StrategoDefaultAnalyzeConfigFunction.instance;
        }
        return function;
    }

    public void set(Function<ResourcePath, ? extends Result<Option<StatixConfig>, ?>> function) {
        if(this.function != null) {
            throw new IllegalStateException("Function in StatixConfigFunctionWrapper was already set or used. After setting or using the function, it may not be changed any more to guarantee sound incrementality");
        }
        this.function = function;
    }


    private static class StrategoDefaultAnalyzeConfigFunction implements Function<ResourcePath, Result<Option<StatixConfig>, ?>> {
        private static final StrategoDefaultAnalyzeConfigFunction instance = new StrategoDefaultAnalyzeConfigFunction();

        private StrategoDefaultAnalyzeConfigFunction() {}

        @Override
        public Result<Option<StatixConfig>, ?> apply(ExecContext context, ResourcePath rootDirectory) {
            return Result.ofOk(Option.ofSome(StatixConfig.createDefault(rootDirectory)));
        }

        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() { return 0; }

        @Override public String toString() { return "StrategoDefaultAnalyzeConfigFunction()"; }

        private Object readResolve() { return instance; }
    }
}
