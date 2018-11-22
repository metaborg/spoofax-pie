package mb.spoofax.pie.sdf3

import com.google.inject.Inject
import mb.fs.java.JavaFSPath
import mb.log.api.Logger
import mb.pie.api.*
import mb.pie.api.fs.stamp.FileSystemStampers
import mb.spoofax.pie.config.ParseWorkspaceConfig
import mb.spoofax.pie.config.requireConfigValue
import mb.spoofax.pie.legacy.*
import mb.spoofax.runtime.cfg.LangId
import mb.spoofax.runtime.jsglr.Table
import org.metaborg.core.action.EndNamedGoal
import org.metaborg.sdf2table.io.ParseTableGenerator
import org.metaborg.spoofax.core.build.SpoofaxCommonPaths
import java.io.Serializable

class SDF3ToJSGLRParseTable
@Inject constructor(
  logFactory: Logger,
  private val parseWorkspaceConfig: ParseWorkspaceConfig,
  private val legacyLoadProject: LegacyLoadProject
) : TaskDef<SDF3ToJSGLRParseTable.Input, Table?> {
  val log: Logger = logFactory.forContext(SDF3ToJSGLRParseTable::class.java)

  companion object {
    const val id = "sdf3.ToJSGLRParseTable"
  }

  data class Input(
    val langId: LangId,
    val root: JavaFSPath
  ) : Serializable

  data class LangSpecConfigInfo(
    val dir: JavaFSPath,
    val files: List<JavaFSPath>,
    val mainFile: JavaFSPath?
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Table? {
    val (langId, root) = input

    val (langSpecDir, files, mainFile) = requireConfigValue(this, parseWorkspaceConfig, root) { workspaceConfig ->
      val langSpecConfig = workspaceConfig.langSpecConfigForId(langId)
      if(langSpecConfig != null) {
        LangSpecConfigInfo(langSpecConfig.dir(), langSpecConfig.syntaxParseFiles(), langSpecConfig.syntaxParseMainFile())
      } else {
        null
      }
    } ?: throw ExecException("Could not get language specification configuration for language with identifier $langId")
    if(mainFile == null || files.isEmpty()) {
      return null
    }
    if(!files.contains(mainFile)) {
      throw ExecException("SDF3 main file $mainFile is not in the list of files $files")
    }

    val langSpecProject = require(legacyLoadProject, langSpecDir).v
    val outputs = processAll(files.map { it.toNode() }, langSpecProject, true, EndNamedGoal("to Normal Form (abstract)"), FileSystemStampers.hash, FileSystemStampers.modified, log)

    // Create table
    // Main input file
    val mainResource = outputs.firstOrNull { it.inputFile == mainFile }?.outputFile
      ?: throw ExecException("Main file $mainFile could not be normalized")
    // Output file
    val spoofaxPaths = SpoofaxCommonPaths(langSpecProject.location())
    val vfsOutputFile = spoofaxPaths.targetMetaborgDir().resolveFile("sdf-new.tbl")
    val outputFile = Spx.spoofax().resourceService.localPath(vfsOutputFile)
      ?: throw ExecException("Parse table output file $vfsOutputFile is not on the local file system")
    // Paths
    val srcGenSyntaxDir = langSpecDir.appendSegments("src-gen", "syntax")
    val paths = listOf(srcGenSyntaxDir.toString())
    // Create table and make dependencies
    require(mainResource, FileSystemStampers.modified)
    val generator = ParseTableGenerator(mainResource.javaPath.toFile(), outputFile, null, null, paths)
    generator.outputTable(false, false, false)
    provide(vfsOutputFile.fsPath, FileSystemStampers.modified)
    return Table(vfsOutputFile.fsNode.readAllBytes())
  }
}
