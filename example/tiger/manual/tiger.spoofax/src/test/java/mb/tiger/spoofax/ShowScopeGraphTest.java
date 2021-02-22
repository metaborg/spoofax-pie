package mb.tiger.spoofax;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.tiger.spoofax.command.TigerShowScopeGraphCommand;
import mb.tiger.spoofax.task.TigerShowArgs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShowScopeGraphTest extends TestBase {
    @Test void test() throws ExecException, InterruptedException {
        final TextResource resource = textResourceRegistry.createResource("1 + 1", "a.tig");
        final TigerShowScopeGraphCommand command = component.getShowScopeGraphCommand();
        try(final MixedSession session = newSession()) {
            final CommandFeedback output = session.require(command.createTask(new TigerShowArgs(resource.key, null)));
            assertFalse(output.getShowFeedbacks().isEmpty());
            final ShowFeedback showFeedback = output.getShowFeedbacks().get(0);
            assertTrue(showFeedback.caseOf().showText_(true).otherwise_(false));
            assertTrue(showFeedback.caseOf().showText((text, name, region) -> text.contains("ScopeGraph(") && name.contains("Scope graph for")).otherwise_(false));
        }
    }
}
