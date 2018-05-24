package mb.spoofax.runtime.pie

import mb.pie.vfs.path.PPath

fun shouldProcessFile(path: PPath): Boolean {
  val str = path.toString()
  return !str.contains("src-gen")
}
