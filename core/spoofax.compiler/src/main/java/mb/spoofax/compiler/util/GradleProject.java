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

    ResourcePath baseDirectory();


    @Value.Default default ResourcePath srcDirectory() {
        return baseDirectory().appendSegment("src");
    }

    @Value.Default default ResourcePath srcMainDirectory() {
        return srcDirectory().appendSegment("main");
    }


    @Value.Default default ResourcePath buildDirectory() {
        return baseDirectory().appendRelativePath("build");
    }


    @Value.Default default ResourcePath buildGeneratedSourcesDirectory() {
        return buildDirectory().appendRelativePath("generated/sources");
    }

    @Value.Default default ResourcePath buildGeneratedResourcesDirectory() {
        return buildDirectory().appendRelativePath("generated/resources");
    }


    default GradleDependency asProjectDependency() {
        return GradleDependency.project(":" + coordinate().artifactId());
    }

    default GradleDependency asModuleDependency() {
        return GradleDependency.module(coordinate());
    }
}
