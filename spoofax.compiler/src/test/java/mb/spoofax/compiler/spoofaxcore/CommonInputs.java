package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;

class CommonInputs {
    static Shared.Builder tigerSharedBuilder(ResourcePath baseDirectory) {
        return Shared.builder()
            .name("Tiger")
            .basePackageId("mb.tiger")
            .baseDirectory(baseDirectory);
    }

    static Shared tigerShared(ResourcePath baseDirectory) {
        return tigerSharedBuilder(baseDirectory).build();
    }


    private static LanguageProjectCompiler.Input.Builder tigerLanguageProjectCompilerInputBuilder(Shared shared) {
        return LanguageProjectCompiler.Input.builder()
            .shared(shared)
            .languageSpecificationDependency(JavaDependency.module(Coordinate.fromGradleNotation("org.metaborg:org.metaborg.lang.tiger:develop-SNAPSHOT")));
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
