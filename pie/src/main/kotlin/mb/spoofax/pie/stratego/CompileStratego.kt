package mb.spoofax.pie.stratego

import mb.fs.api.node.match.PathNodeMatcher
import mb.fs.api.path.match.ExtensionsPathMatcher
import mb.fs.java.JavaFSNode
import mb.fs.java.JavaFSPath
import mb.pie.api.*
import mb.pie.api.fs.stamp.FileSystemStampers
import mb.spoofax.runtime.cfg.StrategoCompilerConfig
import mb.spoofax.runtime.stratego.StrategoCompiler
import java.io.IOException
import java.io.Serializable
import java.nio.charset.Charset

class CompileStratego : TaskDef<CompileStratego.Input, JavaFSPath?> {
  companion object {
    const val id = "stratego.Compile"
  }

  data class Input(
    val config: StrategoCompilerConfig,
    val taskDeps: Iterable<STask<*>>
  ) : Serializable

  override val id = Companion.id
  override fun key(input: Input): JavaFSPath = input.config.outputFileOrDefault()
  override fun ExecContext.exec(input: Input): JavaFSPath? {
    val (config, taskDeps) = input
    // Explicitly require hidden dependencies.
    taskDeps.forEach {
      require(it)
    }
    // Compile Stratego.
    val compiler = StrategoCompiler()
    val result = compiler.compile(config)
    if(result == null) {
      // Make manual dependencies, since no depfile is generated if compilation fails.
      require(config.mainFile(), FileSystemStampers.hash)
      config.includeFiles().forEach { require(it, FileSystemStampers.hash) }
      config.includeDirs().forEach { require(it, FileSystemStampers.hash(PathNodeMatcher(ExtensionsPathMatcher("str", "rtree")))) }
      return null
    }
    provide(result.outputFile)
    provide(result.depFile)
    requiredPaths(result.depFile).forEach { require(it, FileSystemStampers.hash) }
    return result.outputFile.path
  }

  @Throws(IOException::class)
  private fun requiredPaths(depFile: JavaFSNode) =
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
          JavaFSPath(pathStr)
        }
      }
}
