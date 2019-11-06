package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.Preconditions;
import mb.resource.hierarchical.ResourcePath;

class CommonInputs {
    static Shared.Builder tigerSharedBuilder(ResourcePath baseDirectory) {
        return Shared.builder()
            .name("Tiger")
            .basePackageId("mb.tiger")
            .baseDirectory(baseDirectory)
            .resourceDep(JavaDependency.files(Preconditions.checkNotNull(System.getProperty("resource:classpath"))))
            .commonDep(JavaDependency.files(Preconditions.checkNotNull(System.getProperty("common:classpath"))))
            .jsglr1CommonDep(JavaDependency.files(Preconditions.checkNotNull(System.getProperty("jsglr1.common:classpath"))));
    }

    static Shared tigerShared(ResourcePath baseDirectory) {
        return tigerSharedBuilder(baseDirectory).build();
    }


    private static LanguageProjectCompiler.Input.Builder tigerLanguageProjectCompilerInputBuilder(Shared shared) {
        return LanguageProjectCompiler.Input.builder()
            .shared(shared)
            .languageSpecificationDependency(JavaDependency.files(Preconditions.checkNotNull(System.getProperty("org.metaborg.lang.tiger:classpath"))));
    }

    static LanguageProjectCompiler.Input tigerLanguageProjectCompilerInput(Shared shared) {
        return tigerLanguageProjectCompilerInputBuilder(shared).build();
    }


    static ParserCompiler.Input.Builder tigerParserCompilerInputBuilder(Shared shared, JavaProject languageProject) {
        return ParserCompiler.Input.builder()
            .shared(shared)
            .languageProject(languageProject);
    }

    static ParserCompiler.Input tigerParserCompilerInput(Shared shared, JavaProject languageProject) {
        return tigerParserCompilerInputBuilder(shared, languageProject).build();
    }
}
