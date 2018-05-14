package mb.spoofax.runtime.pie.stratego

import com.google.inject.Inject
import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.PathStampers
import mb.spoofax.runtime.impl.cfg.StrategoConfig
import mb.spoofax.runtime.impl.stratego.StrategoCompiler
import mb.vfs.path.*
import java.io.IOException
import java.io.Serializable
import java.nio.charset.Charset

class Compile
@Inject constructor(
  private val pathSrv: PathSrv
) : Func<Compile.Input, PPath?> {
  companion object {
    const val id = "Stratego.Compile"
  }

  data class Input(
    val config: StrategoConfig,
    val taskDeps: Iterable<UFuncApp>
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): PPath? {
    val (config, taskDeps) = input
    // Explicitly require hidden dependencies.
    taskDeps.forEach {
      requireExec(it)
    }
    // Compile Stratego.
    val compiler = StrategoCompiler()
    val result = compiler.compile(config)
    if(result == null) {
      // Make manual dependencies, since no depfile is generated if compilation fails.
      require(config.mainFile(), PathStampers.hash)
      config.includeFiles().forEach { require(it, PathStampers.hash) }
      config.includeDirs().forEach { require(it, PathStampers.hash(PPaths.extensionsPathWalker(listOf("str", "rtree")))) }
      return null
    }
    generate(result.outputFile)
    generate(result.depFile)
    requiredPaths(result.depFile).forEach { require(it, PathStampers.hash) }
    return result.outputFile
  }

  @Throws(IOException::class)
  private fun requiredPaths(depFile: PPath) =
    depFile
      .readAllLines(Charset.defaultCharset())
      .drop(1) // Skip first line (start at 1 instead of 0), which lists the generated CTree file.
      .mapNotNull { line ->
        // Remove leading and trailing whitespace.
        val trimmedLine = line.trim { it <= ' ' }
        val length = trimmedLine.length
        if(length < 3) {
          // Don't process empty lines, i.e. lines with just ' /' or '/'.
          null
        } else {
          // Remove the trailing ' /'.
          val pathStr = trimmedLine.substring(0, length - 2)
          val path = pathSrv.resolveLocal(pathStr)
          path
        }
      }
}

fun ExecContext.compileStratego(input: Compile.Input) = requireOutput(Compile::class, Compile.id, input)
fun ExecContext.compileStratego(config: StrategoConfig, taskDeps: Iterable<UFuncApp>) = compileStratego(Compile.Input(config, taskDeps))
