package mb.pipe.run.ceres.spoofax.core

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.BuildException
import mb.ceres.Builder
import mb.ceres.PathStampers
import mb.pipe.run.ceres.util.Tuple2
import mb.pipe.run.core.log.Logger
import mb.pipe.run.core.path.PPath
import mb.pipe.run.spoofax.cfg.SpxCoreConfig
import org.metaborg.core.action.CompileGoal
import org.metaborg.core.action.EndNamedGoal
import org.metaborg.core.action.ITransformGoal
import org.metaborg.core.messages.IMessage
import org.metaborg.core.transform.TransformException
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet
import org.metaborg.spoofax.core.unit.AnalyzeContrib
import org.metaborg.spoofax.core.unit.ParseContrib
import org.metaborg.util.iterators.Iterables2
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

fun createCompileGoal() = CompileGoal()
fun createNamedGoal(name: String) = EndNamedGoal(name)
class CoreTrans @Inject constructor(log: Logger) : Builder<CoreTrans.Input, CoreTrans.Output> {
  companion object {
    val id = "coreTrans"
  }

  data class Input(val config: SpxCoreConfig, val project: PPath, val file: PPath, val ast: IStrategoTerm, val goal: ITransformGoal) : Serializable {
    fun mayOverlap(other: Input): Boolean {
      return config == other.config && project == other.project && file == other.file && goal == other.goal
    }
  }

  data class Output(val ast: IStrategoTerm?, val writtenFile: PPath?) : Tuple2<IStrategoTerm?, PPath?>

  val log: Logger = log.forContext(CoreTrans::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Output {
    val spoofax = Spx.spoofax()
    val langImpl = buildOrLoad(input.config)

    // Require Stratego runtime files
    val facet = langImpl.facet<StrategoRuntimeFacet>(StrategoRuntimeFacet::class.java)
    if (facet != null) {
      facet.ctreeFiles.forEach { require(it.cPath, PathStampers.hash) }
      facet.jarFiles.forEach { require(it.cPath, PathStampers.hash) }
    }

    // Perform transformation
    val resource = input.file.fileObject
    val project = spoofax.projectService.get(resource) ?: throw BuildException("Cannot transform $resource, it does not belong to a project")
    val inputUnit = spoofax.unitService.inputUnit(resource, "hack", langImpl, null)
    val parseUnit = spoofax.unitService.parseUnit(inputUnit, ParseContrib(true, true, input.ast, Iterables2.empty<IMessage>(), -1))
    val spoofaxContext = spoofax.contextService.get(project.location(), project, langImpl)
    val analyzeUnit = spoofax.unitService.analyzeUnit(parseUnit,
            AnalyzeContrib(true, true, true, input.ast, Iterables2.empty<IMessage>(), -1), spoofaxContext)
    spoofaxContext.read().use {
      try {
        val result = spoofax.transformService.transform(analyzeUnit, spoofaxContext, input.goal)
        val unit = result.first()
        val ast = unit.ast()
        val output = unit.outputs().firstOrNull()
        val outputResource = output?.output()
        val writtenFile: PPath?
        if (outputResource != null) {
          generate(outputResource.cPath)
          writtenFile = outputResource.pPath
        } else {
          writtenFile = null
        }
        return Output(ast, writtenFile)
      } catch(e: TransformException) {
        log.error("Transformation failed", e)
        return Output(null, null)
      }
    }
  }

  override fun mayOverlap(input1: Input, input2: Input): Boolean {
    return input1.mayOverlap(input2)
  }
}

fun BuildContext.trans(input: CoreTrans.Input) = requireOutput(CoreTrans::class.java, input)
fun BuildContext.trans(config: SpxCoreConfig, project: PPath, file: PPath, ast: IStrategoTerm, goal: ITransformGoal) = trans(CoreTrans.Input(config, project, file, ast, goal))
