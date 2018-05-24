package mb.spoofax.pie.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Simple time measurement functionality.
 */
public class Timer {
    /** ThreadMXBean for measuring CPU time. **/
    private final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

    /** If precise CPU time measurements are available. **/
    private final boolean canLogCPUTime = false;

    /** Last starting time since start was called. **/
    private long startTime = 0;


    public Timer() {
        this(false);
    }

    public Timer(boolean start) {
        //canLogCPUTime = mxBean.isThreadCpuTimeSupported();
        if(canLogCPUTime)
            mxBean.setThreadCpuTimeEnabled(true);
        if(start)
            start();
    }


    /**
     * Starts the timer, noting the current time.
     */
    public void start() {
        startTime = time();
    }

    /**
     * @return The duration, in nanoseconds, between the call to {@link #start()} and this invocation. This method can
     *         be called multiple times after one {@link #start()} invocation.
     */
    public long stop() {
        return time() - startTime;
    }


    public static File logFile;

    public static void clearFile() {
        try {
            new FileWriter(logFile, false)
                .append("name,time_ms,requires,executions,fileReqs,fileGens,taskReqs\n")
                .close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void stopAndPrint(String name, int requires, int executions, int fileReqs, int fileGens, int callReqs) {
        final long nanoTime = time() - startTime;
        final double msTime = (double)nanoTime / 1000_000.0;
        System.out.println(name + ": " + msTime);
        try {
            new FileWriter(logFile, true)
                .append("\"" + name + "\",")
                .append(String.valueOf(msTime))
                .append(",")
                .append(String.valueOf(requires))
                .append(",")
                .append(String.valueOf(executions))
                .append(",")
                .append(String.valueOf(fileReqs))
                .append(",")
                .append(String.valueOf(fileGens))
                .append(",")
                .append(String.valueOf(callReqs))
                .append("\n")
                .close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the timer, forgetting the time noted when {@link #start()} was called.
     */
    public void reset() {
        startTime = 0;
    }


    private long time() {
        if(canLogCPUTime)
            return mxBean.getCurrentThreadCpuTime();
        else
            return System.nanoTime();
    }
}
