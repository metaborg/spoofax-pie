package mb.spoofax.runtime.pie.sdf3

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.spoofax.runtime.impl.sdf.Table
import mb.spoofax.runtime.pie.generated.createWorkspaceConfig
import mb.spoofax.runtime.pie.legacy.*
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
import org.metaborg.core.action.EndNamedGoal
import org.metaborg.sdf2table.io.ParseTableGenerator
import org.metaborg.spoofax.core.build.SpoofaxCommonPaths
import java.io.Serializable

class CompileParseTable
@Inject constructor(
  log: Logger,
  private val pathSrv: PathSrv
) : TaskDef<CompileParseTable.Input, Table?> {
  val log: Logger = log.forContext(CompileParseTable::class.java)

  companion object {
    const val id = "sdf3.CompileParseTable"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Table? {
    val (langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class.java, createWorkspaceConfig.id, root)
        ?: throw ExecException("Could not create workspace config for root $root")
    val metaLangExt = "sdf3"
    val metaLangConfig = workspace.spxCoreConfigForExt(metaLangExt)
      ?: throw ExecException("Could not get meta-language config for extension $metaLangExt")
    val metaLangImpl = buildOrLoad(metaLangConfig)
    val langSpec =
      workspace.langSpecConfigForExt(input.langSpecExt)
        ?: throw ExecException("Could not get language specification config for extension $langSpecExt")
    val langSpecProject = loadProj(langSpec.dir())
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
    val srcGenSyntaxDir = langSpec.dir().resolve("src-gen/syntax");
    val paths = listOf(srcGenSyntaxDir.toString())
    // Create table and make dependencies
    require(mainResource, FileStampers.hash);
    val generator = ParseTableGenerator(mainResourceLocal, outputFile, null, null, paths)
    generator.outputTable(false, false, false)
    generate(vfsOutputFile.pPath)
    return Table(vfsOutputFile.pPath)
  }
}
