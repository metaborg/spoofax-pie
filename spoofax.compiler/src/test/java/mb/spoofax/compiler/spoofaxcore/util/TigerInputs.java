package mb.spoofax.compiler.spoofaxcore.util;

import mb.common.util.ListView;
import mb.common.util.Preconditions;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.spoofaxcore.AdapterProject;
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzer;
import mb.spoofax.compiler.spoofaxcore.LanguageProject;
import mb.spoofax.compiler.spoofaxcore.Parser;
import mb.spoofax.compiler.spoofaxcore.RootProject;
import mb.spoofax.compiler.spoofaxcore.Shared;
import mb.spoofax.compiler.spoofaxcore.StrategoRuntime;
import mb.spoofax.compiler.spoofaxcore.Styler;
import mb.spoofax.compiler.util.GradleDependency;

public class TigerInputs {
    /// Shared input

    public static Shared.Builder sharedBuilder(ResourcePath baseDirectory) {
        return Shared.builder()
            .name("Tiger")
            .basePackageId("mb.tiger")
            .baseDirectory(baseDirectory)
            .logApiDep(fromSystemProperty("log.api:classpath"))
            .resourceDep(fromSystemProperty("resource:classpath"))
            .spoofaxCompilerInterfacesDep(fromSystemProperty("spoofax.compiler.interfaces:classpath"))
            .commonDep(fromSystemProperty("common:classpath"))
            .jsglr1CommonDep(fromSystemProperty("jsglr1.common:classpath"))
            .esvCommonDep(fromSystemProperty("esv.common:classpath"))
            .strategoCommonDep(fromSystemProperty("stratego.common:classpath"))
            .constraintCommonDep(fromSystemProperty("constraint.common:classpath"))
            .nabl2CommonDep(fromSystemProperty("nabl2.common:classpath"))
            .statixCommonDep(fromSystemProperty("statix.common:classpath"))
            ;
    }

    private static GradleDependency fromSystemProperty(String key) {
        return GradleDependency.files(Preconditions.checkNotNull(System.getProperty(key)));
    }

    public static Shared shared(ResourcePath baseDirectory) {
        return sharedBuilder(baseDirectory).build();
    }


    /// Parser compiler input

    public static Parser.Input.Builder parserBuilder(Shared shared) {
        return Parser.Input.builder()
            .shared(shared)
            ;
    }

    public static Parser.Input parser(Shared shared) {
        return parserBuilder(shared).build();
    }


    /// Styler compiler input

    public static Styler.Input.Builder stylerBuilder(Shared shared) {
        return Styler.Input.builder()
            .shared(shared)
            ;
    }

    public static Styler.Input styler(Shared shared) {
        return stylerBuilder(shared).build();
    }


    /// Stratego runtime builder compiler input

    public static StrategoRuntime.Input.Builder strategoRuntimeBuilder(Shared shared) {
        return StrategoRuntime.Input.builder()
            .shared(shared)
            .addInteropRegisterersByReflection("org.metaborg.lang.tiger.trans.InteropRegisterer", "org.metaborg.lang.tiger.strategies.InteropRegisterer")
            .addNaBL2Primitives(true)
            .addStatixPrimitives(false)
            .copyJavaStrategyClasses(true)
            ;
    }

    public static StrategoRuntime.Input strategoRuntime(Shared shared) {
        return strategoRuntimeBuilder(shared).build();
    }


    /// Constraint analyzer compiler input

    public static ConstraintAnalyzer.Input.Builder constraintAnalyzerBuilder(Shared shared) {
        return ConstraintAnalyzer.Input.builder()
            .shared(shared)
            ;
    }

    public static ConstraintAnalyzer.Input constraintAnalyzer(Shared shared) {
        return constraintAnalyzerBuilder(shared).build();
    }


    /// Language project compiler input

    public static LanguageProject.Input.Builder languageProjectBuilder(Shared shared) {
        return LanguageProject.Input.builder()
            .shared(shared)
            .parser(parser(shared))
            .styler(styler(shared))
            .strategoRuntime(strategoRuntime(shared))
            .constraintAnalyzer(constraintAnalyzer(shared))
            .languageSpecificationDependency(GradleDependency.files(Preconditions.checkNotNull(System.getProperty("org.metaborg.lang.tiger:classpath"))))
            ;
    }

    public static LanguageProject.Input languageProject(Shared shared) {
        return languageProjectBuilder(shared).build();
    }


    /// Adapter project compiler input

    public static AdapterProject.Input.Builder adapterProjectBuilder(Shared shared) {
        return AdapterProject.Input.builder()
            .shared(shared)
            ;
    }

    public static AdapterProject.Input adapterProject(Shared shared) {
        return adapterProjectBuilder(shared).build();
    }


    /// Root project compiler input

    public static RootProject.Input.Builder rootProjectBuilder(Shared shared) {
        return RootProject.Input.builder()
            .shared(shared)
            ;
    }

    public static RootProject.Input rootProject(Shared shared) {
        return rootProjectBuilder(shared).build();
    }
}
