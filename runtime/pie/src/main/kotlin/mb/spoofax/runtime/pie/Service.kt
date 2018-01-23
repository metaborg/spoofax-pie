package mb.spoofax.runtime.pie

import com.google.inject.Inject
import com.google.inject.Provider
import mb.pie.runtime.core.Cache
import mb.pie.runtime.core.exec.PullingExecutor
import mb.pie.runtime.core.exec.PullingExecutorFactory
import mb.pie.runtime.core.exec.DirtyFlaggingExecutor
import mb.pie.runtime.core.exec.DirtyFlaggingExecutorFactory
import mb.pie.runtime.core.Store
import mb.pie.runtime.core.impl.store.InMemoryStore
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
import java.util.concurrent.ConcurrentHashMap


interface PieSrv {
  fun getPullingExecutor(dir: PPath, useInMemoryStore: Boolean): PullingExecutor
  fun getPushingExecutor(dir: PPath, useInMemoryStore: Boolean): DirtyFlaggingExecutor
}

class PieSrvImpl @Inject constructor(
  private val pathSrv: PathSrv,
  private val storeFactory: LMDBBuildStoreFactory,
  private val cacheFactory: Provider<Cache>,
  private val pullingExecutorFactory: PullingExecutorFactory,
  private val dirtyFlaggingExecutorFactory: DirtyFlaggingExecutorFactory
) : PieSrv {
  private val stores = ConcurrentHashMap<PPath, Store>()
  private val caches = ConcurrentHashMap<PPath, Cache>()
  private val pullingExecutors = ConcurrentHashMap<PPath, PullingExecutor>()
  private val pushingExecutors = ConcurrentHashMap<PPath, DirtyFlaggingExecutor>()


  override fun getPullingExecutor(dir: PPath, useInMemoryStore: Boolean): PullingExecutor {
    return pullingExecutors.getOrPut(dir) {
      val store = getStore(dir, useInMemoryStore)
      val cache = getCache(dir)
      pullingExecutorFactory.create(store, cache)
    }
  }

  override fun getPushingExecutor(dir: PPath, useInMemoryStore: Boolean): DirtyFlaggingExecutor {
    return pushingExecutors.getOrPut(dir) {
      val store = getStore(dir, useInMemoryStore)
      val cache = getCache(dir)
      dirtyFlaggingExecutorFactory.create(store, cache)
    }
  }


  private fun getStore(dir: PPath, useInMemoryStore: Boolean) = stores.getOrPut(dir) {
    if (useInMemoryStore) {
      InMemoryStore()
    } else {
      val storeDir = dir.resolve(".pie")
      val localStoreDir = pathSrv.localPath(storeDir) ?: throw RuntimeException("Cannot create PIE LMDB store at $storeDir because it is not on the local filesystem")
      storeFactory.create(localStoreDir)
    }
  }

  private fun getCache(dir: PPath) = caches.getOrPut(dir) {
    cacheFactory.get()
  }
}
