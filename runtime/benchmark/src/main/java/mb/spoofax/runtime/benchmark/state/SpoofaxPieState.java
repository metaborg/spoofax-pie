package mb.spoofax.runtime.benchmark.state;

import com.google.inject.Key;
import com.google.inject.Provider;
import mb.pie.runtime.builtin.util.LogLogger;
import mb.pie.runtime.core.*;
import mb.pie.runtime.core.impl.PollingExec;
import mb.pie.runtime.core.impl.cache.MapCache;
import mb.pie.runtime.core.impl.cache.NoopCache;
import mb.pie.runtime.core.impl.layer.NoopLayer;
import mb.pie.runtime.core.impl.layer.ValidationLayer;
import mb.pie.runtime.core.impl.logger.NoopLogger;
import mb.pie.runtime.core.impl.logger.StreamLogger;
import mb.pie.runtime.core.impl.logger.TraceLogger;
import mb.pie.runtime.core.BuildShare;
import mb.pie.runtime.core.impl.share.CoroutineBuildShare;
import mb.pie.runtime.core.impl.share.NonSharingBuildShare;
import mb.pie.runtime.core.impl.store.InMemoryStore;
import mb.pie.runtime.core.impl.store.InMemoryPathOnlyStore;
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory;
import mb.pie.runtime.core.impl.store.NoopStore;
import mb.vfs.path.PPath;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import static mb.spoofax.runtime.benchmark.state.SpoofaxPieStaticState.injector;
import static mb.spoofax.runtime.benchmark.state.SpoofaxPieStaticState.pathSrv;

@State(Scope.Benchmark)
public class SpoofaxPieState {
    @Param({"true"}) public boolean incremental;

    @Param({}) public String workspaceRootStr;
    public PPath workspaceRoot;
    public PPath storePath;

    public void setupWorkspaceRoot() {
        this.workspaceRoot = pathSrv.resolve(workspaceRootStr);
        this.storePath = workspaceRoot.resolve(".pie/");
    }


    public Store store;
    public Cache cache;
    public BuildShare share;
    public Layer layer;
    public Logger logger;
    public Map<String, Func<?, ?>> builders;
    public PollingExec build;

    public void setupBuild() {
        this.store = storeKind.provider(storePath).get();
        try(final StoreWriteTxn txn = this.store.writeTxn()) {
            txn.drop();
        }
        this.cache = cacheKind.provider().get();
        this.cache.drop();
        this.share = shareKind.provider().get();
        this.layer = layerKind.provider().get();
        this.logger = loggerKind.provider().get();
        this.builders = injector.getInstance(new Key<Map<String, Func<?, ?>>>() {
        });
        this.build = new PollingExec(store, cache, share, layer, logger, builders, injector);
    }

    public void renewBuild() {
        this.layer = layerKind.provider().get();
        this.logger = loggerKind.provider().get();
        this.build = new PollingExec(store, cache, share, layer, logger, builders, injector);
    }

    public void resetBuild() {
        try(final StoreWriteTxn txn = this.store.writeTxn()) {
            txn.drop();
        }
        this.cache.drop();
    }

    public ExecInfo<PPath, ? extends Serializable> runBuild() {
        final FuncApp<PPath, ? extends Serializable> app = new FuncApp<>("processWorkspace", workspaceRoot);
        return build.requireInitial(app);
    }


    @Setup(Level.Invocation) public void setupFull() {
        if(!incremental) {
            setupWorkspaceRoot();
            setupBuild();
        }
    }

    @Setup(Level.Trial) public void setupIncrTrial() {
        if(incremental) {
            setupWorkspaceRoot();
            setupBuild();
            runBuild();
        }
    }

    @Setup(Level.Invocation) public void setupIncrInvocation() {
        if(incremental) {
            renewBuild();
        }
    }


    @Param({"lmdb"}) public BuildStoreKind storeKind;
    @Param({"map"}) public BuildCacheKind cacheKind;
    @Param({"coroutine"}) public BuildShareKind shareKind;
    @Param({"validation"}) public BuildLayerKind layerKind;
    @Param({"trace"}) public BuildLoggerKind loggerKind;

    public enum BuildStoreKind {
        lmdb {
            @Override public Provider<Store> provider(PPath storePath) {
                final LMDBBuildStoreFactory factory = injector.getInstance(LMDBBuildStoreFactory.class);
                final File localStorePath = pathSrv.localPath(storePath);
                if(localStorePath == null) {
                    throw new RuntimeException(
                        "Cannot create PIE LMDB store at " + storePath + " because it is not on the local filesystem");
                }
                return () -> factory.create(localStorePath, 1024 * 1024 * 1024, 1024);
            }
        },
        in_memory {
            @Override public Provider<Store> provider(PPath storePath) {
                return InMemoryStore::new;
            }
        },
        in_memory_path_only {
            @Override public Provider<Store> provider(PPath storePath) {
                return InMemoryPathOnlyStore::new;
            }
        },
        noop {
            @Override public Provider<Store> provider(PPath storePath) {
                return NoopStore::new;
            }
        };

        public abstract Provider<Store> provider(PPath storePath);
    }

    public enum BuildCacheKind {
        map {
            @Override public Provider<Cache> provider() {
                return MapCache::new;
            }
        },
        noop {
            @Override public Provider<Cache> provider() {
                return NoopCache::new;
            }
        };

        public abstract Provider<Cache> provider();
    }

    public enum BuildShareKind {
        coroutine {
            @Override public Provider<BuildShare> provider() {
                return CoroutineBuildShare::new;
            }
        },
        non_sharing {
            @Override public Provider<BuildShare> provider() {
                return NonSharingBuildShare::new;
            }
        };

        public abstract Provider<BuildShare> provider();
    }

    public enum BuildLayerKind {
        validation {
            @Override public Provider<Layer> provider() {
                return () -> SpoofaxPieStaticState.injector.getInstance(ValidationLayer.class);
            }
        },
        noop {
            @Override public Provider<Layer> provider() {
                return NoopLayer::new;
            }
        };

        public abstract Provider<Layer> provider();
    }

    public enum BuildLoggerKind {
        trace {
            @Override public Provider<Logger> provider() {
                return TraceLogger::new;
            }
        },
        log {
            @Override public Provider<Logger> provider() {
                return () -> SpoofaxPieStaticState.injector.getInstance(LogLogger.class);
            }
        },
        stdout {
            @Override public Provider<Logger> provider() {
                return StreamLogger::new;
            }
        },
        noop {
            @Override public Provider<Logger> provider() {
                return NoopLogger::new;
            }
        };

        public abstract Provider<Logger> provider();
    }
}
