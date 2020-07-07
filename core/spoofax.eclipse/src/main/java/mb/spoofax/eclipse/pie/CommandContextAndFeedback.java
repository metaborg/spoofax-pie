package mb.spoofax.eclipse.pie;

import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandFeedback;

public class CommandContextAndFeedback {
    public final CommandContext context;
    public final CommandFeedback feedback;

    public CommandContextAndFeedback(CommandContext context, CommandFeedback feedback) {
        this.context = context;
        this.feedback = feedback;
    }
}
