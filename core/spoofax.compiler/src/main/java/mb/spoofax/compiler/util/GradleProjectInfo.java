package mb.spoofax.compiler.util;

import org.immutables.value.Value;

@Value.Immutable
public interface GradleProjectInfo {
    public static GradleProjectInfo of(GradleProject project, String packageId) {
        return ImmutableGradleProjectInfo.of(project, packageId);
    }

    class Builder extends ImmutableGradleProjectInfo.Builder {}

    static Builder builder() {
        return new Builder();
    }


    @Value.Parameter GradleProject project();

    @Value.Parameter String packageId();

    @Value.Default default String packagePath() {
        return Conversion.packageIdToPath(packageId());
    }
}
