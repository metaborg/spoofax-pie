package mb.dynamix.task.spoofax;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.dynamix.DynamixScope;
import mb.dynamix.task.DynamixConfig;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@DynamixScope
public class DynamixConfigFunctionWrapper {
    private @Nullable Function<ResourcePath, ? extends Result<Option<DynamixConfig>, ?>> function = null;


    @Inject public DynamixConfigFunctionWrapper() {
    }


    public Function<ResourcePath, ? extends Result<Option<DynamixConfig>, ?>> get() {
        if(function == null) {
            function = DynamixDefaultAnalyzeConfigFunction.instance;
        }
        return function;
    }

    public void set(Function<ResourcePath, ? extends Result<Option<DynamixConfig>, ?>> function) {
        if(this.function != null) {
            throw new IllegalStateException("Function in DynamixDefaultAnalyzeConfigFunction was already set or used. After setting or using the function, it may not be changed any more to guarantee sound incrementality");
        }
        this.function = function;
    }


    private static class DynamixDefaultAnalyzeConfigFunction implements Function<ResourcePath, Result<Option<DynamixConfig>, ?>> {
        private static final DynamixDefaultAnalyzeConfigFunction instance = new DynamixDefaultAnalyzeConfigFunction();

        private DynamixDefaultAnalyzeConfigFunction() {
        }

        @Override
        public Result<Option<DynamixConfig>, ?> apply(ExecContext context, ResourcePath rootDirectory) {
            return Result.ofOk(Option.ofSome(DynamixConfig.createDefault(rootDirectory)));
        }

        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() {
            return 0;
        }

        @Override public String toString() {
            return "DynamixDefaultAnalyzeConfigFunction()";
        }

        private Object readResolve() {
            return instance;
        }
    }
}
