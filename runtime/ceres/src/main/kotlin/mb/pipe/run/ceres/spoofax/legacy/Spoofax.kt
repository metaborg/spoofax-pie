package mb.pipe.run.ceres.spoofax.legacy

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.Builder
import mb.ceres.CPath
import mb.ceres.OutTransient
import mb.pipe.run.ceres.path.cPath
import mb.pipe.run.core.log.Logger
import mb.pipe.run.core.path.PPath
import mb.pipe.run.core.path.PPathImpl
import mb.pipe.run.spoofax.util.StaticSpoofax
import org.apache.commons.vfs2.FileObject
import org.metaborg.core.MetaborgException
import org.metaborg.core.action.ITransformGoal
import org.metaborg.core.analysis.AnalysisException
import org.metaborg.core.build.CommonPaths
import org.metaborg.core.language.IComponentCreationConfigRequest
import org.metaborg.core.language.ILanguageImpl
import org.metaborg.core.language.LanguageIdentifier
import org.metaborg.core.messages.IMessage
import org.metaborg.core.project.IProject
import org.metaborg.core.project.ISimpleProjectService
import org.metaborg.core.syntax.ParseException
import org.metaborg.core.transform.TransformException
import org.metaborg.spoofax.core.syntax.SyntaxFacet
import org.metaborg.spoofax.core.unit.AnalyzeContrib
import org.metaborg.spoofax.core.unit.ParseContrib
import org.metaborg.util.iterators.Iterables2
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable
import java.net.URI

object Spx {
  fun spoofax() = StaticSpoofax.spoofax()!!
}


val FileObject.pPath: PPath get() = PPathImpl(URI(this.name.uri))
val PPath.fileObject: FileObject get() = Spx.spoofax().resourceService.resolve(this.javaPath.toUri())

val FileObject.cPath: CPath get() = CPath(URI(this.name.uri))
val CPath.fileObject: FileObject get() = Spx.spoofax().resourceService.resolve(this.javaPath.toUri())


class CoreLoadLang : Builder<PPath, OutTransient<ILanguageImpl>> {
  override val id = "coreLoadLang"
  override fun BuildContext.build(input: PPath): OutTransient<ILanguageImpl> {
    val spoofax = Spx.spoofax()
    val resource = input.fileObject
    val request: IComponentCreationConfigRequest
    if (resource.isFile) {
      request = spoofax.languageComponentFactory.requestFromArchive(resource);
      require(input.cPath)
    } else {
      request = spoofax.languageComponentFactory.requestFromDirectory(resource);
      val paths = CommonPaths(resource);
      require(paths.mbComponentConfigFile().cPath)
      require(paths.targetMetaborgDir().resolveFile("sdf.tbl").cPath)
      require(paths.targetMetaborgDir().resolveFile("editor.esv.af").cPath)
    }
    val config = spoofax.languageComponentFactory.createConfig(request);
    val component = spoofax.languageService.add(config);
    val impl = component.contributesTo().first()
    return OutTransient(impl)
  }
}

fun BuildContext.loadLang(input: PPath) = requireOutput(CoreLoadLang::class.java, input).v


class CoreLoadProj : Builder<PPath, OutTransient<IProject>> {
  override val id = "coreLoadProj"
  override fun BuildContext.build(input: PPath): OutTransient<IProject> {
    val spoofax = Spx.spoofax()
    val projLoc = input.fileObject
    var project = spoofax.projectService.get(projLoc)
    if (project == null) {
      project = spoofax.injector.getInstance(ISimpleProjectService::class.java).create(projLoc)
    }
    return OutTransient(project!!)
  }
}

fun BuildContext.loadProj(input: PPath) = requireOutput(CoreLoadProj::class.java, input).v


class CoreParse @Inject constructor(log: Logger) : Builder<CoreParse.Input, IStrategoTerm?> {
  data class Input(val langId: LanguageIdentifier, val file: PPath, val text: String) : Serializable

  val log: Logger = log.forContext(CoreTrans::class.java)

  override val id = "coreParse"
  override fun BuildContext.build(input: Input): IStrategoTerm? {
    val spoofax = Spx.spoofax()
    val langImpl = spoofax.languageService.getImpl(input.langId)!!
    val facet = langImpl.facet<SyntaxFacet>(SyntaxFacet::class.java)
    if (facet != null) {
      val parseTableFile = facet.parseTable
      if (parseTableFile != null) {
        require(parseTableFile.cPath)
      }
    }

    val resource = input.file.fileObject
    val inputUnit = spoofax.unitService.inputUnit(resource, input.text, langImpl, null)
    try {
      val parseUnit = spoofax.syntaxService.parse(inputUnit)
      return parseUnit.ast()
    } catch(e: ParseException) {
      log.error("Parsing failed", e)
      return null
    }
  }
}

fun BuildContext.parse(input: CoreParse.Input) = requireOutput(CoreParse::class.java, input)


class CoreAnalyze @Inject constructor(log: Logger) : Builder<CoreAnalyze.Input, IStrategoTerm?> {
  data class Input(val langId: LanguageIdentifier, val project: PPath, val file: PPath, val ast: IStrategoTerm?) : Serializable

  val log: Logger = log.forContext(CoreTrans::class.java)

  override val id = "coreAnalyze"
  override fun BuildContext.build(input: Input): IStrategoTerm? {
    if (input.ast == null) {
      return null
    }

    val spoofax = Spx.spoofax()
    val langImpl = spoofax.languageService.getImpl(input.langId)
    val resource = input.file.fileObject
    val project = spoofax.projectService.get(resource) ?: throw MetaborgException("Cannot analyze $resource, it does not belong to a project")

    val inputUnit = spoofax.unitService.inputUnit(resource, "hack", langImpl, null)
    val parseUnit = spoofax.unitService.parseUnit(inputUnit, ParseContrib(true, true, input.ast, Iterables2.empty<IMessage>(), -1))

    val spoofaxContext = spoofax.contextService.get(project.location(), project, langImpl)
    spoofaxContext.write().use {
      try {
        val analyzeResult = spoofax.analysisService.analyze(parseUnit, spoofaxContext)
        return analyzeResult.result().ast()
      } catch(e: AnalysisException) {
        log.error("Analysis failed", e)
        return null
      }
    }
  }
}

fun BuildContext.analyze(input: CoreAnalyze.Input) = requireOutput(CoreAnalyze::class.java, input)


class CoreTrans @Inject constructor(log: Logger) : Builder<CoreTrans.Input, CoreTrans.Output> {
  data class Input(val langId: LanguageIdentifier, val project: PPath, val file: PPath, val ast: IStrategoTerm?, val goal: ITransformGoal) : Serializable {
    fun mayOverlap(other: Input): Boolean {
      return langId == other.langId && project == other.project && file == other.file && goal == other.goal
    }
  }

  data class Output(val ast: IStrategoTerm?, val writtenFile: PPath?) : Serializable

  val log: Logger = log.forContext(CoreTrans::class.java)

  override val id = "coreTrans"
  override fun BuildContext.build(input: Input): Output {
    if (input.ast == null) {
      return Output(null, null)
    }

    val spoofax = Spx.spoofax()
    val langImpl = spoofax.languageService.getImpl(input.langId)
    val resource = input.file.fileObject
    val project = spoofax.projectService.get(resource) ?: throw MetaborgException("Cannot transform $resource, it does not belong to a project")

    val inputUnit = spoofax.unitService.inputUnit(resource, "hack", langImpl, null)
    val parseUnit = spoofax.unitService.parseUnit(inputUnit, ParseContrib(true, true, input.ast, Iterables2.empty<IMessage>(), -1))
    val spoofaxContext = spoofax.contextService.get(project.location(), project, langImpl)
    val analyzeUnit = spoofax.unitService.analyzeUnit(parseUnit,
      AnalyzeContrib(true, true, true, input.ast, Iterables2.empty<IMessage>(), -1), spoofaxContext)

    spoofaxContext.read().use {
      try {
        val result = spoofax.transformService.transform(analyzeUnit, spoofaxContext, input.goal)
        val unit = result.iterator().next()
        val ast = unit.ast()
        val output = unit.outputs().first()
        val outputResource = output.output()
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