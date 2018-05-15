package mb.spoofax.runtime.pie

import com.google.inject.Inject
import com.google.inject.Provider
import mb.pie.runtime.Cache
import mb.pie.runtime.Store
import mb.pie.runtime.exec.*
import mb.pie.runtime.impl.store.InMemoryStore
import mb.pie.runtime.impl.store.LMDBBuildStoreFactory
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
import java.util.concurrent.ConcurrentHashMap


interface PieSrv {
  fun getTopDownExecutor(dir: PPath, useInMemoryStore: Boolean): TopDownExecutor
  fun getBottomUpExecutor(dir: PPath, useInMemoryStore: Boolean): BottomUpExecutor
}

class PieSrvImpl @Inject constructor(
  private val pathSrv: PathSrv,
  private val storeFactory: LMDBBuildStoreFactory,
  private val cacheFactory: Provider<Cache>,
  private val topDownExecutorFactory: TopDownExecutorFactory,
  private val bottomUpExecutorFactory: BottomUpExecutorFactory
) : PieSrv {
  private val stores = ConcurrentHashMap<PPath, Store>()
  private val caches = ConcurrentHashMap<PPath, Cache>()
  private val topDownExecutors = ConcurrentHashMap<PPath, TopDownExecutor>()
  private val bottomUpExecutors = ConcurrentHashMap<PPath, BottomUpExecutor>()


  override fun getTopDownExecutor(dir: PPath, useInMemoryStore: Boolean): TopDownExecutor {
    return topDownExecutors.getOrPut(dir) {
      val store = getStore(dir, useInMemoryStore)
      val cache = getCache(dir)
      topDownExecutorFactory.create(store, cache)
    }
  }

  override fun getBottomUpExecutor(dir: PPath, useInMemoryStore: Boolean): BottomUpExecutor {
    return bottomUpExecutors.getOrPut(dir) {
      val store = getStore(dir, useInMemoryStore)
      val cache = getCache(dir)
      bottomUpExecutorFactory.create(store, cache)
    }
  }


  private fun getStore(dir: PPath, useInMemoryStore: Boolean) = stores.getOrPut(dir) {
    if(useInMemoryStore) {
      InMemoryStore()
    } else {
      val storeDir = dir.resolve(".pie")
      val localStoreDir = pathSrv.localPath(storeDir)
        ?: throw RuntimeException("Cannot create PIE LMDB store at $storeDir because it is not on the local filesystem")
      storeFactory.create(localStoreDir)
    }
  }

  private fun getCache(dir: PPath) = caches.getOrPut(dir) {
    cacheFactory.get()
  }
}
