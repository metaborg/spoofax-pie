package mb.spoofax.runtime.pie

import com.google.inject.Inject
import com.google.inject.Provider
import mb.pie.runtime.core.PollingExecManager
import mb.pie.runtime.core.PollingExecManagerFactory
import mb.pie.runtime.core.Cache
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
import java.util.concurrent.ConcurrentHashMap

interface PieSrv {
  operator fun get(dir: PPath): PollingExecManager
}

class PieSrvImpl @Inject constructor(
  private val pathSrv: PathSrv,
  private val pollingExecManagerFactory: PollingExecManagerFactory,
  private val storeFactory: LMDBBuildStoreFactory,
  private val cacheFactory: Provider<Cache>)
  : PieSrv {
  private val buildManagers = ConcurrentHashMap<PPath, PollingExecManager>()


  override operator fun get(dir: PPath): PollingExecManager {
    return buildManagers.getOrPut(dir) {
      val storeDir = dir.resolve(".pie")
      val localStoreDir = pathSrv.localPath(storeDir) ?: throw RuntimeException("Cannot create PIE LMDB store at $storeDir because it is not on the local filesystem")
      val store = storeFactory.create(localStoreDir)
      val cache = cacheFactory.get()
      pollingExecManagerFactory.create(store, cache)
    }
  }
}
