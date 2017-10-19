package mb.spoofax.runtime.pie.builder.stratego

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.core.*
import mb.spoofax.runtime.impl.cfg.StrategoConfig
import mb.spoofax.runtime.impl.stratego.StrategoCompiler
import mb.vfs.path.*
import java.io.IOException
import java.nio.charset.Charset

class CompileStratego
@Inject constructor(private val log: Logger, private val pathSrv: PathSrv)
  : Func<StrategoConfig, PPath?> {
  companion object {
    val id = "compileStratego"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: StrategoConfig): PPath? {
    val compiler = StrategoCompiler()
    val result = compiler.compile(input)
    if(result == null) {
      // Make manual dependencies, since no depfile is generated if compilation fails.
      require(input.mainFile(), PathStampers.hash)
      input.includeFiles().forEach { require(it, PathStampers.hash) }
      input.includeDirs().forEach { require(it, PathStampers.hash(PPaths.extensionsPathWalker(listOf("str", "rtree")))) }
      return null
    }
    generate(result.outputFile)
    generate(result.depFile)
    requiredPaths(result.depFile).forEach { require(it) }
    return result.outputFile
  }

  override fun mayOverlap(input1: StrategoConfig, input2: StrategoConfig): Boolean {
    return input1.outputFile() == input2.outputFile();
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

fun ExecContext.compileStratego(input: StrategoConfig) = requireOutput(CompileStratego::class.java, input)