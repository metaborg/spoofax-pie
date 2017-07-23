package mb.vfs.access;

import mb.vfs.path.PPath;

public interface FileAccess {
    void readFile(PPath file);

    void writeFile(PPath file);
}
