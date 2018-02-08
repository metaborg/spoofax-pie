package mb.spoofax.runtime.benchmark.state;

import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.ExecInfo;
import mb.pie.runtime.core.FuncApp;
import mb.pie.runtime.core.exec.TopDownExec;
import mb.pie.runtime.core.impl.exec.TopDownExecImpl;
import mb.util.async.NullCancelled;
import mb.vfs.path.PPath;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.Serializable;


@State(Scope.Benchmark)
public class PullingExecState {
    public TopDownExec exec;

    public void setup(InfraState infraState) {
        this.exec =
            new TopDownExecImpl(infraState.store, infraState.cache, infraState.share, infraState.layer.get(),
                infraState.logger.get(), infraState.funcs);
    }

    public void renew(InfraState infraState) {
        this.exec =
            new TopDownExecImpl(infraState.store, infraState.cache, infraState.share, infraState.layer.get(),
                infraState.logger.get(), infraState.funcs);
    }

    public ExecInfo<PPath, ? extends Serializable> exec(WorkspaceState workspaceState) {
        final FuncApp<PPath, ? extends Serializable> app = new FuncApp<>("processWorkspace", workspaceState.root);
        try {
            return exec.requireInfo(app, new NullCancelled());
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
