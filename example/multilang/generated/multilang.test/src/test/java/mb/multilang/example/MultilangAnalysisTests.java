package mb.multilang.example;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.statix.multilang.ConfigurationException;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.pie.config.ContextConfig;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;
import org.junit.jupiter.api.Test;
import org.metaborg.util.log.Level;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultilangAnalysisTests extends TestBase {

    @Test public void testMultilangAnalysisSucceeds() throws IOException, ExecException, InterruptedException {
        createTextFile("module a sorts ID A context-free syntax A.B = <<ID*>>", "a.msdf");
        createTextFile("module b imports a rules rw: B(lid) -> B([])", "b.mstr");

        try(MixedSession session = newSession()) {
            KeyedMessages msdfMessages = session.require(miniSdfComponent
                .getLanguageInstance().createCheckTask(rootDirectory.getKey()));

            KeyedMessages mstrMessages = session.require(miniStrComponent
                .getLanguageInstance().createCheckTask(rootDirectory.getKey()));

            assertNotNull(msdfMessages);
            assertNotNull(mstrMessages);

            assertEquals(0, msdfMessages.size());
            assertEquals(0, mstrMessages.size());
        }
    }

    @Test public void testMultilangAnalysisFails() throws IOException, ExecException, InterruptedException {
        createTextFile("module a sorts ID A context-free syntax A.B = <<ID*>>", "a.msdf");
        createTextFile("module b imports a rules rw: B(lid) -> B()", "b.mstr");

        try(MixedSession session = newSession()) {
            KeyedMessages msdfMessages = session.require(miniSdfComponent
                .getLanguageInstance().createCheckTask(rootDirectory.getKey()));

            KeyedMessages mstrMessages = session.require(miniStrComponent
                .getLanguageInstance().createCheckTask(rootDirectory.getKey()));

            assertNotNull(msdfMessages);
            assertNotNull(mstrMessages);

            // Because message count and location is not deterministic
            // We combine the message sets, and assert there are at least some
            assertTrue(msdfMessages.size() + mstrMessages.size() > 0);
        }
    }

    @Test public void testMultilangAnalysisWithConfigFails() throws IOException, ExecException, InterruptedException {
        createTextFile("module a sorts ID A context-free syntax A.B = <<ID*>>", "a.msdf");
        createTextFile("module b imports a rules rw: B(lid) -> B([])", "b.mstr");
        createTextFile("\n" +
            "languageContexts:\n" +
            "  'mb.minisdf': \"mb.minisdf\"\n" +
            "  'mb.ministr': \"mb.ministr\"", "multilang.yaml");

        try(MixedSession session = newSession()) {
            KeyedMessages msdfMessages = session.require(miniSdfComponent
                .getLanguageInstance().createCheckTask(rootDirectory.getKey()));

            KeyedMessages mstrMessages = session.require(miniStrComponent
                .getLanguageInstance().createCheckTask(rootDirectory.getKey()));

            assertNotNull(msdfMessages);
            assertNotNull(mstrMessages);

            // Because message count and location is not deterministic
            // We combine the message sets, and assert there are at least some
            assertTrue(msdfMessages.size() + mstrMessages.size() > 0);
        }
    }


    @Test public void readConfig() throws IOException, ExecException, InterruptedException, ConfigurationException {
        createTextFile("\n" +
            "languageContexts:\n" +
            "  'mb.lang1': \"debugContext\"\n" +
            "  'mb.lang2': \"debugContext\"\n" +
            "  'mb.lang3': \"lang3\"\n" +
            "contextConfigs:\n" +
            "  debugContext:\n" +
            "    logging: debug\n" +
            "    stripTraces: true", "multilang.yaml");

        try(MixedSession session = newSession()) {
            Result<ContextConfig, ConfigurationException> configResult = session.require(multiLangComponent
                .getBuildContextConfiguration()
                .createTask(new SmlBuildContextConfiguration.Input(rootDirectory.getPath(), new LanguageId("mb.lang1"))));

            assertTrue(configResult.isOk());
            ContextConfig config = configResult.unwrap();

            assertEquals(2, config.languages().size());
            assertTrue(config.languages().contains(new LanguageId("mb.lang1")));
            assertTrue(config.languages().contains(new LanguageId("mb.lang2")));
            assertEquals(Level.Debug, config.parseLevel());
            assertTrue(config.stripTraces());
        }
    }
}
