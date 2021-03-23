package mb.esv;

import dagger.Module;
import dagger.Provides;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.esv.task.EsvConfig;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

@Module
public class EsvConfigFunctionModule {
    private final Function<ResourcePath, Result<Option<EsvConfig>, ?>> function;

    public EsvConfigFunctionModule(Function<ResourcePath, Result<Option<EsvConfig>, ?>> function) {
        this.function = function;
    }

    public EsvConfigFunctionModule() {
        this(EsvDefaultConfigFunction.instance);
    }


    @Provides @EsvScope
    Function<ResourcePath, Result<Option<EsvConfig>, ?>> provideCheckConfigFunction() {
        return function;
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
