package mb.spoofax.runtime.pie.builder.core

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.builtin.util.Tuple2
import mb.pie.runtime.core.*
import mb.spoofax.runtime.impl.cfg.SpxCoreConfig
import mb.spoofax.runtime.impl.legacy.MessageConverter
import mb.spoofax.runtime.model.message.Msg
import mb.vfs.path.PPath
import org.metaborg.core.analysis.AnalysisException
import org.metaborg.core.messages.IMessage
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet
import org.metaborg.spoofax.core.unit.ParseContrib
import org.metaborg.util.iterators.Iterables2
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

class CoreAnalyze @Inject constructor(log: Logger, private val messageConverter: MessageConverter) : Builder<CoreAnalyze.Input, CoreAnalyze.Output> {
  companion object {
    val id = "coreAnalyze"
  }

  data class Input(val config: SpxCoreConfig, val project: PPath, val file: PPath, val ast: IStrategoTerm) : Serializable
  data class Output(val ast: IStrategoTerm?, val messages: ArrayList<Msg>, val file: PPath) : Tuple2<IStrategoTerm?, ArrayList<Msg>>

  val log: Logger = log.forContext(CoreAnalyze::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Output {
    val spoofax = Spx.spoofax()
    val langImpl = buildOrLoad(input.config)

    // Require Stratego runtime files
    val facet = langImpl.facet<StrategoRuntimeFacet>(StrategoRuntimeFacet::class.java)
    if(facet != null) {
      facet.ctreeFiles.forEach { require(it.pPath, PathStampers.hash) }
      facet.jarFiles.forEach { require(it.pPath, PathStampers.hash) }
    }

    // Perform analysis
    val resource = input.file.fileObject
    val project = spoofax.projectService.get(resource) ?: throw BuildException("Cannot analyze $resource, it does not belong to a project")
    val inputUnit = spoofax.unitService.inputUnit(resource, "hack", langImpl, null)
    val parseUnit = spoofax.unitService.parseUnit(inputUnit, ParseContrib(true, true, input.ast, Iterables2.empty<IMessage>(), -1))
    val spoofaxContext = spoofax.contextService.get(resource, project, langImpl)
    spoofaxContext.write().use {
      try {
        val analyzeResult = spoofax.analysisService.analyze(parseUnit, spoofaxContext)
        val result = analyzeResult.result();
        val ast = result.ast();
        val messages = messageConverter.toMsgs(result.messages());
        return Output(ast, messages, input.file)
      } catch(e: AnalysisException) {
        log.error("Analysis failed unexpectedly", e)
        return Output(null, ArrayList(), input.file)
      }
    }
  }
}

fun BuildContext.analyze(input: CoreAnalyze.Input) = requireOutput(CoreAnalyze::class.java, input)
fun BuildContext.analyze(config: SpxCoreConfig, project: PPath, file: PPath, ast: IStrategoTerm) = analyze(CoreAnalyze.Input(config, project, file, ast))


class CoreAnalyzeAll @Inject constructor(log: Logger, private val messageConverter: MessageConverter) : Builder<CoreAnalyzeAll.Input, ArrayList<CoreAnalyzeAll.Output>> {
  companion object {
    val id = "coreAnalyzeAll"
  }

  data class AstFilePair(val ast: IStrategoTerm, val file: PPath) : Tuple2<IStrategoTerm, PPath> {
    constructor(tuple: Tuple2<IStrategoTerm, PPath>) : this(tuple.component1(), tuple.component2())
  }

  data class Input(val config: SpxCoreConfig, val project: PPath, val pairs: Iterable<AstFilePair>) : Serializable
  data class Output(val ast: IStrategoTerm?, val messages: ArrayList<Msg>, val file: PPath) : Tuple2<IStrategoTerm?, ArrayList<Msg>>

  val log: Logger = log.forContext(CoreAnalyzeAll::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: Input): ArrayList<Output> {
    val spoofax = Spx.spoofax()
    val langImpl = buildOrLoad(input.config)

    // Require Stratego runtime files
    val facet = langImpl.facet<StrategoRuntimeFacet>(StrategoRuntimeFacet::class.java)
    if(facet != null) {
      facet.ctreeFiles.forEach { require(it.pPath, PathStampers.hash) }
      facet.jarFiles.forEach { require(it.pPath, PathStampers.hash) }
    }

    // Perform analysis
    val project = spoofax.projectService.get(input.project.fileObject) ?: throw BuildException("Cannot analyze $input.project, it is not a project location")
    val spoofaxContext = spoofax.contextService.get(project.location(), project, langImpl)
    val parseUnits = input.pairs.map { (ast, file) ->
      val resource = file.fileObject
      val project = spoofax.projectService.get(resource) ?: throw BuildException("Cannot analyze $resource, it does not belong to a project")
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
        return input.pairs.map { (text, file) -> Output(null, ArrayList(), file) }.toCollection(ArrayList())
      }
    }
  }
}

fun BuildContext.analyzeAll(input: CoreAnalyzeAll.Input) = requireOutput(CoreAnalyzeAll::class.java, input)
fun BuildContext.analyzeAll(config: SpxCoreConfig, project: PPath, pairs: Iterable<CoreAnalyzeAll.AstFilePair>) = analyzeAll(CoreAnalyzeAll.Input(config, project, pairs))