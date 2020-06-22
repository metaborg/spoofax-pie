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
    final ClassloaderResourcesCompiler classloaderResourcesCompiler = new ClassloaderResourcesCompiler(templateCompiler);
    final ParserCompiler parserCompiler = new ParserCompiler(templateCompiler);
    final StylerCompiler stylerCompiler = new StylerCompiler(templateCompiler);
    final CompleterCompiler completerCompiler = new CompleterCompiler(templateCompiler);
    final StrategoRuntimeCompiler strategoRuntimeCompiler = new StrategoRuntimeCompiler(templateCompiler);
    final ConstraintAnalyzerCompiler constraintAnalyzerCompiler = new ConstraintAnalyzerCompiler(templateCompiler);
    final MultilangAnalyzerCompiler multilangAnalyzerCompiler = new MultilangAnalyzerCompiler(templateCompiler);

    final RootProjectCompiler rootProjectCompiler = new RootProjectCompiler(templateCompiler);
    final LanguageProjectCompiler languageProjectCompiler = new LanguageProjectCompiler(templateCompiler, classloaderResourcesCompiler, parserCompiler, stylerCompiler, completerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler, multilangAnalyzerCompiler);
    final AdapterProjectCompiler adapterProjectCompiler = new AdapterProjectCompiler(templateCompiler, parserCompiler, stylerCompiler, completerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler, multilangAnalyzerCompiler);

    final CliProjectCompiler cliProjectCompiler = new CliProjectCompiler(templateCompiler);

    final EclipseExternaldepsProjectCompiler eclipseExternaldepsProjectCompiler = new EclipseExternaldepsProjectCompiler(templateCompiler);
    final EclipseProjectCompiler eclipseProjectCompiler = new EclipseProjectCompiler(templateCompiler);

    final IntellijProjectCompiler intellijProjectCompiler = new IntellijProjectCompiler(templateCompiler);


    LanguageProjectCompiler.Input compileLanguageProject(Shared shared, LanguageProject languageProject) throws IOException {
        final LanguageProjectCompiler.Input input = TigerInputs.languageProjectInput(shared, languageProject).build();
        languageProjectCompiler.compile(input);
        return input;
    }

    AdapterProjectCompiler.Input compileAdapterProject(Shared shared, LanguageProject languageProject, AdapterProject adapterProject) throws IOException {
        final AdapterProjectCompiler.Input input = TigerInputs.adapterProjectInput(shared, languageProject, adapterProject)
            .languageProjectDependency(GradleDependency.project(":" + languageProject.project().coordinate().artifactId()))
            .build();
        TigerInputs.copyTaskDefsIntoAdapterProject(input, resourceService);
        adapterProjectCompiler.compile(input);
        return input;
    }

    AdapterProjectCompiler.Input compileLanguageAndAdapterProject(Shared shared, LanguageProject languageProject, AdapterProject adapterProject) throws IOException {
        compileLanguageProject(shared, languageProject);
        return compileAdapterProject(shared, languageProject, adapterProject);
    }
}
