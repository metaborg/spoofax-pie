package mb.spoofax.runtime.benchmark.state;

import kotlin.Unit;
import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.ObsFuncApp;
import mb.pie.runtime.core.PushingExecutor;
import mb.pie.runtime.core.impl.PushingExecutorImpl;
import mb.spoofax.runtime.pie.builder.SpoofaxPipeline;
import mb.vfs.path.PPath;
import mb.vfs.path.PPaths;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@State(Scope.Benchmark)
public class PushingExecState {
    public PushingExecutor pushingExecutor;

    public void setup(SpoofaxPieState spoofaxPieState, InfraState infraState) {
        this.pushingExecutor =
            new PushingExecutorImpl(infraState.store, infraState.cache, infraState.share, infraState.layer,
                infraState.logger, infraState.builders, spoofaxPieState.logger);
    }

    public void renew(SpoofaxPieState spoofaxPieState, InfraState infraState) {
        this.pushingExecutor =
            new PushingExecutorImpl(infraState.store, infraState.cache, infraState.share, infraState.layer,
                infraState.logger, infraState.builders, spoofaxPieState.logger);
    }

    public void exec(WorkspaceState workspaceState, List<PPath> changedPaths) {
        final PPath root = workspaceState.root;
        try(final Stream<PPath> stream = root.list(PPaths.directoryPathMatcher())) {
            final ArrayList<ObsFuncApp<? extends Serializable, Serializable>> obsFuncApps = stream
                .filter((path) -> !path.toString().contains("root"))
                .map((path) -> SpoofaxPipeline.INSTANCE.processProjectObsFunApp(path, root, o -> Unit.INSTANCE))
                .collect(Collectors.toCollection(ArrayList::new));
            pushingExecutor.require(obsFuncApps, changedPaths);
        } catch(IOException | ExecException e) {
            throw new RuntimeException(e);
        }
    }
}
