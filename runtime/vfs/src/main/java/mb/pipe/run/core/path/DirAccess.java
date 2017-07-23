package mb.pipe.run.core.path;

public interface DirAccess {
    void readDir(PPath dir);

    void writeDir(PPath dir);
}
