package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;
import mb.spoofax.compiler.util.TemplateCompiler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

class TestBase {
    final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
    final FileAssertions fileAssertions = new FileAssertions(resourceService);
    final Charset charset = StandardCharsets.UTF_8;

    final TemplateCompiler templateCompiler = new TemplateCompiler(Shared.class, resourceService, charset);
    final Parser parserCompiler = new Parser(templateCompiler);
    final Styler stylerCompiler = new Styler(templateCompiler);
    final StrategoRuntime strategoRuntimeCompiler = new StrategoRuntime(templateCompiler);
    final ConstraintAnalyzer constraintAnalyzerCompiler = new ConstraintAnalyzer(templateCompiler);

    final RootProject rootProjectCompiler = new RootProject(templateCompiler);
    final LanguageProject languageProjectCompiler = new LanguageProject(templateCompiler, parserCompiler, stylerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler);
    final AdapterProject adapterProjectCompiler = new AdapterProject(templateCompiler, parserCompiler, stylerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler);

    final CliProject cliProjectCompiler = new CliProject(templateCompiler);

    final EclipseExternaldepsProject eclipseExternaldepsProjectCompiler = new EclipseExternaldepsProject(templateCompiler);
    final EclipseProject eclipseProjectCompiler = new EclipseProject(templateCompiler);
}
