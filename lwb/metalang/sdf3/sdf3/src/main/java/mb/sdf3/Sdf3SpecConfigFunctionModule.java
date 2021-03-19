package mb.sdf3;

import dagger.Module;
import dagger.Provides;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

@Module
public class Sdf3SpecConfigFunctionModule {
    private final Function<ResourcePath, Result<Option<Sdf3SpecConfig>, ?>> function;

    public Sdf3SpecConfigFunctionModule(Function<ResourcePath, Result<Option<Sdf3SpecConfig>, ?>> function) {
        this.function = function;
    }

    public Sdf3SpecConfigFunctionModule() {
        this(Sdf3DefaultSpecConfigFunction.instance);
    }

    @Provides @Sdf3Scope
    Function<ResourcePath, Result<Option<Sdf3SpecConfig>, ?>> provideFunction() {
        return function;
    }

    private static class Sdf3DefaultSpecConfigFunction implements Function<ResourcePath, Result<Option<Sdf3SpecConfig>, ?>> {
        private static final Sdf3DefaultSpecConfigFunction instance = new Sdf3DefaultSpecConfigFunction();

        private Sdf3DefaultSpecConfigFunction() {}

        @Override public Result<Option<Sdf3SpecConfig>, ?> apply(ExecContext context, ResourcePath root) {
            return Result.ofOk(Option.ofSome(Sdf3SpecConfig.createDefault(root)));
        }

        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() { return 0; }

        private Object readResolve() { return instance; }
    }
}
