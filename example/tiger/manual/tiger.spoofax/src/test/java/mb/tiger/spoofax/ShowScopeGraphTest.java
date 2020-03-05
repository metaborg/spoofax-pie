package mb.tiger.spoofax;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandOutput;
import mb.tiger.spoofax.command.TigerShowScopeGraphCommand;
import mb.tiger.spoofax.task.TigerShowArgs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShowScopeGraphTest extends TestBase {
    @Test void test() throws ExecException {
        final TextResource resource = textResourceRegistry.createResource("1 + 1", "a.tig");
        final TigerShowScopeGraphCommand command = languageComponent.getShowScopeGraphCommand();
        try(final MixedSession session = languageComponent.newPieSession()) {
            final CommandOutput output = session.require(command.createTask(new TigerShowArgs(resource.key, null)));
            assertFalse(output.feedback.isEmpty());
            final CommandFeedback feedback = output.feedback.get(0);
            assertTrue(feedback.caseOf().showFile_(false).showText_(true));
            assertTrue(feedback.caseOf().showText((text, name, region) -> text.contains("ScopeGraph(") && name.contains("Scope graph for")).otherwise_(false));
        }
    }
}
