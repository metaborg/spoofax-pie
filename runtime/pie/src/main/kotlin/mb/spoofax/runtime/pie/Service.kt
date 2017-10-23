package mb.spoofax.runtime.pie

import com.google.inject.Inject
import com.google.inject.Provider
import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
import java.util.concurrent.ConcurrentHashMap


interface PieSrv {
  fun getPullingExecutor(dir: PPath): PullingExecutor
  fun getPushingExecutor(dir: PPath): PushingExecutor
}

class PieSrvImpl @Inject constructor(
  private val pathSrv: PathSrv,
  private val storeFactory: LMDBBuildStoreFactory,
  private val cacheFactory: Provider<Cache>,
  private val pullingExecutorFactory: PullingExecutorFactory,
  private val pushingExecutorFactory: PushingExecutorFactory
) : PieSrv {
  private val pullingExecutors = ConcurrentHashMap<PPath, PullingExecutor>()
  private val pushingExecutors = ConcurrentHashMap<PPath, PushingExecutor>()


  override fun getPullingExecutor(dir: PPath): PullingExecutor {
    return pullingExecutors.getOrPut(dir) {
      val storeDir = dir.resolve(".pie")
      val localStoreDir = pathSrv.localPath(storeDir) ?: throw RuntimeException("Cannot create PIE LMDB store at $storeDir because it is not on the local filesystem")
      val store = storeFactory.create(localStoreDir)
      val cache = cacheFactory.get()
      pullingExecutorFactory.create(store, cache)
    }
  }

  override fun getPushingExecutor(dir: PPath): PushingExecutor {
    return pushingExecutors.getOrPut(dir) {
      val storeDir = dir.resolve(".pie")
      val localStoreDir = pathSrv.localPath(storeDir) ?: throw RuntimeException("Cannot create PIE LMDB store at $storeDir because it is not on the local filesystem")
      val store = storeFactory.create(localStoreDir)
      val cache = cacheFactory.get()
      pushingExecutorFactory.create(store, cache)
    }
  }
}
