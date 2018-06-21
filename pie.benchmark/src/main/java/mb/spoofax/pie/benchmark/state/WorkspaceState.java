package mb.spoofax.pie.benchmark.state;

import mb.pie.vfs.path.PPath;
import mb.spoofax.legacy.LoadMetaLanguages;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;


@State(Scope.Benchmark)
public class WorkspaceState {
    @Param({}) private String workspaceRootStr;

    public PPath root;
    public PPath storePath;


    public void setup(SpoofaxPieState spoofaxPieState) {
        this.root = spoofaxPieState.pathSrv.resolve(workspaceRootStr);
        this.storePath = root.resolve(".pie/");
        unpackAndLoadMetaLanguages();
    }

    public void unpackAndLoadMetaLanguages() {
        try {
            LoadMetaLanguages.loadAll(this.root);
        } catch(IOException | MetaborgException e) {
            throw new RuntimeException("Could not unpack and load meta-languages", e);
        }
    }
}
