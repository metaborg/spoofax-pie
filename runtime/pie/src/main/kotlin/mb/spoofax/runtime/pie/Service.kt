package mb.spoofax.runtime.pie

import com.google.inject.Inject
import com.google.inject.Provider
import mb.pie.runtime.core.BuildManager
import mb.pie.runtime.core.BuildManagerFactory
import mb.pie.runtime.core.impl.BuildCache
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
import java.util.concurrent.ConcurrentHashMap

interface PieSrv {
  operator fun get(dir: PPath): BuildManager
}

class PieSrvImpl @Inject constructor(
  private val pathSrv: PathSrv,
  private val buildManagerFactory: BuildManagerFactory,
  private val storeFactory: LMDBBuildStoreFactory,
  private val cacheFactory: Provider<BuildCache>)
  : PieSrv {
  val buildManagers = ConcurrentHashMap<PPath, BuildManager>()


  override operator fun get(dir: PPath): BuildManager {
    return buildManagers.getOrPut(dir) {
      val storeDir = dir.resolve(".pie")
      val localStoreDir = pathSrv.localPath(storeDir) ?: throw RuntimeException("Cannot create PIE LMDB store at $storeDir because it is not on the local filesystem")
      val store = storeFactory.create(localStoreDir)
      val cache = cacheFactory.get()
      buildManagerFactory.create(store, cache)
    }
  }
}