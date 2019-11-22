package mb.spoofax.compiler.spoofaxcore.util;

import mb.common.util.ListView;
import mb.common.util.Preconditions;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzer;
import mb.spoofax.compiler.spoofaxcore.LanguageProject;
import mb.spoofax.compiler.spoofaxcore.Parser;
import mb.spoofax.compiler.spoofaxcore.RootProject;
import mb.spoofax.compiler.spoofaxcore.Shared;
import mb.spoofax.compiler.spoofaxcore.StrategoRuntime;
import mb.spoofax.compiler.spoofaxcore.Styler;
import mb.spoofax.compiler.util.JavaDependency;
import mb.spoofax.compiler.util.JavaProject;

public class TigerInputs {
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

    private static JavaDependency fromSystemProperty(String key) {
        return JavaDependency.files(Preconditions.checkNotNull(System.getProperty(key)));
    }

    public static Shared shared(ResourcePath baseDirectory) {
        return sharedBuilder(baseDirectory).build();
    }


    public static LanguageProject.Input.Builder languageProjectBuilder(Shared shared) {
        return LanguageProject.Input.builder()
            .shared(shared)
            .languageSpecificationDependency(JavaDependency.files(Preconditions.checkNotNull(System.getProperty("org.metaborg.lang.tiger:classpath"))))
            .enableStyler(true)
            .enableStrategoTransformations(true)
            .copyStrategoCTree(false)
            .copyStrategoClasses(true)
            .copyStrategoJavaStrategyClasses(true)
            .enableConstraintAnalysis(true)
            .enableNaBL2ConstraintGeneration(true)
            .enableStatixConstraintGeneration(false)
            ;
    }

    public static LanguageProject.Input languageProject(Shared shared) {
        return languageProjectBuilder(shared).build();
    }


    public static RootProject.Input.Builder rootProjectBuilder(Shared shared, String... includedProjects) {
        return RootProject.Input.builder()
            .shared(shared)
            .includedProjects(ListView.of(includedProjects))
            ;
    }

    public static RootProject.Input rootProject(Shared shared, String... includedProjects) {
        return rootProjectBuilder(shared, includedProjects).build();
    }


    public static Parser.Input.Builder parserBuilder(Shared shared, JavaProject languageProject) {
        return Parser.Input.builder()
            .shared(shared)
            .languageProject(languageProject)
            ;
    }

    public static Parser.Input parser(Shared shared, JavaProject languageProject) {
        return parserBuilder(shared, languageProject).build();
    }


    public static Styler.Input.Builder stylerBuilder(Shared shared, JavaProject languageProject) {
        return Styler.Input.builder()
            .shared(shared)
            .languageProject(languageProject)
            ;
    }

    public static Styler.Input styler(Shared shared, JavaProject languageProject) {
        return stylerBuilder(shared, languageProject).build();
    }


    public static StrategoRuntime.Input.Builder strategoRuntimeBuilder(Shared shared, JavaProject languageProject) {
        return StrategoRuntime.Input.builder()
            .shared(shared)
            .languageProject(languageProject)
            ;
    }

    public static StrategoRuntime.Input strategoRuntime(Shared shared, JavaProject languageProject) {
        return strategoRuntimeBuilder(shared, languageProject).build();
    }


    public static ConstraintAnalyzer.Input.Builder constraintAnalyzerBuilder(Shared shared, JavaProject languageProject) {
        return ConstraintAnalyzer.Input.builder()
            .shared(shared)
            .languageProject(languageProject)
            ;
    }

    public static ConstraintAnalyzer.Input constraintAnalyzer(Shared shared, JavaProject languageProject) {
        return constraintAnalyzerBuilder(shared, languageProject).build();
    }
}
