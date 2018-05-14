package mb.spoofax.runtime.pie.legacy

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.builtin.util.Tuple2
import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.FileStampers
import mb.spoofax.runtime.impl.cfg.SpxCoreConfig
import mb.spoofax.runtime.impl.legacy.MessageConverter
import mb.spoofax.runtime.model.message.Msg
import mb.vfs.path.PPath
import org.apache.commons.vfs2.FileObject
import org.metaborg.core.analysis.AnalysisException
import org.metaborg.core.messages.IMessage
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet
import org.metaborg.spoofax.core.unit.ParseContrib
import org.metaborg.util.iterators.Iterables2
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

class CoreAnalyze @Inject constructor(log: Logger, private val messageConverter: MessageConverter) : TaskDef<CoreAnalyze.Input, CoreAnalyze.Output> {
  companion object {
    const val id = "coreAnalyze"
  }

  data class Input(val config: SpxCoreConfig, val project: PPath, val file: PPath, val ast: IStrategoTerm) : Serializable
  data class Output(val ast: IStrategoTerm?, val messages: ArrayList<Msg>, val file: PPath) : Tuple2<IStrategoTerm?, ArrayList<Msg>>

  val log: Logger = log.forContext(CoreAnalyze::class.java)

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Output {
    val spoofax = Spx.spoofax()
    val langImpl = buildOrLoad(input.config)

    // Require Stratego runtime files
    val facet = langImpl.facet(StrategoRuntimeFacet::class.java)
    if(facet != null) {
      facet.ctreeFiles.forEach<FileObject> { require(it.pPath, FileStampers.hash) }
      facet.jarFiles.forEach<FileObject> { require(it.pPath, FileStampers.hash) }
    }

    // Perform analysis
    val resource = input.file.fileObject
    val project = spoofax.projectService.get(resource) ?: throw ExecException("Cannot analyze $resource, it does not belong to a project")
    val inputUnit = spoofax.unitService.inputUnit(resource, "hack", langImpl, null)
    val parseUnit = spoofax.unitService.parseUnit(inputUnit, ParseContrib(true, true, input.ast, Iterables2.empty<IMessage>(), -1))
    val spoofaxContext = spoofax.contextService.get(resource, project, langImpl)
    spoofaxContext.write().use {
      try {
        val analyzeResult = spoofax.analysisService.analyze(parseUnit, spoofaxContext)
        val result = analyzeResult.result()
        val ast = result.ast()
        val messages = messageConverter.toMsgs(result.messages())
        return Output(ast, messages, input.file)
      } catch(e: AnalysisException) {
        log.error("Analysis failed unexpectedly", e)
        return Output(null, ArrayList(), input.file)
      }
    }
  }
}

fun ExecContext.analyze(input: CoreAnalyze.Input) = requireOutput(CoreAnalyze::class.java, CoreAnalyze.id, input)
fun ExecContext.analyze(config: SpxCoreConfig, project: PPath, file: PPath, ast: IStrategoTerm) = analyze(CoreAnalyze.Input(config, project, file, ast))


class CoreAnalyzeAll @Inject constructor(log: Logger, private val messageConverter: MessageConverter) : TaskDef<CoreAnalyzeAll.Input, ArrayList<CoreAnalyzeAll.Output>> {
  companion object {
    const val id = "coreAnalyzeAll"
  }

  data class AstFilePair(val ast: IStrategoTerm, val file: PPath) : Tuple2<IStrategoTerm, PPath> {
    constructor(tuple: Tuple2<IStrategoTerm, PPath>) : this(tuple.component1(), tuple.component2())
  }

  data class Input(val config: SpxCoreConfig, val project: PPath, val pairs: Iterable<AstFilePair>) : Serializable
  data class Output(val ast: IStrategoTerm?, val messages: ArrayList<Msg>, val file: PPath) : Tuple2<IStrategoTerm?, ArrayList<Msg>>

  val log: Logger = log.forContext(CoreAnalyzeAll::class.java)

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ArrayList<Output> {
    val spoofax = Spx.spoofax()
    val langImpl = buildOrLoad(input.config)

    // Require Stratego runtime files
    val facet = langImpl.facet(StrategoRuntimeFacet::class.java)
    if(facet != null) {
      facet.ctreeFiles.forEach<FileObject> { require(it.pPath, FileStampers.hash) }
      facet.jarFiles.forEach<FileObject> { require(it.pPath, FileStampers.hash) }
    }

    // Perform analysis
    val project = spoofax.projectService.get(input.project.fileObject)
      ?: throw ExecException("Cannot analyze $input.project, it is not a project location")
    val spoofaxContext = spoofax.contextService.get(project.location(), project, langImpl)
    val parseUnits = input.pairs.map { (ast, file) ->
      val resource = file.fileObject
      spoofax.projectService.get(resource) ?: throw ExecException("Cannot analyze $resource, it does not belong to a project")
      val inputUnit = spoofax.unitService.inputUnit(resource, "hack", langImpl, null)
      spoofax.unitService.parseUnit(inputUnit, ParseContrib(true, true, ast, Iterables2.empty<IMessage>(), -1))
    }
    spoofaxContext.write().use {
      try {
        val analyzeResults = spoofax.analysisService.analyzeAll(parseUnits, spoofaxContext)
        return analyzeResults.results().map {
          val ast = it.ast()
          val messages = messageConverter.toMsgs(it.messages())
          Output(ast, messages, it.source()?.pPath!!)
        }.toCollection(ArrayList())
      } catch(e: AnalysisException) {
        log.error("Analysis failed unexpectedly", e)
        return input.pairs.map { (_, file) -> Output(null, ArrayList(), file) }.toCollection(ArrayList())
      }
    }
  }
}

fun ExecContext.analyzeAll(input: CoreAnalyzeAll.Input) = requireOutput(CoreAnalyzeAll::class.java, CoreAnalyzeAll.id, input)
fun ExecContext.analyzeAll(config: SpxCoreConfig, project: PPath, pairs: Iterable<CoreAnalyzeAll.AstFilePair>) = analyzeAll(CoreAnalyzeAll.Input(config, project, pairs))