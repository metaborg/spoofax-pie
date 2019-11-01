package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;

public class CommonInputs {
    public static Shared.Builder tigerSharedBuilder(ResourcePath baseDirectory) {
        return Shared.builder()
            .name("Tiger")
            .basePackageId("mb.tiger")
            .baseDirectory(baseDirectory);
    }

    public static Shared tigerShared(ResourcePath baseDirectory) {
        return tigerSharedBuilder(baseDirectory).build();
    }


    public static LanguageProjectCompilerInput.Builder tigerLanguageProjectCompilerInputBuilder(Shared shared) {
        return LanguageProjectCompilerInput.builder()
            .shared(shared)
            .languageSpecificationDependency(JavaDependency.module(Coordinate.fromGradleNotation("org.metaborg:org.metaborg.lang.tiger:develop-SNAPSHOT")));
    }

    public static LanguageProjectCompilerInput tigerLanguageProjectCompilerInput(Shared shared) {
        return tigerLanguageProjectCompilerInputBuilder(shared).build();
    }


    public static ParserCompilerInput.Builder tigerParserCompilerInputBuilder(Shared shared, JavaProject languageProject) {
        return ParserCompilerInput.builder()
            .shared(shared)
            .languageProject(languageProject);
    }

    public static ParserCompilerInput tigerParserCompilerInput(Shared shared, JavaProject languageProject) {
        return tigerParserCompilerInputBuilder(shared, languageProject).build();
    }
}
