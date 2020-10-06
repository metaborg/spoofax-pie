package mb.str.spoofax;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.message.Severity;
import mb.common.util.ListView;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.ResourcePath;
import mb.str.spoofax.config.StrategoAnalyzeConfig;
import mb.str.spoofax.task.StrategoAnalyze;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AnalyzeTest extends TestBase {
    @Disabled("Temporarily disable") @Test void testCompileTask() throws Exception {
        final FSResource projectDir = rootDirectory.appendRelativePath("project").createDirectory(true);
        final FSResource mainFile = createTextFile(projectDir, "module main imports libstratego-lib rules hello = !$[hello [<world>]]; debug", "main.str");
        final FSResource otherFile = createTextFile(projectDir, "module other rules world = !\"world\"", "other.str");

        try(final MixedSession session = newSession()) {
            final ArrayList<ResourcePath> includeDirs = new ArrayList<>();
            includeDirs.add(projectDir.getPath());
            final ArrayList<String> builtinLibs = new ArrayList<>();
            builtinLibs.add("stratego-lib");
            final StrategoAnalyzeConfig config = new StrategoAnalyzeConfig(
                projectDir.getPath(),
                mainFile.getPath(),
                includeDirs,
                builtinLibs
            );
            @SuppressWarnings("ConstantConditions") final KeyedMessages messages = session.require(analyze.createTask(new StrategoAnalyze.Input(
                config,
                new ArrayList<>()
            )));
            assertTrue(messages.containsError());
            final ListView<Message> mainMessages = messages.getMessagesOfKey(mainFile.getKey());
            assertEquals(1, mainMessages.size());
            assertEquals(Severity.Error, mainMessages.get(0).severity);
            assertTrue(mainMessages.get(0).text.contains("world"));
            final ListView<Message> otherMessages = messages.getMessagesOfKey(otherFile.getKey());
            assertEquals(0, otherMessages.size());
        }
    }
}
