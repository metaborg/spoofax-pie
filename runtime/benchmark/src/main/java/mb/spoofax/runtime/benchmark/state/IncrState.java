package mb.spoofax.runtime.benchmark.state;

import mb.vfs.path.PPath;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@State(Scope.Benchmark)
public class IncrState {
    @Param({}) public String changedPathsStr;

    public List<PPath> changedPaths;


    public void setup(WorkspaceState workspaceState) throws IOException {
        this.changedPaths = new ArrayList<>();

        for(String changedPathStr : changedPathsStr.split("\\|")) {
            changedPathStr = changedPathStr.trim();
            if(changedPathStr.isEmpty()) continue;
            final PPath changedPath = workspaceState.root.resolve(changedPathStr);
            changedPath.touchFile();
            this.changedPaths.add(changedPath);
        }
    }
}
