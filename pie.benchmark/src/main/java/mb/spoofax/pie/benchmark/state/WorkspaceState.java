package mb.spoofax.pie.benchmark.state;

import mb.fs.java.JavaFSPath;
import mb.spoofax.legacy.LoadMetaLanguages;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;


@State(Scope.Benchmark)
public class WorkspaceState {
    @Param({}) private String workspaceRootStr;

    public JavaFSPath root;
    public JavaFSPath storePath;


    public void setup(SpoofaxPieState spoofaxPieState) {
        this.root = new JavaFSPath(workspaceRootStr).toAbsoluteFromWorkingDirectory();
        this.storePath = root.appendSegment(".pie");
        unpackAndLoadMetaLanguages();
    }

    public void unpackAndLoadMetaLanguages() {
        try {
            LoadMetaLanguages.loadAll(this.root.toNode());
        } catch(IOException | MetaborgException e) {
            throw new RuntimeException("Could not unpack and load meta-languages", e);
        }
    }
}
