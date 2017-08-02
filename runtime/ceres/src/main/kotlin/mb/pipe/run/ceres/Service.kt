package mb.pipe.run.ceres

import com.google.inject.Inject
import com.google.inject.Provider
import mb.ceres.BuildManager
import mb.ceres.BuildManagerFactory
import mb.ceres.impl.BuildCache
import mb.ceres.impl.store.LMDBBuildStoreFactory
import mb.pipe.run.core.PipeRunEx
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
import java.util.concurrent.ConcurrentHashMap

interface CeresSrv {
  operator fun get(dir: PPath): BuildManager
}

class CeresSrvImpl @Inject constructor(
  private val pathSrv: PathSrv,
  private val buildManagerFactory: BuildManagerFactory,
  private val storeFactory: LMDBBuildStoreFactory,
  private val cacheFactory: Provider<BuildCache>)
  : CeresSrv {
  val buildManagers = ConcurrentHashMap<PPath, BuildManager>()


  override operator fun get(dir: PPath): BuildManager {
    return buildManagers.getOrPut(dir) {
      val storeDir = dir.resolve(".ceres");
      val localStoreDir = pathSrv.localPath(storeDir);
      if (localStoreDir == null) {
        throw PipeRunEx("Cannot create Ceres LMDB store at $storeDir because it is not on the local filesystem");
      }
      val store = storeFactory.create(localStoreDir);
      val cache = cacheFactory.get()
      buildManagerFactory.create(store, cache);
    }
  }
}