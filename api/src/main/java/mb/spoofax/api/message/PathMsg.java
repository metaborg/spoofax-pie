package mb.spoofax.api.message;

import mb.pie.vfs.path.PPath;

public interface PathMsg extends Msg {
    /**
     * @return Path the message belongs to.
     */
    PPath path();
}
