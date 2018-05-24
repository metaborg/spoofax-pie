package mb.spoofax.runtime.benchmark.state;

import mb.pie.vfs.path.PPath;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;


@State(Scope.Benchmark)
public class WorkspaceState {
    @Param({}) private String workspaceRootStr;

    public PPath root;
    public PPath storePath;


    public void setup(SpoofaxPieState spoofaxPieState) throws IOException {
        this.root = spoofaxPieState.pathSrv.resolve(workspaceRootStr);
        this.storePath = root.resolve(".pie/");
    }
}
