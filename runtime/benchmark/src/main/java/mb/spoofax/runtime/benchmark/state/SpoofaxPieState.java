package mb.spoofax.runtime.benchmark.state;

import com.google.inject.Key;
import com.google.inject.Provider;
import mb.pie.runtime.builtin.util.LogBuildLogger;
import mb.pie.runtime.core.*;
import mb.pie.runtime.core.impl.BuildImpl;
import mb.pie.runtime.core.impl.cache.MapBuildCache;
import mb.pie.runtime.core.impl.cache.NoopBuildCache;
import mb.pie.runtime.core.impl.layer.NoopBuildLayer;
import mb.pie.runtime.core.impl.layer.ValidationBuildLayer;
import mb.pie.runtime.core.impl.logger.NoopBuildLogger;
import mb.pie.runtime.core.impl.logger.StreamBuildLogger;
import mb.pie.runtime.core.impl.logger.TraceBuildLogger;
import mb.pie.runtime.core.impl.share.BuildShare;
import mb.pie.runtime.core.impl.share.CoroutineBuildShare;
import mb.pie.runtime.core.impl.share.NonSharingBuildShare;
import mb.pie.runtime.core.impl.store.InMemoryBuildStore;
import mb.pie.runtime.core.impl.store.InMemoryPathOnlyBuildStore;
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory;
import mb.pie.runtime.core.impl.store.NoopBuildStore;
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


    public BuildStore store;
    public BuildCache cache;
    public BuildShare share;
    public BuildLayer layer;
    public BuildLogger logger;
    public Map<String, Builder<?, ?>> builders;
    public BuildImpl build;

    public void setupBuild() {
        this.store = storeKind.provider(storePath).get();
        try(final BuildStoreWriteTxn txn = this.store.writeTxn()) {
            txn.drop();
        }
        this.cache = cacheKind.provider().get();
        this.cache.drop();
        this.share = shareKind.provider().get();
        this.layer = layerKind.provider().get();
        this.logger = loggerKind.provider().get();
        this.builders = injector.getInstance(new Key<Map<String, Builder<?, ?>>>() {
        });
        this.build = new BuildImpl(store, cache, share, layer, logger, builders, injector);
    }

    public void renewBuild() {
        this.layer = layerKind.provider().get();
        this.logger = loggerKind.provider().get();
        this.build = new BuildImpl(store, cache, share, layer, logger, builders, injector);
    }

    public void resetBuild() {
        try(final BuildStoreWriteTxn txn = this.store.writeTxn()) {
            txn.drop();
        }
        this.cache.drop();
    }

    public BuildInfo<PPath, ? extends Serializable> runBuild() {
        final BuildApp<PPath, ? extends Serializable> app = new BuildApp<>("processWorkspace", workspaceRoot);
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
            @Override public Provider<BuildStore> provider(PPath storePath) {
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
            @Override public Provider<BuildStore> provider(PPath storePath) {
                return InMemoryBuildStore::new;
            }
        },
        in_memory_path_only {
            @Override public Provider<BuildStore> provider(PPath storePath) {
                return InMemoryPathOnlyBuildStore::new;
            }
        },
        noop {
            @Override public Provider<BuildStore> provider(PPath storePath) {
                return NoopBuildStore::new;
            }
        };

        public abstract Provider<BuildStore> provider(PPath storePath);
    }

    public enum BuildCacheKind {
        map {
            @Override public Provider<BuildCache> provider() {
                return MapBuildCache::new;
            }
        },
        noop {
            @Override public Provider<BuildCache> provider() {
                return NoopBuildCache::new;
            }
        };

        public abstract Provider<BuildCache> provider();
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
            @Override public Provider<BuildLayer> provider() {
                return () -> SpoofaxPieStaticState.injector.getInstance(ValidationBuildLayer.class);
            }
        },
        noop {
            @Override public Provider<BuildLayer> provider() {
                return NoopBuildLayer::new;
            }
        };

        public abstract Provider<BuildLayer> provider();
    }

    public enum BuildLoggerKind {
        trace {
            @Override public Provider<BuildLogger> provider() {
                return TraceBuildLogger::new;
            }
        },
        log {
            @Override public Provider<BuildLogger> provider() {
                return () -> SpoofaxPieStaticState.injector.getInstance(LogBuildLogger.class);
            }
        },
        stdout {
            @Override public Provider<BuildLogger> provider() {
                return StreamBuildLogger::new;
            }
        },
        noop {
            @Override public Provider<BuildLogger> provider() {
                return NoopBuildLogger::new;
            }
        };

        public abstract Provider<BuildLogger> provider();
    }
}
