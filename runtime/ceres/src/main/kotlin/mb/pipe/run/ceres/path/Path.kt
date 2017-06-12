package mb.pipe.run.ceres.path

import mb.ceres.*
import mb.pipe.run.core.StaticPipeFacade
import mb.pipe.run.core.path.PPath
import mb.pipe.run.core.path.PPathImpl
import java.io.IOException
import java.nio.file.Files

val CPath.pPath get() = PPathImpl(this.javaPath)
val PPath.cPath get() = CPath(this.javaPath)

fun resolve(uriStr: String): PPath {
  return StaticPipeFacade.facade().pathSrv.resolve(uriStr)
}


class Read : Builder<PPath, String> {
  override val id = "read"
  override fun BuildContext.build(input: PPath): String {
    require(input.cPath)
    try {
      return String(Files.readAllBytes(input.javaPath))
    } catch(e: IOException) {
      throw BuildException("Reading '$input' failed", e)
    }
  }
}

fun BuildContext.read(input: PPath) = requireOutput(Read::class.java, input)


class Copy : OutEffectBuilder<Copy.Input> {
  data class Input(val from: PPath, val to: PPath) : In

  override val id = "read"
  override fun BuildContext.effect(input: Input) {
    val (from, to) = input
    require(from.cPath)
    try {
      Files.copy(from.javaPath, to.javaPath)
    } catch(e: IOException) {
      throw BuildException("Copying '${input.from}' to '${input.to}' failed", e)
    }
    generate(to.cPath)
  }
}

fun BuildContext.copy(input: Copy.Input) = requireOutput(Copy::class.java, input)
