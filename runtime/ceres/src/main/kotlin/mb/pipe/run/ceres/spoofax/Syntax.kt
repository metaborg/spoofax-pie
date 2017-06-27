package mb.pipe.run.ceres.spoofax

import com.google.common.collect.Lists
import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.BuildException
import mb.ceres.Builder
import mb.pipe.run.ceres.path.cPath
import mb.pipe.run.ceres.path.read
import mb.pipe.run.ceres.spoofax.core.CoreParse
import mb.pipe.run.ceres.spoofax.core.CoreTrans
import mb.pipe.run.ceres.spoofax.core.Spx
import mb.pipe.run.ceres.spoofax.core.cPath
import mb.pipe.run.ceres.spoofax.core.fileObject
import mb.pipe.run.ceres.spoofax.core.loadLang
import mb.pipe.run.ceres.spoofax.core.loadProj
import mb.pipe.run.ceres.spoofax.core.pPath
import mb.pipe.run.ceres.spoofax.core.parse
import mb.pipe.run.ceres.spoofax.core.trans
import mb.pipe.run.ceres.util.Tuple3
import mb.pipe.run.core.log.Logger
import mb.pipe.run.core.model.message.Msg
import mb.pipe.run.core.model.parse.Token
import mb.pipe.run.core.path.PPath
import mb.pipe.run.core.path.PathSrv
import mb.pipe.run.spoofax.sdf.Table
import org.metaborg.core.action.EndNamedGoal
import org.metaborg.sdf2table.parsetable.ParseTableGenerator
import org.metaborg.spoofax.core.build.SpoofaxCommonPaths
import org.spoofax.interpreter.terms.IStrategoTerm
import org.spoofax.terms.TermFactory
import java.io.Serializable

class GenerateTable
@Inject constructor(private val log: Logger, private val pathSrv: PathSrv)
  : Builder<GenerateTable.Input, Table> {
  companion object {
    val id = "spoofaxGenerateTable"
  }

  data class Input(val langLoc: PPath, val specDir: PPath, val mainFile: PPath, val includedFiles: ArrayList<PPath>) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Table {
    // Read input files
    val mainFileText = read(input.mainFile)
    val texts = mutableMapOf<PPath, String>()
    for (file in input.includedFiles) {
      val text = read(input.mainFile)
      texts.put(file, text)
    }
    texts.put(input.mainFile, mainFileText)

    // Load SDF3, required for parsing, analysis, and transformation.
    val langImpl = loadLang(input.langLoc)
    val langId = langImpl.id()

    // Parse input files
    val asts = mutableMapOf<PPath, IStrategoTerm>()
    for ((file, text) in texts) {
      val (ast, _, _) = parse(CoreParse.Input(langId, file, text))
      if (ast == null) {
        log.error("Unable to parse SDF file $file, skipping file")
        continue
      }
      asts.put(file, ast)
    }

    // Load project, required for analysis and transformation.
    loadProj(input.specDir)

    // Transform
    val transformGoal = EndNamedGoal("to Normal Form (abstract)")
    val normalized = mutableMapOf<PPath, CoreTrans.Output>()
    for ((file, ast) in asts) {
      val trans = trans(CoreTrans.Input(langId, input.specDir, file, ast, transformGoal))
      if (trans.ast == null || trans.writtenFile == null) {
        log.error("Unable to transform SDF file $file, skipping file")
        continue
      }
      normalized.put(file, trans)
    }

    val mainResource = normalized[input.mainFile]?.writtenFile ?: throw BuildException("Main file " + input.mainFile + " could not be normalized")

    // Create table
    // Main input file
    val mainFile = pathSrv.localPath(mainResource) ?: throw BuildException("Normalized main file $mainResource is not on the local file system")
    // Output file
    val spoofaxPaths = SpoofaxCommonPaths(input.specDir.fileObject)
    val vfsOutputFile = spoofaxPaths.targetMetaborgDir().resolveFile("sdf-new.tbl")
    val outputFile = Spx.spoofax().resourceService.localPath(vfsOutputFile) ?: throw BuildException("Parse table output file $vfsOutputFile is not on the local file system")
    // Paths
    val paths = Lists.newArrayList(spoofaxPaths.syntaxSrcGenDir().name.uri)
    // Create table and make dependencies
    val generator = ParseTableGenerator(mainFile, outputFile, null, null, paths, false)
    generator.createTable(false, false)
    for (required in generator.requiredFiles()) {
      require(pathSrv.resolveLocal(required).cPath)
    }
    generate(vfsOutputFile.cPath)
    return Table(vfsOutputFile.pPath)
  }
}

class Parse : Builder<Parse.Input, Parse.Output> {
  companion object {
    val id = "spoofaxParse"
  }

  data class Input(val text: String, val startSymbol: String, val table: Table) : Serializable
  data class Output(val ast: IStrategoTerm?, val tokenStream: ArrayList<Token>?, val messages: ArrayList<Msg>) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Msg>>

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Output {
    val parser = input.table.createParser(TermFactory())
    val output = parser.parse(input.text, input.startSymbol)

    return Output(output.ast, output.tokenStream, output.messages)
  }
}
