package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.TemplateCompiler;

import java.io.IOException;
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

    final IntellijProject intellijProjectCompiler = new IntellijProject(templateCompiler);


    LanguageProject.Input compileLanguageProject(Shared shared) throws IOException {
        final LanguageProject.Input input = TigerInputs.languageProject(shared);
        languageProjectCompiler.generateInitial(input);
        languageProjectCompiler.generateGradleFiles(input);
        languageProjectCompiler.compile(input);
        return input;
    }

    AdapterProject.Input compileAdapterProject(Shared shared) throws IOException {
        final AdapterProject.Input input = TigerInputs.adapterProjectBuilder(shared)
            .languageProjectDependency(GradleDependency.project(":" + shared.languageProject().coordinate().artifactId()))
            .build();
        TigerInputs.copyTaskDefsIntoAdapterProject(input, resourceService);
        adapterProjectCompiler.generateInitial(input);
        adapterProjectCompiler.generateBuildGradleKts(input);
        adapterProjectCompiler.compile(input);
        return input;
    }

    AdapterProject.Input compileLanguageAndAdapterProject(Shared shared) throws IOException {
        compileLanguageProject(shared);
        return compileAdapterProject(shared);
    }
}
