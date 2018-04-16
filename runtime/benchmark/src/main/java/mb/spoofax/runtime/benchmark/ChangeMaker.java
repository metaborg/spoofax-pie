package mb.spoofax.runtime.benchmark;

import mb.pie.runtime.core.Stats;
import mb.spoofax.runtime.benchmark.state.exec.BUTopsortState;
import mb.spoofax.runtime.benchmark.state.exec.TDState;
import mb.vfs.path.PPath;
import org.jetbrains.annotations.Nullable;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

public abstract class ChangeMaker {
    private @Nullable TDState tdState;
    private @Nullable BUTopsortState buTopsortState;


    public void run(TDState state) {
        this.tdState = state;
        this.buTopsortState = null;
        this.apply();
    }

    public void run(BUTopsortState state) {
        this.tdState = null;
        this.buTopsortState = state;
        this.apply();
    }


    protected abstract void apply();


    protected String read(PPath file) {
        try {
            return new String(file.readAllBytes());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void write(PPath file, String text) {
        try {
            Files.write(file.getJavaPath(), text.getBytes());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }


    protected void addProject(PPath project, Blackhole blackhole, String name) {
        if(buTopsortState != null) {
            final Timer timer = startStats();
            buTopsortState.addProject(project, blackhole);
            endStats(timer, name);
        }
    }

    protected void execInitial(Blackhole blackhole, String name) {
        if(tdState != null) {
            final Timer timer = startStats();
            tdState.execAll(blackhole);
            endStats(timer, name);
        }
    }

    protected void execEditor(String text, PPath file, PPath project, Blackhole blackhole, String name) {
        final Timer timer = startStats();
        if(tdState != null) {
            tdState.addOrUpdateEditor(text, file, project, blackhole);
        } else if(buTopsortState != null) {
            buTopsortState.addOrUpdateEditor(text, file, project, blackhole);
        }
        endStats(timer, name);
    }

    protected void execPathChanges(PPath pathChange, Blackhole blackhole, String name) {
        execPathChanges(Collections.singletonList(pathChange), blackhole, name);
    }

    protected void execPathChanges(List<PPath> pathChanges, Blackhole blackhole, String name) {
        final Timer timer = startStats();
        if(tdState != null) {
            tdState.execAll(blackhole);
        } else if(buTopsortState != null) {
            buTopsortState.execPathChanges(pathChanges);
        }
        endStats(timer, name);
    }


    private Timer startStats() {
        final Timer timer = new Timer(true);
        Stats.INSTANCE.reset();
        return timer;
    }

    private void endStats(Timer timer, String name) {
        timer.stopAndPrint(name, Stats.INSTANCE.getRequires(), Stats.INSTANCE.getExecutions(),
            Stats.INSTANCE.getFileReqs(), Stats.INSTANCE.getFileGens(), Stats.INSTANCE.getCallReqs());
    }
}
