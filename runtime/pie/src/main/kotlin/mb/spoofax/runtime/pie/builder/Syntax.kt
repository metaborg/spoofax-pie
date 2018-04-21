package mb.spoofax.runtime.pie.builder

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.builtin.util.Tuple3
import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.PathStampers
import mb.spoofax.runtime.impl.sdf.Signatures
import mb.spoofax.runtime.impl.sdf.Table
import mb.spoofax.runtime.model.message.Msg
import mb.spoofax.runtime.model.parse.Token
import mb.spoofax.runtime.pie.builder.core.*
import mb.spoofax.runtime.pie.generated.createWorkspaceConfig
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
import org.metaborg.core.action.EndNamedGoal
import org.metaborg.sdf2table.io.ParseTableGenerator
import org.metaborg.spoofax.core.build.SpoofaxCommonPaths
import org.spoofax.interpreter.terms.IStrategoTerm
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory
import org.spoofax.terms.TermFactory
import java.io.Serializable
import java.util.*

class GenerateTable
@Inject constructor(
  log: Logger,
  private val pathSrv: PathSrv
) : Func<GenerateTable.Input, Table?> {
  val log: Logger = log.forContext(GenerateTable::class.java)

  companion object {
    val id = "spoofaxGenerateTable"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Table? {
    val (langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.Companion.id, root)
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
    output.reqFiles.forEach { require(it, PathStampers.hash) }
    output.genFiles.forEach { generate(it, PathStampers.hash) }

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
    require(mainResource, PathStampers.hash);
    val generator = ParseTableGenerator(mainResourceLocal, outputFile, null, null, paths)
    generator.outputTable(false, false, false)
    generate(vfsOutputFile.pPath)
    return Table(vfsOutputFile.pPath)
  }
}

class GenerateSignatures
@Inject constructor(
  log: Logger,
  private val pathSrv: PathSrv
) : Func<GenerateSignatures.Input, Signatures?> {
  val log: Logger = log.forContext(GenerateSignatures::class.java)

  companion object {
    val id = "spoofaxGenerateSignatures"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Signatures? {
    val (langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.Companion.id, root)
        ?: throw ExecException("Could not create workspace config for root $root")
    val metaLangExt = "sdf3"
    val metaLangConfig = workspace.spxCoreConfigForExt(metaLangExt)
      ?: throw ExecException("Could not get meta-language config for extension $metaLangExt")
    val metaLangImpl = buildOrLoad(metaLangConfig)
    val langSpec =
      workspace.langSpecConfigForExt(input.langSpecExt)
        ?: throw ExecException("Could not get language specification config for extension $langSpecExt")
    val langSpecProject = loadProj(langSpec.dir())
    val files = langSpec.syntaxSignatureFiles() ?: return null
    val outputs = process(files, metaLangImpl, langSpecProject, true, EndNamedGoal("Generate Signature (concrete)"), log)
    outputs.reqFiles.forEach { require(it, PathStampers.hash) }
    outputs.genFiles.forEach { generate(it, PathStampers.hash) }

    val signatureFiles = outputs.outputs
      .filter { it.ast != null && it.outputFile != null }
      .map { it.outputFile!! }
      .toCollection(ArrayList())
    val includeDir = langSpec.dir().resolve("src-gen")
    return Signatures(signatureFiles, includeDir)
  }
}

class Parse : Func<Parse.Input, Parse.Output> {
  companion object {
    val id = "spoofaxParse"
  }

  data class Input(
    val text: String,
    val table: Table,
    val file: PPath,
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  data class Output(
    val ast: IStrategoTerm?,
    val tokenStream: ArrayList<Token>?,
    val messages: ArrayList<Msg>
  ) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Msg>>

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Output {
    val (text, table, file, langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.Companion.id, root)
        ?: throw ExecException("Could not create workspace config for root $root")
    val langSpec =
      workspace.langSpecConfigForExt(input.langSpecExt)
        ?: throw ExecException("Could not get language specification config for extension $langSpecExt")
    val startSymbol = langSpec.syntaxParseStartSymbolId()

    val termFactory = ImploderOriginTermFactory(TermFactory())
    val parser = table.createParser(termFactory)
    val output = parser.parse(text, startSymbol, file)
    return Output(output.ast, output.tokenStream, output.messages)
  }
}
