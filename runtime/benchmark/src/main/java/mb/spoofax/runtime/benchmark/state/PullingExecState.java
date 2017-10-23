package mb.spoofax.runtime.benchmark.state;

import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.ExecInfo;
import mb.pie.runtime.core.FuncApp;
import mb.pie.runtime.core.PullingExec;
import mb.pie.runtime.core.impl.PullingExecImpl;
import mb.vfs.path.PPath;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.Serializable;


@State(Scope.Benchmark)
public class PullingExecState {
    public PullingExec pullingExec;

    public void setup(InfraState infraState) {
        this.pullingExec =
            new PullingExecImpl(infraState.store, infraState.cache, infraState.share, infraState.layer.get(),
                infraState.logger.get(), infraState.builders);
    }

    public void renew(InfraState infraState) {
        this.pullingExec =
            new PullingExecImpl(infraState.store, infraState.cache, infraState.share, infraState.layer.get(),
                infraState.logger.get(), infraState.builders);
    }

    public ExecInfo<PPath, ? extends Serializable> exec(WorkspaceState workspaceState) {
        final FuncApp<PPath, ? extends Serializable> app = new FuncApp<>("processWorkspace", workspaceState.root);
        try {
            return pullingExec.requireInfo(app);
        } catch(ExecException e) {
            throw new RuntimeException(e);
        }
    }
}
