package mb.pipe.run.ceres.clang

import mb.pipe.run.core.StaticPipeFacade
import mb.vfs.path.PPath

fun extractCompileDeps(text: String): ArrayList<PPath> {
  val regex = Regex("([^:]+): (.+)", RegexOption.DOT_MATCHES_ALL)
  val result = regex.matchEntire(text) ?: return ArrayList()
  val requiredStr = result.groupValues[2] // Group 0 is the entire string, 1 the generated path, 2 the required paths
  val required = requiredStr.split(' ')
  val pathSrv = StaticPipeFacade.facade().pathSrv;
  return required.map { pathSrv.resolveLocal(it) }.toCollection(ArrayList())
}
