package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

class TestBase {
    final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    final SpoofaxCompilerTestComponent component = DaggerSpoofaxCompilerTestComponent.builder()
        .spoofaxCompilerModule(new SpoofaxCompilerModule(new TemplateCompiler(StandardCharsets.UTF_8)))
        .spoofaxCompilerTestModule(new SpoofaxCompilerTestModule(PieBuilderImpl::new))
        .build();
    final ResourceService resourceService = component.getResourceService();
    final Pie pie = component.getPie();
    final FileAssertions fileAssertions = new FileAssertions(resourceService);


    ResourcePath defaultRootDirectory() {
        return new FSPath(fileSystem.getPath("repo"));
    }

    TigerInputs defaultInputs() {
        return new TigerInputs(defaultRootDirectory());
    }

    TigerInputs defaultInputs(Shared shared) {
        return new TigerInputs(defaultRootDirectory(), shared);
    }


    LanguageProjectCompiler.Input compileLanguageProject(MixedSession session, TigerInputs inputs) throws ExecException, InterruptedException {
        final LanguageProjectCompiler.Input input = inputs.languageProjectCompilerInput();
        session.require(component.getLanguageProjectCompiler().createTask(input));
        return input;
    }

    AdapterProjectCompiler.Input compileAdapterProject(MixedSession session, TigerInputs inputs) throws ExecException, InterruptedException {
        final AdapterProjectCompiler.Input input = inputs.adapterProjectCompilerInput();
        session.require(component.getAdapterProjectCompiler().createTask(input));
        return input;
    }

    AdapterProjectCompiler.Input compileLanguageAndAdapterProject(MixedSession session, TigerInputs inputs) throws ExecException, InterruptedException {
        compileLanguageProject(session, inputs);
        return compileAdapterProject(session, inputs);
    }
}