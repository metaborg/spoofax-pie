package mb.spoofax.runtime.pie.builder.stratego

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.PathStampers
import mb.spoofax.runtime.impl.cfg.StrategoConfig
import mb.spoofax.runtime.impl.stratego.StrategoCompiler
import mb.vfs.path.*
import java.io.IOException
import java.io.Serializable
import java.nio.charset.Charset

class CompileStratego
@Inject constructor(
  private val log: Logger,
  private val pathSrv: PathSrv
) : Func<CompileStratego.Input, PPath?> {
  companion object {
    val id = "compileStratego"
  }

  data class Input(
    val config: StrategoConfig,
    val apps: Iterable<UFuncApp>
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): PPath? {
    val (config, apps) = input

    // Explicitly require hidden dependencies.
    apps.forEach {
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

  override fun mayOverlap(input1: Input, input2: Input): Boolean {
    return input1.config.outputFile() == input2.config.outputFile()
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

//fun ExecContext.compileStratego(input: StrategoConfig) = requireOutput(CompileStratego::class, CompileStratego.Companion.id, input)