package mb.spoofax.runtime.pie.stratego

import com.google.inject.Inject
import mb.pie.runtime.*
import mb.pie.runtime.stamp.FileStampers
import mb.spoofax.runtime.impl.cfg.StrategoConfig
import mb.spoofax.runtime.impl.stratego.StrategoCompiler
import mb.vfs.path.*
import java.io.IOException
import java.io.Serializable
import java.nio.charset.Charset

class Compile
@Inject constructor(
  private val pathSrv: PathSrv
) : TaskDef<Compile.Input, PPath?> {
  companion object {
    const val id = "stratego.Compile"
  }

  data class Input(
    val config: StrategoConfig,
    val taskDeps: Iterable<UTask>
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
      require(config.mainFile(), FileStampers.hash)
      config.includeFiles().forEach { require(it, FileStampers.hash) }
      config.includeDirs().forEach { require(it, FileStampers.hash(PPaths.extensionsPathWalker(listOf("str", "rtree")))) }
      return null
    }
    generate(result.outputFile)
    generate(result.depFile)
    requiredPaths(result.depFile).forEach { require(it, FileStampers.hash) }
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

fun ExecContext.compileStratego(input: Compile.Input) = requireOutput(Compile::class.java, Compile.id, input)
fun ExecContext.compileStratego(config: StrategoConfig, taskDeps: Iterable<UTask>) = compileStratego(Compile.Input(config, taskDeps))
