package mb.spoofax.compiler.util;

import mb.resource.hierarchical.ResourcePath;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface GradleProject extends Serializable {
    class Builder extends ImmutableGradleProject.Builder {}

    static Builder builder() {
        return new Builder();
    }


    Coordinate coordinate();

    String packageId();

    @Value.Derived default String packagePath() {
        return Conversion.packageIdToPath(packageId());
    }

    ResourcePath baseDirectory();


    @Value.Default default ResourcePath sourceDirectory() {
        return baseDirectory().appendSegment("src");
    }

    @Value.Default default ResourcePath sourceMainDirectory() {
        return sourceDirectory().appendSegment("main");
    }

    @Value.Default default ResourcePath sourceMainJavaDirectory() {
        return sourceMainDirectory().appendSegment("java");
    }

    @Value.Default default ResourcePath sourceMainResourcesDirectory() {
        return sourceMainDirectory().appendSegment("resources");
    }


    @Value.Default default ResourcePath outputDirectory() {
        return baseDirectory().appendRelativePath("build");
    }


    @Value.Default default ResourcePath genSourceDirectory() {
        return outputDirectory().appendRelativePath("generated/sources");
    }

    @Value.Default default ResourcePath genSourceSpoofaxDirectory() {
        return genSourceDirectory().appendSegment("spoofax");
    }

    @Value.Default default ResourcePath genSourceSpoofaxJavaDirectory() {
        return genSourceSpoofaxDirectory().appendSegment("java");
    }

    @Value.Default default ResourcePath genSourceSpoofaxResourcesDirectory() {
        return genSourceSpoofaxDirectory().appendSegment("resources");
    }

    @Value.Default default ResourcePath genSourceSpoofaxGradleDirectory() {
        return genSourceSpoofaxDirectory().appendSegment("gradle");
    }
}
