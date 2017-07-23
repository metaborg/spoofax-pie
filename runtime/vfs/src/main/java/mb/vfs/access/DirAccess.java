package mb.vfs.access;

import mb.vfs.path.PPath;

public interface DirAccess {
    void readDir(PPath dir);

    void writeDir(PPath dir);
}
