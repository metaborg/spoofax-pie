package mb.spoofax.pie.benchmark.counter;

import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
@AuxCounters(AuxCounters.Type.EVENTS)
public class PieCounters {
//    public long timeTotalNs;
//    public double timeTotalConverted;
//
//    public long numRequires;
//
//
//    public long timeConsistentCheckNs;
//    public double timeConsistentCheckConverted;
//    public double timePercentageConsistentCheck;
//    public long numConsistentChecked;
//    public long numConsistent;
//
//    public double percentageConsistent() {
//        return div(numConsistent, numConsistentChecked);
//    }
//
//
//    public long timeCacheCheckNs;
//    public double timeCacheCheckConverted;
//    public double timePercentageCacheCheck;
//    public long numCacheChecked;
//    public long numCached;
//
//    public double percentageCached() {
//        return div(numCached, numCacheChecked);
//    }
//
//
//    public long timeStoreCheckNs;
//    public double timeStoreCheckConverted;
//    public double timePercentageStoreCheck;
//    public long numStoreChecked;
//    public long numStored;
//
//    public double percentageStored() {
//        return div(numStored, numStoreChecked);
//    }
//
//
//    public long timeGenCheckNs;
//    public double timeGenCheckConverted;
//    public double timePercentageGenCheck;
//    public long numGensChecked;
//    public long numGensConsistent;
//
//    public double percentageGensConsistent() {
//        return div(numGensChecked, numGensConsistent);
//    }
//
//
//    public long timePathReqCheckNs;
//    public double timePathReqCheckConverted;
//    public double timePercentagePathReqCheck;
//    public long numPathReqsChecked;
//    public long numPathReqsConsistent;
//
//    public double percentagePathReqsConsistent() {
//        return div(numPathReqsChecked, numPathReqsConsistent);
//    }
//
//
//    public long timeBuildReqCheckNs;
//    public double timeBuildReqCheckConverted;
//    public double timePercentageBuildReqCheck;
//    public long numBuildReqsChecked;
//    public long numBuildReqsConsistent;
//
//    public double percentageBuildReqsConsistent() {
//        return div(numBuildReqsChecked, numBuildReqsConsistent);
//    }
//
//
//    public long numRebuilds;
//
//    public double percentageRebuilt() {
//        return div(numRebuilds, numRequires);
//    }
//
//
//    @TearDown(Level.Invocation)
//    public void tearDown(InfraState state, BenchmarkParams params) {
//        if(state.loggerKind != InfraState.LoggerKind.trace) {
//            throw new RuntimeException("Counting was requested but logger kind was not set to 'trace'");
//        }
//        final TraceLogger logger = (TraceLogger) state.logger;
//        final List<Trace> traces = logger.getTraces();
//
//        long timeTotal = 0;
//        long startTimestampTotal = -1;
//        long timeConsistentCheck = 0;
//        long startTimestampConsistentCheck = -1;
//        long timeCacheCheck = 0;
//        long startTimestampCacheCheck = -1;
//        long timeStoreCheck = 0;
//        long startTimestampStoreCheck = -1;
//        long timeGenCheck = 0;
//        long startTimestampGenCheck = -1;
//        long timePathReqCheck = 0;
//        long startTimestampPathReqCheck = -1;
//        long timeBuildReqCheck = 0;
//        long startTimestampBuildReqCheck = -1;
//
//        for(Trace trace : traces) {
//            long time = trace.getTime();
//            if(trace instanceof RequireTopDownInitialStart) {
//                startTimestampTotal = time;
//            } else if(trace instanceof RequireTopDownInitialEnd) {
//                timeTotal += calcTime(startTimestampTotal, time);
//                startTimestampTotal = -1;
//            } else if(trace instanceof RequireTopDownStart) {
//            } else if(trace instanceof RequireTopDownEnd) {
//                ++numRequires;
//            } else if(trace instanceof CheckVisitedStart) {
//                startTimestampConsistentCheck = time;
//            } else if(trace instanceof CheckVisitedEnd) {
//                timeConsistentCheck += calcTime(startTimestampConsistentCheck, time);
//                startTimestampConsistentCheck = -1;
//                ++numConsistentChecked;
//                final CheckVisitedEnd end = (CheckVisitedEnd) trace;
//                if(end.getResult() != null) {
//                    ++numConsistent;
//                }
//            } else if(trace instanceof CheckCachedStart) {
//                startTimestampCacheCheck = time;
//            } else if(trace instanceof CheckCachedEnd) {
//                timeCacheCheck += calcTime(startTimestampCacheCheck, time);
//                startTimestampCacheCheck = -1;
//                ++numCacheChecked;
//                final CheckCachedEnd end = (CheckCachedEnd) trace;
//                if(end.getResult() != null) {
//                    ++numCached;
//                }
//            } else if(trace instanceof CheckStoredStart) {
//                startTimestampStoreCheck = time;
//            } else if(trace instanceof CheckStoredEnd) {
//                timeStoreCheck += calcTime(startTimestampStoreCheck, time);
//                startTimestampStoreCheck = -1;
//                ++numStoreChecked;
//                final CheckStoredEnd end = (CheckStoredEnd) trace;
//                if(end.getResult() != null) {
//                    ++numStored;
//                }
//            } else if(trace instanceof CheckGenStart) {
//                startTimestampGenCheck = time;
//            } else if(trace instanceof CheckGenEnd) {
//                timeGenCheck += calcTime(startTimestampGenCheck, time);
//                startTimestampGenCheck = -1;
//                ++numGensChecked;
//                final CheckGenEnd end = (CheckGenEnd) trace;
//                if(end.getReason() == null) {
//                    ++numGensConsistent;
//                }
//            } else if(trace instanceof CheckPathReqStart) {
//                startTimestampPathReqCheck = time;
//            } else if(trace instanceof CheckPathReqEnd) {
//                timePathReqCheck += calcTime(startTimestampPathReqCheck, time);
//                startTimestampPathReqCheck = -1;
//                ++numPathReqsChecked;
//                final CheckPathReqEnd end = (CheckPathReqEnd) trace;
//                if(end.getReason() == null) {
//                    ++numPathReqsConsistent;
//                }
//            } else if(trace instanceof CheckBuildReqStart) {
//                startTimestampBuildReqCheck = time;
//            } else if(trace instanceof CheckBuildReqEnd) {
//                timeBuildReqCheck += calcTime(startTimestampBuildReqCheck, time);
//                startTimestampBuildReqCheck = -1;
//                ++numBuildReqsChecked;
//                final CheckBuildReqEnd end = (CheckBuildReqEnd) trace;
//                if(end.getReason() == null) {
//                    ++numBuildReqsConsistent;
//                }
//            } else if(trace instanceof RebuildStart) {
//            } else if(trace instanceof RebuildEnd) {
//                ++numRebuilds;
//            }
//        }
//
//        final TimeUnit targetTimeUnit = params.getTimeUnit();
//
//        this.timeTotalNs = timeTotal;
//        this.timeTotalConverted = convertNsTimeTo(timeTotal, targetTimeUnit);
//
//        this.timeConsistentCheckNs = timeConsistentCheck;
//        this.timeConsistentCheckConverted = convertNsTimeTo(timeConsistentCheck, targetTimeUnit);
//        this.timePercentageConsistentCheck = div(timeConsistentCheck, timeTotal);
//
//        this.timeCacheCheckNs = timeCacheCheck;
//        this.timeCacheCheckConverted = convertNsTimeTo(timeCacheCheck, targetTimeUnit);
//        this.timePercentageCacheCheck = div(timeCacheCheck, timeTotal);
//
//        this.timeStoreCheckNs = timeStoreCheck;
//        this.timeStoreCheckConverted = convertNsTimeTo(timeStoreCheck, targetTimeUnit);
//        this.timePercentageStoreCheck = div(timeStoreCheck, timeTotal);
//
//        this.timeGenCheckNs = timeGenCheck;
//        this.timeGenCheckConverted = convertNsTimeTo(timeGenCheck, targetTimeUnit);
//        this.timePercentageGenCheck = div(timeGenCheck, timeTotal);
//
//        this.timePathReqCheckNs = timePathReqCheck;
//        this.timePathReqCheckConverted = convertNsTimeTo(timePathReqCheck, targetTimeUnit);
//        this.timePercentagePathReqCheck = div(timePathReqCheck, timeTotal);
//
//        this.timeBuildReqCheckNs = timeBuildReqCheck;
//        this.timeBuildReqCheckConverted = convertNsTimeTo(timeBuildReqCheck, targetTimeUnit);
//        this.timePercentageBuildReqCheck = div(timeBuildReqCheck, timeTotal);
//    }
//
//    private long calcTime(long startTimestamp, long endTimestamp) {
//        if(startTimestamp < 0) {
//            throw new RuntimeException("Start timestamp " + startTimestamp + " is smaller than 0");
//        }
//        if(endTimestamp < 0) {
//            throw new RuntimeException("End timestamp " + endTimestamp + " is smaller than 0");
//        }
//        if(startTimestamp > endTimestamp) {
//            throw new RuntimeException(
//                "Start timestamp " + startTimestamp + " is larger than end timestamp " + endTimestamp);
//        }
//        return endTimestamp - startTimestamp;
//    }
//
//    private double div(long a, long b) {
//        if(b == 0) return 0.0;
//        return (double) a / (double) b;
//    }
//
//    private double convertNsTimeTo(long timeNs, TimeUnit target) {
//        switch(target) {
//            case NANOSECONDS:
//                return (double) timeNs;
//            case MICROSECONDS:
//                return div(timeNs, 1000L);
//            case MILLISECONDS:
//                return div(timeNs, 1000L * 1000);
//            case SECONDS:
//                return div(timeNs, 1000L * 1000 * 1000);
//            case MINUTES:
//                return div(timeNs, 1000L * 1000 * 1000 * 60);
//            case HOURS:
//                return div(timeNs, 1000L * 1000 * 1000 * 60 * 60);
//            case DAYS:
//                return div(timeNs, 1000L * 1000 * 1000 * 60 * 60 * 24);
//        }
//        throw new RuntimeException("Unhandled target time: " + target);
//    }
}