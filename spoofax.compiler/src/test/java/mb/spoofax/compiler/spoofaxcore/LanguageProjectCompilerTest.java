package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.util.ResourceDeps;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageProjectCompilerTest {
    @Test
    void testCompiler() throws IOException {
        final Coordinates coordinates = CommonInputs.tigerCoordinates();
        final DependencyVersions dependencyVersions = DependencyVersions.builder().build();
        final LanguageProjectInput input = LanguageProjectInput.builder()
            .coordinates(coordinates)
            .dependencyVersions(dependencyVersions)
            .spoofaxCoreDependency(GradleDependencies.module("org.metaborg:org.metaborg.lang.tiger:develop-SNAPSHOT"))
            .build();
        final LanguageProjectCompiler compiler = LanguageProjectCompiler.fromClassLoaderResources();
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSResource baseDir = new FSResource(fileSystem.getPath("src/main/java"));
        final Charset charset = StandardCharsets.UTF_8;
        final ResourceDeps resourceDeps = compiler.compile(input, baseDir, charset);
        assertTrue(baseDir.exists());
        final HierarchicalResource buildGradleKtsFile = compiler.getBuildGradleKtsFile(baseDir);
        assertTrue(buildGradleKtsFile.exists());
        assertTrue(resourceDeps.providedResources().contains(buildGradleKtsFile));
        assertTrue(buildGradleKtsFile.readString(charset).contains("mb/tiger"));
    }
}
