package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

class TestBase {
    final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
    final FileAssertions fileAssertions = new FileAssertions(resourceService);
    final Charset charset = StandardCharsets.UTF_8;

    final Parser parserCompiler = Parser.fromClassLoaderResources(resourceService, charset);
    final Styler stylerCompiler = Styler.fromClassLoaderResources(resourceService, charset);
    final StrategoRuntime strategoRuntimeCompiler = StrategoRuntime.fromClassLoaderResources(resourceService, charset);
    final ConstraintAnalyzer constraintAnalyzerCompiler = ConstraintAnalyzer.fromClassLoaderResources(resourceService, charset);

    final RootProject rootProjectCompiler = RootProject.fromClassLoaderResources(resourceService, charset);
}
