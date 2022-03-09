package mb.tiger.eclipse;

import mb.spoofax.eclipse.command.RunCommandHandler;
import mb.tiger.spoofax.TigerParticipant;

public class TigerRunCommandHandler extends RunCommandHandler {
    public TigerRunCommandHandler() {
        super(TigerEclipseParticipantFactory.getParticipant().getComponent(), TigerEclipseParticipantFactory.getParticipant().getPieComponent());
    }
}
