package mb.tiger.spoofax;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.tiger.spoofax.command.TigerConstructTextualChangeCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConstructTextualChangeTest extends TestBase {

    @Test void test() throws ExecException, InterruptedException {
        final TextResource resource = textResourceRegistry.createResource("1 + 1", "a.tig");
        final TigerConstructTextualChangeCommand command = component.getConstructTextualChangeCommand();

        try(final MixedSession session = newSession()) {
            final CommandFeedback output = session.require(command.createTask(resource.key));
            assertFalse(output.getShowFeedbacks().isEmpty());
            final ShowFeedback showFeedback = output.getShowFeedbacks().get(0);
            assertTrue(showFeedback.caseOf().showText_(true).otherwise_(false));
            assertEquals("1 + 1", showFeedback.caseOf().showText((text, name, region) -> text).otherwise_(""));
        }
    }

}
