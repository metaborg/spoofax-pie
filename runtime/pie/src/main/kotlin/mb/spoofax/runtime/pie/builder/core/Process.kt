package mb.spoofax.runtime.pie.builder.core

import mb.log.Logger
import mb.pie.runtime.builtin.util.Tuple3
import mb.vfs.path.PPath
import org.apache.commons.vfs2.FileObject
import org.metaborg.core.action.ITransformGoal
import org.metaborg.core.analysis.AnalysisException
import org.metaborg.core.language.ILanguageImpl
import org.metaborg.core.project.IProject
import org.metaborg.core.syntax.ParseException
import org.metaborg.core.transform.TransformException
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet
import org.metaborg.spoofax.core.syntax.SyntaxFacet
import org.spoofax.interpreter.terms.IStrategoTerm


data class ProcessOutputs(
  val outputs: ArrayList<ProcessOutput>,
  val reqFiles: ArrayList<PPath>,
  val genFiles: ArrayList<PPath>
)

data class ProcessOutput(
  val ast: IStrategoTerm?,
  val outputFile: PPath?,
  val inputFile: PPath
) : Tuple3<IStrategoTerm?, PPath?, PPath>

fun process(
  files: Iterable<PPath>,
  langImpl: ILanguageImpl,
  project: IProject?,
  analyze: Boolean,
  transformGoal: ITransformGoal?,
  log: Logger?
): ProcessOutputs {
  val spoofax = Spx.spoofax()
  val reqFiles = arrayListOf<PPath>()
  val genFiles = arrayListOf<PPath>()

  // Read input files.
  val inputUnits = files.mapNotNull { file ->
    reqFiles.add(file)
    if(!file.exists()) {
      null
    } else {
      val bytes = file.readAllBytes()
      val text = String(bytes)
      spoofax.unitService.inputUnit(file.fileObject, text, langImpl, null)
    }
  }

  // Parsing.
  // Require parse table.
  val parseFacet = langImpl.facet<SyntaxFacet>(SyntaxFacet::class.java)
  if(parseFacet != null) {
    val parseTableFile = parseFacet.parseTable
    if(parseTableFile != null) {
      reqFiles.add(parseTableFile.pPath)
    }
  }
  // Create input units.
  // Do actual parsing.
  val parseUnits = try {
    spoofax.syntaxService.parseAll(inputUnits)
  } catch(e: ParseException) {
    log?.error("Parsing failed unexpectedly", e)
    return ProcessOutputs(arrayListOf(), reqFiles, genFiles)
  }

  if(!analyze && transformGoal == null) {
    val outputs = parseUnits.map {
      ProcessOutput(it.ast(), null, it.input().source()!!.pPath)
    }.toCollection(arrayListOf())
    return ProcessOutputs(outputs, reqFiles, genFiles)
  }

  // Require Stratego runtime files for analysis and transformation.
  val strategoRuntimeFacet = langImpl.facet(StrategoRuntimeFacet::class.java)
  if(strategoRuntimeFacet != null) {
    strategoRuntimeFacet.ctreeFiles.forEach<FileObject> {
      reqFiles.add(it.pPath)
    }
    strategoRuntimeFacet.jarFiles.forEach<FileObject> {
      reqFiles.add(it.pPath)
    }
  }
  // Load project for analysis and transformation.
  project ?: throw RuntimeException("Project must be set if analysis is true or transformGoal is not null")
  // Load context for analysis and transformation.
  val spoofaxContext = spoofax.contextService.get(project.location(), project, langImpl)

  val analyzeUnits = if(analyze) {
    // Perform analysis
    spoofaxContext.write().use { _ ->
      try {
        val analyzeResults = spoofax.analysisService.analyzeAll(parseUnits, spoofaxContext)
        analyzeResults.results()
      } catch(e: AnalysisException) {
        log?.error("Analysis failed unexpectedly", e)
        return ProcessOutputs(arrayListOf(), reqFiles, genFiles)
      }
    }
  } else {
    null
  }

  // Perform transformation
  if(transformGoal != null) {
    spoofaxContext.read().use { _ ->
      try {
        val results = if(analyzeUnits != null) {
          spoofax.transformService.transformAllAnalyzed(analyzeUnits, spoofaxContext, transformGoal)
        } else {
          spoofax.transformService.transformAllParsed(parseUnits, spoofaxContext, transformGoal)
        }
        val outputs = results.flatMap { result ->
          val ast = result.ast()
          result.outputs().map { output ->
            val outputResource = output?.output()
            val writtenFile: PPath?
            writtenFile = if(outputResource != null) {
              genFiles.add(outputResource.pPath)
              outputResource.pPath
            } else {
              null
            }
            ProcessOutput(ast, writtenFile, result.source()!!.pPath)
          }
        }.toCollection(arrayListOf())
        return ProcessOutputs(outputs, reqFiles, genFiles)
      } catch(e: TransformException) {
        log?.error("Transformation failed", e)
        return ProcessOutputs(arrayListOf(), reqFiles, genFiles)
      }
    }
  } else {
    val outputs = analyzeUnits!!.map {
      ProcessOutput(it.ast(), null, it.input().source()!!.pPath)
    }.toCollection(arrayListOf())
    return ProcessOutputs(outputs, reqFiles, genFiles)
  }
}