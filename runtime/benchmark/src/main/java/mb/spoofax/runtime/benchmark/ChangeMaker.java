package mb.spoofax.runtime.benchmark;

import mb.pie.runtime.exec.Stats;
import mb.pie.vfs.path.PPath;
import mb.spoofax.runtime.benchmark.state.exec.BUState;
import mb.spoofax.runtime.benchmark.state.exec.TDState;
import org.jetbrains.annotations.Nullable;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.util.*;

public abstract class ChangeMaker {
    private @Nullable TDState tdState;
    private @Nullable BUState buState;


    public void run(TDState state) {
        this.tdState = state;
        this.buState = null;
        this.apply();
    }

    public void run(BUState state) {
        this.tdState = null;
        this.buState = state;
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
        if(buState != null) {
            gc();
            final Timer timer = startStats();
            buState.addProject(project, blackhole);
            endStats(timer, name);
        }
    }

    protected void execInitial(Blackhole blackhole, String name) {
        if(tdState != null) {
            gc();
            final Timer timer = startStats();
            tdState.execAll(blackhole);
            endStats(timer, name);
        }
    }

    protected void execEditor(String text, PPath file, PPath project, Blackhole blackhole, String name) {
        gc();
        final Timer timer = startStats();
        if(tdState != null) {
            tdState.addOrUpdateEditor(text, file, project, blackhole);
        } else if(buState != null) {
            buState.addOrUpdateEditor(text, file, project, blackhole);
        }
        endStats(timer, name);
    }

    protected void execPathChanges(PPath pathChange, Blackhole blackhole, String name) {
        final HashSet<PPath> pathChanges = new HashSet<>();
        pathChanges.add(pathChange);
        execPathChanges(pathChanges, blackhole, name);
    }

    protected void execPathChanges(Blackhole blackhole, String name, PPath... pathChanges) {
        final HashSet<PPath> pathChangesSet = new HashSet<>();
        Collections.addAll(pathChangesSet, pathChanges);
        execPathChanges(pathChangesSet, blackhole, name);
    }

    protected void execPathChanges(Set<PPath> pathChanges, Blackhole blackhole, String name) {
        gc();
        final Timer timer = startStats();
        if(tdState != null) {
            tdState.execAll(blackhole);
        } else if(buState != null) {
            buState.execPathChanges(pathChanges);
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

    private static void gc() {
        Object obj = new Object();
        final WeakReference ref = new WeakReference<>(obj);
        //noinspection AssignmentToNull,UnusedAssignment
        obj = null;
        do {
            System.gc();
        } while(ref.get() != null);
    }
}
