package mb.pipe.run.ceres.spoofax.core

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.BuildException
import mb.ceres.Builder
import mb.ceres.CPath
import mb.ceres.OutTransient
import mb.pipe.run.ceres.path.cPath
import mb.pipe.run.ceres.util.Tuple2
import mb.pipe.run.ceres.util.Tuple3
import mb.pipe.run.core.log.Logger
import mb.pipe.run.core.model.message.Msg
import mb.pipe.run.core.model.parse.Token
import mb.pipe.run.core.model.style.Styling
import mb.pipe.run.core.path.PPath
import mb.pipe.run.core.path.PPathImpl
import mb.pipe.run.spoofax.sdf.TokenExtractor
import mb.pipe.run.spoofax.util.MessageConverter
import mb.pipe.run.spoofax.util.StaticSpoofax
import mb.pipe.run.spoofax.util.StyleConverter
import org.apache.commons.vfs2.FileObject
import org.metaborg.core.MetaborgException
import org.metaborg.core.action.CompileGoal
import org.metaborg.core.action.EndNamedGoal
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
  companion object {
    val id = "coreLoadLang"
  }

  override val id = Companion.id
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


class CoreLoadProj : Builder<PPath, OutTransient<CoreLoadProj.Project>> {
  companion object {
    val id = "coreLoadProj"
  }

  class Project(private val spxCoreProject: IProject) {
    fun directory(): PPath {
      return spxCoreProject.location().pPath;
    }
  }

  override val id = Companion.id
  override fun BuildContext.build(input: PPath): OutTransient<Project> {
    val spoofax = Spx.spoofax()
    val projLoc = input.fileObject
    var project = spoofax.projectService.get(projLoc)
    if (project == null) {
      project = spoofax.injector.getInstance(ISimpleProjectService::class.java).create(projLoc)
    }
    return OutTransient(Project(project!!))
  }
}

fun BuildContext.loadProj(input: PPath) = requireOutput(CoreLoadProj::class.java, input).v


class CoreParse @Inject constructor(log: Logger, val messageConverter: MessageConverter) : Builder<CoreParse.Input, CoreParse.Output> {
  companion object {
    val id = "coreParse"
  }

  data class Input(val langId: LanguageIdentifier, val file: PPath, val text: String) : Serializable
  data class Output(val ast: IStrategoTerm?, val tokens: ArrayList<Token>?, val messages: ArrayList<Msg>) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Msg>>

  val log: Logger = log.forContext(CoreTrans::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Output {
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
      val ast = parseUnit.ast();
      val tokens = if (ast != null) TokenExtractor.extract(ast) else null
      val messages = messageConverter.toMsgs(parseUnit.messages());
      return Output(ast, tokens, messages);
    } catch(e: ParseException) {
      log.error("Parsing failed unexpectedly", e)
      return Output(null, null, ArrayList(0))
    }
  }
}

fun BuildContext.parse(input: CoreParse.Input) = requireOutput(CoreParse::class.java, input)


class CoreAnalyze @Inject constructor(log: Logger, val messageConverter: MessageConverter) : Builder<CoreAnalyze.Input, CoreAnalyze.Output> {
  companion object {
    val id = "coreAnalyze"
  }

  data class Input(val langId: LanguageIdentifier, val project: PPath, val file: PPath, val ast: IStrategoTerm) : Serializable
  data class Output(val ast: IStrategoTerm?, val messages: ArrayList<Msg>) : Tuple2<IStrategoTerm?, ArrayList<Msg>>

  val log: Logger = log.forContext(CoreTrans::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Output {
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
        val result = analyzeResult.result();
        val ast = result.ast();
        val messages = messageConverter.toMsgs(result.messages());
        return Output(ast, messages)
      } catch(e: AnalysisException) {
        log.error("Analysis failed unexpectedly", e)
        return Output(null, ArrayList(0))
      }
    }
  }
}

fun BuildContext.analyze(input: CoreAnalyze.Input) = requireOutput(CoreAnalyze::class.java, input)

fun createCompileGoal() = CompileGoal()
fun createNamedGoal(name: String) = EndNamedGoal(name)

class CoreTrans @Inject constructor(log: Logger) : Builder<CoreTrans.Input, CoreTrans.Output> {
  companion object {
    val id = "coreTrans"
  }

  data class Input(val langId: LanguageIdentifier, val project: PPath, val file: PPath, val ast: IStrategoTerm, val goal: ITransformGoal) : Serializable {
    fun mayOverlap(other: Input): Boolean {
      return langId == other.langId && project == other.project && file == other.file && goal == other.goal
    }
  }

  data class Output(val ast: IStrategoTerm?, val writtenFile: PPath?) : Tuple2<IStrategoTerm?, PPath?>

  val log: Logger = log.forContext(CoreTrans::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Output {
    val spoofax = Spx.spoofax()
    val langImpl = spoofax.languageService.getImpl(input.langId)
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

class CoreStyle @Inject constructor(log: Logger) : Builder<CoreStyle.Input, Styling> {
  companion object {
    val id = "coreStyle"
  }

  data class Input(val langId: LanguageIdentifier, val tokenStream: ArrayList<Token>, val ast: IStrategoTerm) : Serializable


  val log: Logger = log.forContext(CoreTrans::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Styling {
    val spoofax = Spx.spoofax()
    val langImpl = spoofax.languageService.getImpl(input.langId)
    val inputUnit = spoofax.unitService.inputUnit("hack", langImpl, null)
    val parseUnit = spoofax.unitService.parseUnit(inputUnit, ParseContrib(true, true, input.ast, Iterables2.empty<IMessage>(), -1))
    val categorization = spoofax.categorizerService.categorize(langImpl, parseUnit)
    val styling = StyleConverter.toStyling(spoofax.stylerService.styleParsed(langImpl, categorization))
    return styling
  }
}