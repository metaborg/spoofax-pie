package mb.spoofax.runtime.pie.builder

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.builtin.path.read
import mb.pie.runtime.builtin.util.Tuple3
import mb.pie.runtime.core.*
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
@Inject constructor(private val log: Logger, private val pathSrv: PathSrv)
  : Builder<GenerateTable.Input, Table> {
  companion object {
    val id = "spoofaxGenerateTable"
  }

  data class Input(val sdfLangConfig: SpxCoreConfig, val specDir: PPath, val files: Iterable<PPath>, val mainFile: PPath) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Table {
    // Read input files
    val mainFileText = read(input.mainFile)
    val texts = mutableMapOf<PPath, String>()
    for(file in input.files) {
      val text = read(file)
      texts.put(file, text)
    }
    texts.put(input.mainFile, mainFileText)

    // Parse input files
    val parsed = mutableMapOf<PPath, IStrategoTerm>()
    for((file, text) in texts) {
      val (ast, _, _) = parse(input.sdfLangConfig, text)
      if(ast == null) {
        log.error("Unable to parse SDF file $file, skipping file")
        continue
      }
      parsed.put(file, ast)
    }

    // Load project, required for analysis and transformation.
    val proj = loadProj(input.specDir)

    // Transform
    val transformGoal = EndNamedGoal("to Normal Form (abstract)")
    val normalized = mutableMapOf<PPath, CoreTrans.Output>()
    for((file, parsedAst) in parsed) {
      val results = trans(input.sdfLangConfig, proj.path, file, parsedAst, transformGoal)
      var success = false;
      for(trans in results) {
        if(trans.ast != null && trans.writtenFile != null) {
          success = true
          normalized.put(file, trans)
        }
      }
      if(!success) {
        log.error("Unable to transform SDF file $file, skipping file")
      }
    }

    val mainResource = normalized[input.mainFile]?.writtenFile ?: throw BuildException("Main file " + input.mainFile + " could not be normalized")

    // Create table
    // Main input file
    val mainFile = pathSrv.localPath(mainResource) ?: throw BuildException("Normalized main file $mainResource is not on the local file system")
    // Output file
    val spoofaxPaths = SpoofaxCommonPaths(proj.location())
    val vfsOutputFile = spoofaxPaths.targetMetaborgDir().resolveFile("sdf-new.tbl")
    val outputFile = Spx.spoofax().resourceService.localPath(vfsOutputFile) ?: throw BuildException("Parse table output file $vfsOutputFile is not on the local file system")
    // Paths
    val paths = ArrayList(listOf(spoofaxPaths.syntaxSrcGenDir().name.uri))
    // Create table and make dependencies
    require(mainResource);
    val generator = ParseTableGenerator(mainFile, outputFile, null, null, paths)
    generator.outputTable(false)
    generate(vfsOutputFile.pPath)
    return Table(vfsOutputFile.pPath)
  }
}

class GenerateSignatures
@Inject constructor(private val log: Logger, private val pathSrv: PathSrv)
  : Builder<GenerateSignatures.Input, Signatures> {
  companion object {
    val id = "spoofaxGenerateSignatures"
  }

  data class Input(val sdfLangConfig: SpxCoreConfig, val specDir: PPath, val files: Iterable<PPath>) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Signatures {
    // Read input files
    val texts = mutableMapOf<PPath, String>()
    for(file in input.files) {
      val text = read(file)
      texts.put(file, text)
    }

    // Parse input files
    val parsed = mutableMapOf<PPath, IStrategoTerm>()
    for((file, text) in texts) {
      val (ast, _, _) = parse(input.sdfLangConfig, text)
      if(ast == null) {
        log.error("Unable to parse SDF file $file, skipping file")
        continue
      }
      parsed.put(file, ast)
    }

    // Load project, required for analysis and transformation.
    val proj = loadProj(input.specDir)

    // Analyze
    val analyzed = mutableMapOf<PPath, IStrategoTerm>()
    for((file, parsedAst) in parsed) {
      val result = analyze(CoreAnalyze.Input(input.sdfLangConfig, proj.path, file, parsedAst))
      if(result.ast == null) {
        log.error("Unable to analyze SDF file $file, skipping file")
        continue
      }
      analyzed.put(file, result.ast)
    }

    // Transform
    val transformGoal = EndNamedGoal("Generate Signature (concrete)")
    val signatureFiles = ArrayList<PPath>()
    for((file, analyzedAst) in analyzed) {
      val results = trans(input.sdfLangConfig, proj.path, file, analyzedAst, transformGoal)
      var success = false
      for((transformedAst, writtenFile) in results) {
        if(transformedAst != null && writtenFile != null) {
          success = true
          signatureFiles.add(file)
        }
      }
      if(!success) {
        log.error("Unable to generate signatures for SDF file $file, skipping file")
      }
    }

    val includeDir = input.specDir.resolve("src-gen")
    return Signatures(signatureFiles, includeDir)
  }
}

class Parse : Builder<Parse.Input, Parse.Output> {
  companion object {
    val id = "spoofaxParse"
  }

  data class Input(val text: String, val startSymbol: String, val table: Table, val file: PPath) : Serializable
  data class Output(val ast: IStrategoTerm?, val tokenStream: ArrayList<Token>?, val messages: ArrayList<Msg>) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Msg>>

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Output {
    val termFactory = ImploderOriginTermFactory(TermFactory())
    val parser = input.table.createParser(termFactory)
    val output = parser.parse(input.text, input.startSymbol, input.file)

    return Output(output.ast, output.tokenStream, output.messages)
  }
}
