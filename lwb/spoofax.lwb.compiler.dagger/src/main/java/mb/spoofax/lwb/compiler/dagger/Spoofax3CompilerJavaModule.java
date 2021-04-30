package mb.spoofax.lwb.compiler.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.pie.task.java.CompileJava;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.lwb.compiler.CheckLanguageSpecification;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageSpecification;
import mb.spoofax.lwb.compiler.esv.CheckEsv;
import mb.spoofax.lwb.compiler.esv.CompileEsv;
import mb.spoofax.lwb.compiler.esv.ConfigureEsv;
import mb.spoofax.lwb.compiler.sdf3.CheckSdf3;
import mb.spoofax.lwb.compiler.sdf3.CompileSdf3;
import mb.spoofax.lwb.compiler.sdf3.ConfigureSdf3;
import mb.spoofax.lwb.compiler.statix.CheckStatix;
import mb.spoofax.lwb.compiler.statix.CompileStatix;
import mb.spoofax.lwb.compiler.statix.ConfigureStatix;
import mb.spoofax.lwb.compiler.stratego.CheckStratego;
import mb.spoofax.lwb.compiler.stratego.CompileStratego;
import mb.spoofax.lwb.compiler.stratego.ConfigureStratego;

import java.util.HashSet;
import java.util.Set;

@Module
public abstract class Spoofax3CompilerJavaModule {
    @Provides @Spoofax3CompilerScope
    static CompileJava provideCompileJava() {
        return new CompileJava();
    }
}
