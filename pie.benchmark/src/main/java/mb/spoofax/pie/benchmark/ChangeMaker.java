package mb.spoofax.pie.benchmark;

import mb.fs.api.node.FSNode;
import mb.fs.java.JavaFSPath;
import mb.pie.runtime.exec.Stats;
import mb.spoofax.pie.benchmark.state.exec.BUState;
import mb.spoofax.pie.benchmark.state.exec.TDState;
import org.jetbrains.annotations.Nullable;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.lang.ref.WeakReference;
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


    protected String read(JavaFSPath file) {
        return read(file.toNode());
    }

    protected String read(FSNode file) {
        try {
            return new String(file.readAllBytes());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void write(JavaFSPath file, String text) {
        write(file.toNode(), text);
    }

    protected void write(FSNode file, String text) {
        try {
            file.writeAllBytes(text.getBytes());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }


    protected void addOrUpdateProject(JavaFSPath project, Blackhole blackhole, String name) {
        gc();
        final mb.spoofax.pie.benchmark.Timer timer = startStats();
        if(tdState != null) {
            tdState.addOrUpdateProject(project, blackhole);
        } else if(buState != null) {
            buState.addOrUpdateProject(project, blackhole);
        }
        endStats(timer, name);
    }

    protected void remoteProject(JavaFSPath project, String name) {
        gc();
        final mb.spoofax.pie.benchmark.Timer timer = startStats();
        if(tdState != null) {
            tdState.removeProject(project);
        } else if(buState != null) {
            buState.removeProject(project);
        }
        endStats(timer, name);
    }


    protected void addOrUpdateEditor(String text, JavaFSPath file, JavaFSPath project, Blackhole blackhole, String name) {
        gc();
        final mb.spoofax.pie.benchmark.Timer timer = startStats();
        if(tdState != null) {
            tdState.addOrUpdateEditor(text, file, project, blackhole);
        } else if(buState != null) {
            buState.addOrUpdateEditor(text, file, project, blackhole);
        }
        endStats(timer, name);
    }

    protected void removeEditor(JavaFSPath file, String name) {
        gc();
        final mb.spoofax.pie.benchmark.Timer timer = startStats();
        if(tdState != null) {
            tdState.removeEditor(file);
        } else if(buState != null) {
            buState.removeEditor(file);
        }
        endStats(timer, name);
    }


    protected void execResourceChanges(JavaFSPath pathChange, Blackhole blackhole, String name) {
        final HashSet<JavaFSPath> pathChanges = new HashSet<>();
        pathChanges.add(pathChange);
        execResourceChanges(pathChanges, blackhole, name);
    }

    protected void execResourceChanges(Blackhole blackhole, String name, JavaFSPath... pathChanges) {
        final HashSet<JavaFSPath> pathChangesSet = new HashSet<>();
        Collections.addAll(pathChangesSet, pathChanges);
        execResourceChanges(pathChangesSet, blackhole, name);
    }

    protected void execResourceChanges(Set<JavaFSPath> pathChanges, Blackhole blackhole, String name) {
        gc();
        final mb.spoofax.pie.benchmark.Timer timer = startStats();
        if(tdState != null) {
            tdState.execAll(blackhole);
        } else if(buState != null) {
            buState.execResourceChanges(pathChanges);
        }
        endStats(timer, name);
    }


    private mb.spoofax.pie.benchmark.Timer startStats() {
        final mb.spoofax.pie.benchmark.Timer timer = new mb.spoofax.pie.benchmark.Timer(true);
        Stats.INSTANCE.reset();
        return timer;
    }

    private void endStats(mb.spoofax.pie.benchmark.Timer timer, String name) {
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
