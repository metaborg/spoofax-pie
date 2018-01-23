package mb.spoofax.runtime.pie.builder

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.builtin.path.read
import mb.pie.runtime.builtin.util.Tuple3
import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.PathStampers
import mb.spoofax.runtime.impl.cfg.SpxCoreConfig
import mb.spoofax.runtime.impl.sdf.Signatures
import mb.spoofax.runtime.impl.sdf.Table
import mb.spoofax.runtime.model.message.Msg
import mb.spoofax.runtime.model.parse.Token
import mb.spoofax.runtime.pie.builder.core.*
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
@Inject constructor(log: Logger, private val pathSrv: PathSrv)
  : Func<GenerateTable.Input, Table> {
  companion object {
    val id = "spoofaxGenerateTable"
  }

  data class Input(val sdfLangConfig: SpxCoreConfig, val specDir: PPath, val files: Iterable<PPath>, val mainFile: PPath) : Serializable

  val log: Logger = log.forContext(GenerateTable::class.java)

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Table {
    val (langConfig, projDir, files, mainFile) = input

    if(!files.contains(mainFile)) {
      throw ExecException("SDF3 main file $mainFile is not in the list of files $files")
    }

    // Read input files
    val textFilePairs = files.mapNotNull {
      val text = read(it)
      if(text == null) {
        log.error("Unable to read SDF3 file $it (it does not exist), skipping")
        null
      } else {
        CoreParseAll.TextFilePair(text, it)
      }
    }

    // Parse input files
    val parsed = parseAll(langConfig, textFilePairs)
    parsed.forEach { if(it.ast == null) log.error("Unable to parse SDF3 file ${it.file}, skipping") }

    // Load project, required for analysis and transformation.
    val proj = loadProj(projDir)

    // Transform
    val transformGoal = EndNamedGoal("to Normal Form (abstract)")
    val transformPairs = parsed
      .filter { it.ast != null }
      .map { CoreTransAll.AstFilePair(it.ast!!, it.file) }
    val transformed = transAll(langConfig, proj.path, transformGoal, transformPairs)
    transformed.forEach { if(it.ast == null || it.outputFile == null) log.error("Unable to transform SDF3 file ${it.inputFile} with $transformGoal, skipping") }

    // Create table
    // Main input file
    val mainResource = transformed.firstOrNull { it.inputFile == mainFile }?.outputFile ?: throw ExecException("Main file " + input.mainFile + " could not be normalized")
    val mainResourceLocal = pathSrv.localPath(mainResource) ?: throw ExecException("Normalized main file $mainResource is not on the local file system")
    // Output file
    val spoofaxPaths = SpoofaxCommonPaths(proj.location())
    val vfsOutputFile = spoofaxPaths.targetMetaborgDir().resolveFile("sdf-new.tbl")
    val outputFile = Spx.spoofax().resourceService.localPath(vfsOutputFile) ?: throw ExecException("Parse table output file $vfsOutputFile is not on the local file system")
    // Paths
    val srcGenSyntaxDir = input.specDir.resolve("src-gen/syntax");
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
@Inject constructor(log: Logger, private val pathSrv: PathSrv)
  : Func<GenerateSignatures.Input, Signatures> {
  companion object {
    val id = "spoofaxGenerateSignatures"
  }

  data class Input(val sdfLangConfig: SpxCoreConfig, val specDir: PPath, val files: Iterable<PPath>) : Serializable

  val log: Logger = log.forContext(GenerateSignatures::class.java)

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Signatures {
    val (langConfig, projDir, files) = input

    // Read input files
    val textFilePairs = files.mapNotNull {
      val text = read(it)
      if(text == null) {
        log.error("Unable to read SDF3 file $it (it does not exist), skipping")
        null
      } else {
        CoreParseAll.TextFilePair(text, it)
      }
    }

    // Parse input files
    val parsed = parseAll(langConfig, textFilePairs)
    parsed.forEach { if(it.ast == null) log.error("Unable to parse SDF3 file ${it.file}, skipping") }

    // Load project, required for analysis and transformation.
    val proj = loadProj(projDir)

    // Analyze
    val analyzePairs = parsed
      .filter { it.ast != null }
      .map { CoreAnalyzeAll.AstFilePair(it.ast!!, it.file) }
    val analyzed = analyzeAll(langConfig, proj.path, analyzePairs)
    analyzed.forEach { if(it.ast == null) log.error("Unable to analyze SDF3 file ${it.file}, skipping") }

    // Transform
    val transformGoal = EndNamedGoal("Generate Signature (concrete)")
    val transformPairs = analyzed
      .filter { it.ast != null }
      .map { CoreTransAll.AstFilePair(it.ast!!, it.file) }
    val transformed = transAll(langConfig, proj.path, transformGoal, transformPairs)
    transformed.forEach { if(it.ast == null || it.outputFile == null) log.error("Unable to transform SDF3 file ${it.inputFile} with $transformGoal, skipping") }

    val signatureFiles = transformed
      .filter { it.ast != null && it.outputFile != null }
      .map { it.outputFile!! }
      .toCollection(ArrayList())
    val includeDir = projDir.resolve("src-gen")
    return Signatures(signatureFiles, includeDir)
  }
}

class Parse : Func<Parse.Input, Parse.Output> {
  companion object {
    val id = "spoofaxParse"
  }

  data class Input(val text: String, val startSymbol: String, val table: Table, val file: PPath) : Serializable
  data class Output(val ast: IStrategoTerm?, val tokenStream: ArrayList<Token>?, val messages: ArrayList<Msg>) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Msg>>

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Output {
    val termFactory = ImploderOriginTermFactory(TermFactory())
    val parser = input.table.createParser(termFactory)
    val output = parser.parse(input.text, input.startSymbol, input.file)

    return Output(output.ast, output.tokenStream, output.messages)
  }
}
