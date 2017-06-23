package mb.pipe.run.core.path;

public interface FileAccess {
    void readFile(PPath file);

    void writeFile(PPath file);
}
