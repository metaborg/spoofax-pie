package mb.spoofax.runtime.benchmark.state;

import kotlin.Unit;
import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.FuncApp;
import mb.pie.runtime.core.ObsFunc;
import mb.pie.runtime.core.PushingExecutor;
import mb.pie.runtime.core.impl.PushingExecutorImpl;
import mb.vfs.path.PPath;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@State(Scope.Benchmark)
public class PushingExecState {
    public PushingExecutor pushingExecutor;

    public void setup(InfraState infraState) {
        this.pushingExecutor =
            new PushingExecutorImpl(infraState.store, infraState.cache, infraState.share, infraState.layer,
                infraState.logger, infraState.builders);
    }

    public void renew(InfraState infraState) {
        this.pushingExecutor =
            new PushingExecutorImpl(infraState.store, infraState.cache, infraState.share, infraState.layer,
                infraState.logger, infraState.builders);
    }

    public void exec(WorkspaceState workspaceState, List<PPath> changedPaths) {
        final FuncApp<? extends Serializable, Serializable> app =
            new FuncApp<>("processWorkspace", workspaceState.root);
        final ObsFunc<? extends Serializable, Serializable> obsFunc = new ObsFunc<>(app, o -> Unit.INSTANCE);
        final ArrayList<ObsFunc<? extends Serializable, Serializable>> obsFuncs = new ArrayList<>();
        obsFuncs.add(obsFunc);
        try {
            pushingExecutor.require(obsFuncs, changedPaths);
        } catch(ExecException e) {
            throw new RuntimeException(e);
        }
    }
}
