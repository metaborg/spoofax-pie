package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.ClassKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ParserCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared().build();
        final LanguageProject languageProject = TigerInputs.languageProject(baseDirectory, shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(baseDirectory, shared).build();

        try(MixedSession session = pie.newSession()) {
            final ParserLanguageCompiler.Input languageProjectInput = TigerInputs.parserLanguageProjectInput(shared, languageProject).build();
            session.require(component.getParserLanguageCompiler().createTask(languageProjectInput));
            fileAssertions.scopedExists(languageProjectInput.classesGenDirectory(), (s) -> {
                s.assertPublicJavaClass(languageProjectInput.genTable(), "TigerParseTable");
                s.assertPublicJavaClass(languageProjectInput.genParser(), "TigerParser");
                s.assertPublicJavaClass(languageProjectInput.genFactory(), "TigerParserFactory");
            });

            final ParserAdapterCompiler.Input adapterProjectInput = TigerInputs.parserAdapterProjectInput(shared, languageProject, adapterProject).build();
            session.require(component.getParserAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedExists(adapterProjectInput.classesGenDirectory(), (s) -> {
                s.assertPublicJavaClass(adapterProjectInput.genParseTaskDef(), "TigerParse");
                s.assertPublicJavaClass(adapterProjectInput.tokenizeTaskDef(), "TigerTokenize");
            });
        }
    }

    @Test void testCompilerManual() throws Exception {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared().build();
        final LanguageProject languageProject = TigerInputs.languageProject(baseDirectory, shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(baseDirectory, shared).build();

        try(MixedSession session = pie.newSession()) {
            final ParserLanguageCompiler.Input languageProjectInput = TigerInputs.parserLanguageProjectInput(shared, languageProject)
                .classKind(ClassKind.Manual)
                .manualParser("my.lang", "MyParser")
                .manualFactory("my.lang", "MyParserFactory")
                .build();
            session.require(component.getParserLanguageCompiler().createTask(languageProjectInput));
            fileAssertions.scopedNotExists(languageProjectInput.classesGenDirectory(), (s) -> {
                s.assertNotExists(languageProjectInput.genTable());
                s.assertNotExists(languageProjectInput.genParser());
                s.assertNotExists(languageProjectInput.genFactory());
            });

            final ParserAdapterCompiler.Input adapterProjectInput = TigerInputs.parserAdapterProjectInput(shared, languageProject, adapterProject)
                .classKind(ClassKind.Manual)
                .manualParseTaskDef("my.adapter.taskdef", "MyParseTaskDef")
                .manualTokenizeTaskDef("my.adapter.taskdef", "MyTokenizeTaskDef")
                .build();
            session.require(component.getParserAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedNotExists(adapterProjectInput.classesGenDirectory(), (s) -> {
                s.assertNotExists(adapterProjectInput.genParseTaskDef());
                s.assertNotExists(adapterProjectInput.tokenizeTaskDef());
            });
        }
    }

    @ParameterizedTest @EnumSource(value = ClassKind.class, names = {"Manual"})
    void testManualRequiresClasses(ClassKind classKind) {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared().build();
        final LanguageProject languageProject = TigerInputs.languageProject(baseDirectory, shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(baseDirectory, shared).build();

        assertThrows(IllegalArgumentException.class, () -> {
            TigerInputs.parserLanguageProjectInput(shared, languageProject)
                .classKind(classKind)
                .build(); // Class kind is Manual but manual class names were not set: check fails.
        });
        TigerInputs.parserLanguageProjectInput(shared, languageProject)
            .classKind(classKind)
            .manualParser("my.lang", "MyParser")
            .manualFactory("my.lang", "MyParserFactory")
            .build();

        assertThrows(IllegalArgumentException.class, () -> {
            TigerInputs.parserAdapterProjectInput(shared, languageProject, adapterProject)
                .classKind(classKind)
                .build(); // Class kind is Manual, but manual class names were not set: check fails.
        });
        TigerInputs.parserAdapterProjectInput(shared, languageProject, adapterProject)
            .classKind(classKind)
            .manualParseTaskDef("my.adapter.taskdef", "MyParseTaskDef")
            .manualTokenizeTaskDef("my.adapter.taskdef", "MyTokenizeTaskDef")
            .build();
    }
}
