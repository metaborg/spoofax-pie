package mb.spoofax.pie.legacy

import mb.log.api.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStamper
import mb.pie.lang.runtime.util.Tuple3
import mb.pie.vfs.path.PPath
import org.apache.commons.vfs2.FileObject
import org.metaborg.core.action.ITransformGoal
import org.metaborg.core.analysis.AnalysisException
import org.metaborg.core.context.IContext
import org.metaborg.core.project.IProject
import org.metaborg.core.syntax.ParseException
import org.metaborg.core.transform.TransformException
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet
import org.metaborg.spoofax.core.syntax.SyntaxFacet
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit
import org.spoofax.interpreter.terms.IStrategoTerm

data class ProcessOutput(
  val ast: IStrategoTerm?,
  val outputFile: PPath?,
  val inputFile: PPath
) : Tuple3<IStrategoTerm?, PPath?, PPath>

fun ExecContext.processOne(
  file: PPath,
  project: IProject? = null,
  analyze: Boolean = false,
  transformGoal: ITransformGoal? = null,
  reqFileStamper: FileStamper? = null,
  genFileStamper: FileStamper? = null,
  log: Logger? = null
): ProcessOutput? {
  return processAll(arrayListOf(file), project, analyze, transformGoal, reqFileStamper, genFileStamper, log).firstOrNull()
}

fun ExecContext.processAll(
  files: Iterable<PPath>,
  project: IProject? = null,
  analyze: Boolean = false,
  transformGoal: ITransformGoal? = null,
  reqFileStamper: FileStamper? = null,
  genFileStamper: FileStamper? = null,
  log: Logger? = null
): ArrayList<ProcessOutput> {
  val spoofax = Spx.spoofax()
  // Read input files.
  val inputUnits = files.mapNotNull { file ->
    require(file, reqFileStamper)
    if(!file.exists()) {
      log?.warn("File $file does not exist, skipping")
      null
    } else if(file.isDir) {
      log?.warn("Path $file is a directory, skipping")
      null
    } else {
      val resource = file.fileObject
      val langImpl = spoofax.languageIdentifierService.identify(resource, project)
      if(langImpl == null) {
        log?.warn("Cannot identify language of $file, skipping")
        null
      } else {
        val bytes = file.readAllBytes()
        val text = String(bytes)
        spoofax.unitService.inputUnit(file.fileObject, text, langImpl, null)
      }
    }
  }
  val langImpls = inputUnits.map { it.langImpl() }.toHashSet()

  // Parsing.
  // Require parse table.
  langImpls.forEach { langImpl ->
    val parseFacet = langImpl.facet<SyntaxFacet>(SyntaxFacet::class.java)
    if(parseFacet != null) {
      val parseTableFile = parseFacet.parseTable
      if(parseTableFile != null) {
        require(parseTableFile.pPath, reqFileStamper)
      }
    }
  }
  // Create input units.
  // Do actual parsing.
  val parseUnits = try {
    spoofax.syntaxService.parseAll(inputUnits)
  } catch(e: ParseException) {
    log?.error("Parsing failed unexpectedly", e)
    return arrayListOf()
  }

  if(!analyze && transformGoal == null) {
    return parseUnits.map {
      ProcessOutput(it.ast(), null, it.input().source()!!.pPath)
    }.toCollection(arrayListOf())
  }

  // Require Stratego runtime files for analysis and transformation.
  langImpls.forEach { langImpl ->
    val strategoRuntimeFacet = langImpl.facet(StrategoRuntimeFacet::class.java)
    if(strategoRuntimeFacet != null) {
      strategoRuntimeFacet.ctreeFiles.forEach<FileObject> {
        require(it.pPath, reqFileStamper)
      }
      strategoRuntimeFacet.jarFiles.forEach<FileObject> {
        require(it.pPath, reqFileStamper)
      }
    }
  }
  // Load project for analysis and transformation.
  project ?: throw RuntimeException("Project must be set if analysis is true or transformGoal is not null")

  // Load context for analysis and transformation.
  val parseUnitsPerContext = run {
    val perContext = hashMapOf<IContext, MutableCollection<ISpoofaxParseUnit>>()
    for(parseUnit in parseUnits) {
      val context = spoofax.contextService.get(parseUnit.source(), project, parseUnit.input().langImpl())
      if(!perContext.containsKey(context)) {
        perContext[context] = mutableListOf(parseUnit)
      } else {
        perContext[context]!!.add(parseUnit)
      }
    }
    perContext
  }

  // Perform analysis
  val analyzeUnitsPerContext = if(analyze) {
    val perContext = hashMapOf<IContext, MutableCollection<ISpoofaxAnalyzeUnit>>()
    parseUnitsPerContext.forEach { (context, parseUnits) ->
      context.write().use { _ ->
        try {
          val analyzeResults = spoofax.analysisService.analyzeAll(parseUnits, context)
          val analyzeUnits = analyzeResults.results()
          if(!perContext.containsKey(context)) {
            perContext[context] = analyzeUnits
          } else {
            perContext[context]!!.addAll(analyzeUnits)
          }
        } catch(e: AnalysisException) {
          log?.error("Analysis failed unexpectedly", e)
          return arrayListOf()
        }
      }
    }
    perContext
  } else {
    null
  }

  // Perform transformation
  if(transformGoal != null) {
    try {
      val results = if(analyzeUnitsPerContext != null) {
        analyzeUnitsPerContext.flatMap { (context, analyzeUnits) ->
          context.read().use { _ ->
            spoofax.transformService.transformAllAnalyzed(analyzeUnits, context, transformGoal)
          }
        }
      } else {
        parseUnitsPerContext.flatMap { (context, parseUnits) ->
          context.read().use { _ ->
            spoofax.transformService.transformAllParsed(parseUnits, context, transformGoal)
          }
        }
      }
      return results.flatMap { result ->
        val ast = result.ast()
        val outputs = result.outputs()
        if(ast != null && outputs.count() == 0) {
          listOf(ProcessOutput(ast, null, result.source()!!.pPath))
        } else {
          outputs.map { output ->
            val outputResource = output?.output()
            val writtenFile: PPath?
            writtenFile = if(outputResource != null) {
              generate(outputResource.pPath, genFileStamper)
              outputResource.pPath
            } else {
              null
            }
            ProcessOutput(ast, writtenFile, result.source()!!.pPath)
          }
        }
      }.toCollection(arrayListOf())
    } catch(e: TransformException) {
      log?.error("Transformation failed", e)
      return arrayListOf()
    }
  } else {
    return analyzeUnitsPerContext!!.flatMap { it.value }.map {
      ProcessOutput(it.ast(), null, it.input().source()!!.pPath)
    }.toCollection(ArrayList())
  }
}
