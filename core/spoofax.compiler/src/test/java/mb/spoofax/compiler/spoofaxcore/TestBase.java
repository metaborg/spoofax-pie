package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.ResourceService;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.TemplateCompiler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

class TestBase {
    final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    final SpoofaxCompilerTestComponent component = DaggerSpoofaxCompilerTestComponent.builder()
        .spoofaxCompilerModule(new SpoofaxCompilerModule(new TemplateCompiler(Shared.class, StandardCharsets.UTF_8)))
        .spoofaxCompilerTestModule(new SpoofaxCompilerTestModule(PieBuilderImpl::new))
        .build();
    final ResourceService resourceService = component.getResourceService();
    final Pie pie = component.getPie();
    final FileAssertions fileAssertions = new FileAssertions(resourceService);

    LanguageProjectCompiler.Input compileLanguageProject(MixedSession session, Shared shared, LanguageProject languageProject) throws ExecException, InterruptedException {
        final LanguageProjectCompiler.Input input = TigerInputs.languageProjectInput(shared, languageProject).build();
        session.require(component.getLanguageProjectCompiler().createTask(input));
        return input;
    }

    AdapterProjectCompiler.Input compileAdapterProject(MixedSession session, Shared shared, LanguageProject languageProject, AdapterProject adapterProject) throws IOException, ExecException, InterruptedException {
        final AdapterProjectCompiler.Input input = TigerInputs.adapterProjectInput(shared, languageProject, adapterProject)
            .languageProjectDependency(GradleDependency.project(":" + languageProject.project().coordinate().artifactId()))
            .build();
        TigerInputs.copyTaskDefsIntoAdapterProject(input, resourceService);
        session.require(component.getAdapterProjectCompiler().createTask(input));
        return input;
    }

    AdapterProjectCompiler.Input compileLanguageAndAdapterProject(MixedSession session, Shared shared, LanguageProject languageProject, AdapterProject adapterProject) throws IOException, ExecException, InterruptedException {
        compileLanguageProject(session, shared, languageProject);
        return compileAdapterProject(session, shared, languageProject, adapterProject);
    }
}
