package mb.spoofax.compiler.spoofaxcore.util;

import mb.common.util.Preconditions;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.spoofaxcore.*;
import mb.spoofax.compiler.util.JavaDependency;
import mb.spoofax.compiler.util.JavaProject;

public class CommonInputs {
    public static Shared.Builder tigerSharedBuilder(ResourcePath baseDirectory) {
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

    public static Shared tigerShared(ResourcePath baseDirectory) {
        return tigerSharedBuilder(baseDirectory).build();
    }


    public static LanguageProject.Input.Builder tigerLanguageProjectCompilerInputBuilder(Shared shared) {
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

    public static LanguageProject.Input tigerLanguageProjectCompilerInput(Shared shared) {
        return tigerLanguageProjectCompilerInputBuilder(shared).build();
    }


    public static Parser.Input.Builder tigerParserCompilerInputBuilder(Shared shared, JavaProject languageProject) {
        return Parser.Input.builder()
            .shared(shared)
            .languageProject(languageProject);
    }

    public static Parser.Input tigerParserCompilerInput(Shared shared, JavaProject languageProject) {
        return tigerParserCompilerInputBuilder(shared, languageProject).build();
    }


    public static Styler.Input.Builder tigerStylerCompilerInputBuilder(Shared shared, JavaProject languageProject) {
        return Styler.Input.builder()
            .shared(shared)
            .languageProject(languageProject);
    }

    public static Styler.Input tigerStylerCompilerInput(Shared shared, JavaProject languageProject) {
        return tigerStylerCompilerInputBuilder(shared, languageProject).build();
    }


    public static StrategoRuntime.Input.Builder strategoRuntimeBuilderCompilerInputBuilder(Shared shared, JavaProject languageProject) {
        return StrategoRuntime.Input.builder()
            .shared(shared)
            .languageProject(languageProject);
    }

    public static StrategoRuntime.Input strategoRuntimeBuilderCompilerInput(Shared shared, JavaProject languageProject) {
        return strategoRuntimeBuilderCompilerInputBuilder(shared, languageProject).build();
    }


    public static ConstraintAnalyzer.Input.Builder tigerConstraintAnalyzerCompilerInputBuilder(Shared shared, JavaProject languageProject) {
        return ConstraintAnalyzer.Input.builder()
            .shared(shared)
            .languageProject(languageProject);
    }

    public static ConstraintAnalyzer.Input tigerConstraintAnalyzerCompilerInput(Shared shared, JavaProject languageProject) {
        return tigerConstraintAnalyzerCompilerInputBuilder(shared, languageProject).build();
    }
}
