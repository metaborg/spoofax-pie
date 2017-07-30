package mb.pipe.run.ceres.clang

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.BuildException
import mb.ceres.Builder
import mb.ceres.Out
import mb.log.Logger
import mb.pipe.run.ceres.path.read
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
import java.io.IOException

class ExtractCompileDeps @Inject constructor(log: Logger, val pathSrv: PathSrv) : Builder<PPath, ArrayList<PPath>> {
  companion object {
    val id = "extractCompileDeps"
  }

  val log: Logger = log.forContext(ExtractCompileDeps::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: PPath): ArrayList<PPath> {
    val text = read(input)
    val regex = Regex("([^:]+): (.+)", RegexOption.DOT_MATCHES_ALL)
    val result = regex.matchEntire(text) ?: return ArrayList()
    val requiredStr = result.groupValues[2] // Group 0 is the entire string, 1 the generated path, 2 the required paths
    val required = requiredStr.split(' ')
    return required.map { pathSrv.resolveLocal(it) }.toCollection(ArrayList())
  }
}
