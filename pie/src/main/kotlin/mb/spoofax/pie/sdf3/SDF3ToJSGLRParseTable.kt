package mb.spoofax.pie.sdf3

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.pie.vfs.path.PathSrv
import mb.spoofax.pie.generated.createWorkspaceConfig
import mb.spoofax.pie.legacy.*
import mb.spoofax.runtime.sdf.Table
import org.metaborg.core.action.EndNamedGoal
import org.metaborg.sdf2table.io.ParseTableGenerator
import org.metaborg.spoofax.core.build.SpoofaxCommonPaths
import java.io.Serializable

class SDF3ToJSGLRParseTable
@Inject constructor(
  log: Logger,
  private val pathSrv: PathSrv,
  private val createWorkspaceConfig: createWorkspaceConfig,
  private val legacyBuildOrLoadLanguage: LegacyBuildOrLoadLanguage,
  private val legacyLoadProject: LegacyLoadProject
) : TaskDef<SDF3ToJSGLRParseTable.Input, Table?> {
  val log: Logger = log.forContext(SDF3ToJSGLRParseTable::class.java)

  companion object {
    const val id = "sdf3.ToJSGLRParseTable"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Table? {
    val (langSpecExt, root) = input

    // OPTO: only depend on Spoofax Core config for SDF3, and language specification config for langSpecExt.
    val workspaceConfig = require(createWorkspaceConfig, root)
      ?: throw ExecException("Could not get workspace config at root $root")

    val metaLangExt = "sdf3"
    val metaLangConfig = workspaceConfig.spxCoreConfigForExt(metaLangExt)
      ?: throw ExecException("Could not get meta-language config for extension $metaLangExt")
    val metaLangImpl = require(legacyBuildOrLoadLanguage.createTask(metaLangConfig)).v

    // OPTO: only depend on syntax parser files.
    val langSpec = workspaceConfig.langSpecConfigForExt(langSpecExt)
      ?: throw ExecException("Could not get language specification config for extension $langSpecExt")

    val langSpecProject = require(legacyLoadProject, langSpec.dir()).v

    val files = langSpec.syntaxParseFiles() ?: return null
    val mainFile = langSpec.syntaxParseMainFile() ?: return null
    if(!files.contains(mainFile)) {
      throw ExecException("SDF3 main file $mainFile is not in the list of files $files")
    }
    val output = process(files, metaLangImpl, langSpecProject, true, EndNamedGoal("to Normal Form (abstract)"), log)
    output.reqFiles.forEach { require(it, FileStampers.hash) }
    output.genFiles.forEach { generate(it, FileStampers.hash) }

    // Create table
    // Main input file
    val mainResource = output.outputs.firstOrNull { it.inputFile == mainFile }?.outputFile
      ?: throw ExecException("Main file $mainFile could not be normalized")
    val mainResourceLocal = pathSrv.localPath(mainResource)
      ?: throw ExecException("Normalized main file $mainResource is not on the local file system")
    // Output file
    val spoofaxPaths = SpoofaxCommonPaths(langSpecProject.location())
    val vfsOutputFile = spoofaxPaths.targetMetaborgDir().resolveFile("sdf-new.tbl")
    val outputFile = Spx.spoofax().resourceService.localPath(vfsOutputFile)
      ?: throw ExecException("Parse table output file $vfsOutputFile is not on the local file system")
    // Paths
    val srcGenSyntaxDir = langSpec.dir().resolve("src-gen/syntax")
    val paths = listOf(srcGenSyntaxDir.toString())
    // Create table and make dependencies
    require(mainResource, FileStampers.hash)
    val generator = ParseTableGenerator(mainResourceLocal, outputFile, null, null, paths)
    generator.outputTable(false, false, false)
    generate(vfsOutputFile.pPath)
    return Table(vfsOutputFile.pPath)
  }
}
