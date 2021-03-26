package mb.str.task.spoofax;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import mb.str.StrategoScope;
import mb.str.config.StrategoAnalyzeConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@StrategoScope
public class StrategoAnalyzeConfigFunctionWrapper {
    private @Nullable Function<ResourcePath, ? extends Result<Option<StrategoAnalyzeConfig>, ?>> function = null;


    @Inject public StrategoAnalyzeConfigFunctionWrapper() {}


    public Function<ResourcePath, ? extends Result<Option<StrategoAnalyzeConfig>, ?>> get() {
        if(function == null) {
            function = StrategoDefaultAnalyzeConfigFunction.instance;
        }
        return function;
    }

    public void set(Function<ResourcePath, ? extends Result<Option<StrategoAnalyzeConfig>, ?>> function) {
        if(this.function != null) {
            throw new IllegalStateException("Function in StrategoAnalyzeConfigFunctionWrapper was already set or used. After setting or using the function, it may not be changed any more to guarantee sound incrementality");
        }
        this.function = function;
    }


    private static class StrategoDefaultAnalyzeConfigFunction implements mb.pie.api.Function<ResourcePath, Result<Option<StrategoAnalyzeConfig>, ?>> {
        private static final StrategoDefaultAnalyzeConfigFunction instance = new StrategoDefaultAnalyzeConfigFunction();

        private StrategoDefaultAnalyzeConfigFunction() {}

        @Override
        public Result<Option<StrategoAnalyzeConfig>, ?> apply(ExecContext context, ResourcePath rootDirectory) {
            return Result.ofOk(Option.ofSome(StrategoAnalyzeConfig.createDefault(rootDirectory)));
        }

        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() { return 0; }

        @Override public String toString() { return "StrategoDefaultAnalyzeConfigFunction()"; }

        private Object readResolve() { return instance; }
    }
}
