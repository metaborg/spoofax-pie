package mb.spoofax.runtime.benchmark.state;

import com.google.inject.Key;
import com.google.inject.Provider;
import mb.pie.logger.mblog.LogLogger;
import mb.pie.runtime.core.*;
import mb.pie.runtime.cache.MapCache;
import mb.pie.runtime.cache.NoopCache;
import mb.pie.runtime.layer.NoopLayer;
import mb.pie.runtime.layer.ValidationLayer;
import mb.pie.runtime.logger.exec.NoopExecutorLogger;
import mb.pie.runtime.logger.StreamLogger;
import mb.pie.runtime.logger.exec.TraceExecutorLogger;
import mb.pie.runtime.share.CoroutineShare;
import mb.pie.runtime.share.NonSharingShare;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.store.LMDBBuildStoreFactory;
import mb.pie.runtime.store.NoopStore;
import mb.vfs.path.PPath;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.File;
import java.util.Map;


@State(Scope.Benchmark)
public class InfraState {
    public Store store;
    public Cache cache;
    public Share share;
    public Provider<Layer> layer;
    public Provider<Logger> logger;
    public Map<String, TaskDef<?, ?>> funcs;

    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState) {
        this.store = storeKind.provider(workspaceState.storePath, spoofaxPieState).get();
        try(final StoreWriteTxn txn = this.store.writeTxn()) {
            txn.drop();
        }
        this.cache = cacheKind.provider(spoofaxPieState).get();
        this.cache.drop();
        this.share = shareKind.provider(spoofaxPieState).get();
        this.layer = layerKind.provider(spoofaxPieState);
        this.logger = loggerKind.provider(spoofaxPieState);
        this.funcs = spoofaxPieState.injector.getInstance(new Key<Map<String, TaskDef<?, ?>>>() {
        });
    }

    public void reset() {
        try(final StoreWriteTxn txn = this.store.writeTxn()) {
            txn.drop();
        }
        this.cache.drop();
    }


    @Param({"in_memory"}) public StoreKind storeKind;
    @Param({"noop"}) public CacheKind cacheKind;
    @Param({"non_sharing"}) public ShareKind shareKind;
    @Param({"validation"}) public LayerKind layerKind;
    @Param({"noop"}) public LoggerKind loggerKind;

    public enum StoreKind {
        lmdb {
            @Override public Provider<Store> provider(PPath storePath, SpoofaxPieState spoofaxPieState) {
                final LMDBBuildStoreFactory factory = spoofaxPieState.injector.getInstance(LMDBBuildStoreFactory.class);
                final File localStorePath = spoofaxPieState.pathSrv.localPath(storePath);
                if(localStorePath == null) {
                    throw new RuntimeException(
                        "Cannot create PIE LMDB store at " + storePath + " because it is not on the local filesystem");
                }
                return () -> factory.create(localStorePath, 1024 * 1024 * 1024, 1024);
            }
        },
        in_memory {
            @Override public Provider<Store> provider(PPath storePath, SpoofaxPieState spoofaxPieState) {
                return InMemoryStore::new;
            }
        },
        noop {
            @Override public Provider<Store> provider(PPath storePath, SpoofaxPieState spoofaxPieState) {
                return NoopStore::new;
            }
        };

        public abstract Provider<Store> provider(PPath storePath, SpoofaxPieState spoofaxPieState);
    }

    public enum CacheKind {
        map {
            @Override public Provider<Cache> provider(SpoofaxPieState spoofaxPieState) {
                return MapCache::new;
            }
        },
        noop {
            @Override public Provider<Cache> provider(SpoofaxPieState spoofaxPieState) {
                return NoopCache::new;
            }
        };

        public abstract Provider<Cache> provider(SpoofaxPieState spoofaxPieState);
    }

    public enum ShareKind {
        coroutine {
            @Override public Provider<Share> provider(SpoofaxPieState spoofaxPieState) {
                return CoroutineShare::new;
            }
        },
        non_sharing {
            @Override public Provider<Share> provider(SpoofaxPieState spoofaxPieState) {
                return NonSharingShare::new;
            }
        };

        public abstract Provider<Share> provider(SpoofaxPieState spoofaxPieState);
    }

    public enum LayerKind {
        validation {
            @Override public Provider<Layer> provider(SpoofaxPieState spoofaxPieState) {
                return () -> spoofaxPieState.injector.getInstance(ValidationLayer.class);
            }
        },
        noop {
            @Override public Provider<Layer> provider(SpoofaxPieState spoofaxPieState) {
                return NoopLayer::new;
            }
        };

        public abstract Provider<Layer> provider(SpoofaxPieState spoofaxPieState);
    }

    public enum LoggerKind {
        trace {
            @Override public Provider<Logger> provider(SpoofaxPieState spoofaxPieState) {
                return TraceExecutorLogger::new;
            }
        },
        log {
            @Override public Provider<Logger> provider(SpoofaxPieState spoofaxPieState) {
                return () -> spoofaxPieState.injector.getInstance(LogLogger.class);
            }
        },
        stdout {
            @Override public Provider<Logger> provider(SpoofaxPieState spoofaxPieState) {
                return () -> new StreamLogger(System.out, null, 200);
            }
        },
        stdout_verbose {
            @Override public Provider<Logger> provider(SpoofaxPieState spoofaxPieState) {
                return () -> new StreamLogger(System.out, System.out, 200);
            }
        },
        noop {
            @Override public Provider<Logger> provider(SpoofaxPieState spoofaxPieState) {
                return NoopExecutorLogger::new;
            }
        };

        public abstract Provider<Logger> provider(SpoofaxPieState spoofaxPieState);
    }
}
