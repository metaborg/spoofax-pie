package mb.pipe.run.ceres.generated

import com.google.inject.Binder
import com.google.inject.Module
import mb.ceres.BuildContext
import mb.ceres.Builder
import mb.ceres.None
import mb.ceres.bindBuilder
import mb.ceres.builderMapBinder
import mb.pipe.run.ceres.path.Exists
import mb.pipe.run.ceres.path.Read
import mb.pipe.run.ceres.path.WalkContents
import mb.pipe.run.ceres.util.Tuple2
import mb.pipe.run.ceres.util.Tuple3
import mb.pipe.run.ceres.util.Tuple4
import mb.pipe.run.ceres.util.list
import mb.pipe.run.ceres.util.plus
import mb.pipe.run.ceres.util.tuple
import mb.vfs.path.PPath
import mb.vfs.path.PPathImpl
import mb.vfs.path.PPaths
import java.nio.file.Paths

class langSpecConfigForPath : Builder<langSpecConfigForPath.Input, mb.pipe.run.spoofax.cfg.LangSpecConfig?> {
  data class Input(val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig, val path: PPath) : Tuple2<mb.pipe.run.spoofax.cfg.WorkspaceConfig, PPath> {
    constructor(tuple: Tuple2<mb.pipe.run.spoofax.cfg.WorkspaceConfig, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "langSpecConfigForPath"
  override fun BuildContext.build(input: langSpecConfigForPath.Input): mb.pipe.run.spoofax.cfg.LangSpecConfig? {
    val extension = input.path.extension()
    if (extension == null) return null
    return input.workspace.langSpecConfigForExt(extension!!)
  }
}

class spxCoreConfigForpath : Builder<spxCoreConfigForpath.Input, mb.pipe.run.spoofax.cfg.SpxCoreConfig?> {
  data class Input(val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig, val path: PPath) : Tuple2<mb.pipe.run.spoofax.cfg.WorkspaceConfig, PPath> {
    constructor(tuple: Tuple2<mb.pipe.run.spoofax.cfg.WorkspaceConfig, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "spxCoreConfigForpath"
  override fun BuildContext.build(input: spxCoreConfigForpath.Input): mb.pipe.run.spoofax.cfg.SpxCoreConfig? {
    val extension = input.path.extension()
    if (extension == null) return null
    return input.workspace.spxCoreConfigForExt(extension!!)
  }
}

class createWorkspaceConfig : Builder<PPath, mb.pipe.run.spoofax.cfg.WorkspaceConfig?> {
  override val id = "createWorkspaceConfig"
  override fun BuildContext.build(input: PPath): mb.pipe.run.spoofax.cfg.WorkspaceConfig? {
    val cfgLang = mb.pipe.run.spoofax.cfg.SpxCoreConfig.create(PPathImpl(Paths.get("/Users/gohla/metaborg/repo/pipeline/cfg/langspec")) as PPath, false, list("cfg"))
    val workspaceFile = input.resolve(PPathImpl(Paths.get("./root/workspace.cfg")) as PPath)
    if (!requireOutput(Exists::class.java, workspaceFile)) return null
    val text = requireOutput(Read::class.java, workspaceFile)
    val workspaceConfig = requireOutput(mb.pipe.run.ceres.spoofax.GenerateWorkspaceConfig::class.java, mb.pipe.run.ceres.spoofax.GenerateWorkspaceConfig.Input(text, input, cfgLang))
    return workspaceConfig
  }
}

class processProject : Builder<processProject.Input, ArrayList<Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>>> {
  data class Input(val root: PPath, val project: PPath) : Tuple2<PPath, PPath> {
    constructor(tuple: Tuple2<PPath, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "processProject"
  override fun BuildContext.build(input: processProject.Input): ArrayList<Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>> {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class.java, input.root)
    if (workspaceConfig == null) return list()
    val workspace = workspaceConfig!!
    val langSpecResults = requireOutput(WalkContents::class.java, WalkContents.Input(input.project, PPaths.extensionsPathWalker(workspace.langSpecExtensions()))).map { file -> requireOutput(processFileWithLangSpecConfig::class.java, processFileWithLangSpecConfig.Input(file, input.project, workspace)) }.toCollection(ArrayList<Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>>())
    val spxCoreResults = requireOutput(WalkContents::class.java, WalkContents.Input(input.project, PPaths.extensionsPathWalker(workspace.spxCoreExtensions()))).map { file -> requireOutput(processFileWithSpxCore::class.java, processFileWithSpxCore.Input(file, input.project, workspace)) }.toCollection(ArrayList<Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>>())
    return langSpecResults + spxCoreResults
  }
}

class processString : Builder<processString.Input, processString.Output?> {
  data class Input(val text: String, val associatedFile: PPath, val associatedProject: PPath, val root: PPath) : Tuple4<String, PPath, PPath, PPath> {
    constructor(tuple: Tuple4<String, PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _2: ArrayList<mb.pipe.run.core.model.message.Msg>, val _3: mb.pipe.run.core.model.style.Styling?) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>?) = if (tuple == null) null else Output(tuple)

  override val id = "processString"
  override fun BuildContext.build(input: processString.Input): processString.Output? {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class.java, input.root)
    if (workspaceConfig == null) return null
    val workspace = workspaceConfig!!
    val extension = input.associatedFile.extension()
    if (extension == null) return null
    val langSpecConfig = workspace.langSpecConfigForExt(extension!!)
    if (langSpecConfig != null) {
      return output(requireOutput(processStringWithLangSpecConfig::class.java, processStringWithLangSpecConfig.Input(input.text, input.associatedProject, langSpecConfig!!, workspace)) as Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>?)
    }
    val spxCoreConfig = workspace.spxCoreConfigForExt(extension!!)
    if (spxCoreConfig != null) {
      return output(requireOutput(processStringWithSpxCore::class.java, processStringWithSpxCore.Input(input.text, input.associatedFile, spxCoreConfig!!)) as Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>?)
    }
    return null
  }
}

class emptyResult : Builder<PPath, emptyResult.Output> {
  data class Output(val _1: PPath, val _2: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _3: ArrayList<mb.pipe.run.core.model.message.Msg>, val _4: mb.pipe.run.core.model.style.Styling?) : Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "emptyResult"
  override fun BuildContext.build(input: PPath): emptyResult.Output {
    val emptyTokens: ArrayList<mb.pipe.run.core.model.parse.Token>? = null
    val emptyMessages: ArrayList<mb.pipe.run.core.model.message.Msg> = list()
    val emptyStyling: mb.pipe.run.core.model.style.Styling? = null
    return output(tuple(input, emptyTokens, emptyMessages, emptyStyling))
  }
}

class processFileWithLangSpecConfig : Builder<processFileWithLangSpecConfig.Input, processFileWithLangSpecConfig.Output> {
  data class Input(val file: PPath, val project: PPath, val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig) : Tuple3<PPath, PPath, mb.pipe.run.spoofax.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<PPath, PPath, mb.pipe.run.spoofax.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _3: ArrayList<mb.pipe.run.core.model.message.Msg>, val _4: mb.pipe.run.core.model.style.Styling?) : Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processFileWithLangSpecConfig"
  override fun BuildContext.build(input: processFileWithLangSpecConfig.Input): processFileWithLangSpecConfig.Output {
    val langSpec = requireOutput(langSpecConfigForPath::class.java, langSpecConfigForPath.Input(input.workspace, input.file))
    if (langSpec != null) {
      val text = requireOutput(Read::class.java, input.file)
      val (tokens, messages, styling) = requireOutput(processStringWithLangSpecConfig::class.java, processStringWithLangSpecConfig.Input(text, input.project, langSpec!!, input.workspace))
      return output(tuple(input.file, tokens, messages, styling))
    } else {
      return output(requireOutput(emptyResult::class.java, input.file))
    }
  }
}

class processStringWithLangSpecConfig : Builder<processStringWithLangSpecConfig.Input, processStringWithLangSpecConfig.Output> {
  data class Input(val text: String, val associatedProject: PPath, val langSpec: mb.pipe.run.spoofax.cfg.LangSpecConfig, val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig) : Tuple4<String, PPath, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<String, PPath, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _2: ArrayList<mb.pipe.run.core.model.message.Msg>, val _3: mb.pipe.run.core.model.style.Styling?) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processStringWithLangSpecConfig"
  override fun BuildContext.build(input: processStringWithLangSpecConfig.Input): processStringWithLangSpecConfig.Output {
    val (ast, tokenStream, messages) = requireOutput(parse::class.java, parse.Input(input.text, input.associatedProject, input.langSpec, input.workspace))
    val styling: mb.pipe.run.core.model.style.Styling? = if (tokenStream == null) null else requireOutput(style::class.java, style.Input(tokenStream!!, input.langSpec, input.workspace))
    return output(tuple(tokenStream, messages, styling))
  }
}

class parse : Builder<parse.Input, parse.Output> {
  data class Input(val text: String, val associatedProject: PPath, val langSpec: mb.pipe.run.spoofax.cfg.LangSpecConfig, val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig) : Tuple4<String, PPath, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<String, PPath, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _3: ArrayList<mb.pipe.run.core.model.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>>) = Output(tuple)

  override val id = "parse"
  override fun BuildContext.build(input: parse.Input): parse.Output {
    val sdfLang = input.workspace.spxCoreConfigForExt("sdf3")
    if (sdfLang == null) return output(requireOutput(emptyParse::class.java, None.instance))
    val mainFile = input.langSpec.getSyntaxMainFile()
    val startSymbol = input.langSpec.getSyntaxStartSymbol()
    if (mainFile == null || startSymbol == null) return output(requireOutput(emptyParse::class.java, None.instance))
    val parseTable = requireOutput(mb.pipe.run.ceres.spoofax.GenerateTable::class.java, mb.pipe.run.ceres.spoofax.GenerateTable.Input(sdfLang!!, input.associatedProject, mainFile!!, list()))
    if (parseTable == null) return output(requireOutput(emptyParse::class.java, None.instance))
    return output(requireOutput(mb.pipe.run.ceres.spoofax.Parse::class.java, mb.pipe.run.ceres.spoofax.Parse.Input(input.text, startSymbol!!, parseTable!!)))
  }
}

class emptyParse : Builder<None, emptyParse.Output> {
  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _3: ArrayList<mb.pipe.run.core.model.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>>) = Output(tuple)

  override val id = "emptyParse"
  override fun BuildContext.build(input: None): emptyParse.Output {
    val emptyAst: org.spoofax.interpreter.terms.IStrategoTerm? = null
    val emptyTokens: ArrayList<mb.pipe.run.core.model.parse.Token>? = null
    val emptyMessages: ArrayList<mb.pipe.run.core.model.message.Msg> = list()
    return output(tuple(emptyAst, emptyTokens, emptyMessages))
  }
}

class style : Builder<style.Input, mb.pipe.run.core.model.style.Styling?> {
  data class Input(val tokenStream: ArrayList<mb.pipe.run.core.model.parse.Token>, val langSpec: mb.pipe.run.spoofax.cfg.LangSpecConfig, val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = "style"
  override fun BuildContext.build(input: style.Input): mb.pipe.run.core.model.style.Styling? {
    val esvLang = input.workspace.spxCoreConfigForExt("esv")
    if (esvLang == null) return null
    val mainFile = input.langSpec.getSyntaxBasedStylingFile()
    if (mainFile == null) return null
    val syntaxStyler = requireOutput(mb.pipe.run.ceres.spoofax.GenerateStylerRules::class.java, mb.pipe.run.ceres.spoofax.GenerateStylerRules.Input(esvLang!!, mainFile!!, list()))
    if (syntaxStyler == null) return null
    return requireOutput(mb.pipe.run.ceres.spoofax.Style::class.java, mb.pipe.run.ceres.spoofax.Style.Input(input.tokenStream, syntaxStyler!!)) as mb.pipe.run.core.model.style.Styling?
  }
}

class processFileWithSpxCore : Builder<processFileWithSpxCore.Input, processFileWithSpxCore.Output> {
  data class Input(val file: PPath, val project: PPath, val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig) : Tuple3<PPath, PPath, mb.pipe.run.spoofax.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<PPath, PPath, mb.pipe.run.spoofax.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _3: ArrayList<mb.pipe.run.core.model.message.Msg>, val _4: mb.pipe.run.core.model.style.Styling?) : Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processFileWithSpxCore"
  override fun BuildContext.build(input: processFileWithSpxCore.Input): processFileWithSpxCore.Output {
    val config = requireOutput(spxCoreConfigForpath::class.java, spxCoreConfigForpath.Input(input.workspace, input.file))
    if (config != null) {
      val text = requireOutput(Read::class.java, input.file)
      val (tokens, messages, styling) = requireOutput(processStringWithSpxCore::class.java, processStringWithSpxCore.Input(text, input.file, config!!))
      return output(tuple(input.file, tokens, messages, styling))
    } else {
      return output(requireOutput(emptyResult::class.java, input.file))
    }
  }
}

class processStringWithSpxCore : Builder<processStringWithSpxCore.Input, processStringWithSpxCore.Output> {
  data class Input(val text: String, val associatedFile: PPath, val config: mb.pipe.run.spoofax.cfg.SpxCoreConfig) : Tuple3<String, PPath, mb.pipe.run.spoofax.cfg.SpxCoreConfig> {
    constructor(tuple: Tuple3<String, PPath, mb.pipe.run.spoofax.cfg.SpxCoreConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _2: ArrayList<mb.pipe.run.core.model.message.Msg>, val _3: mb.pipe.run.core.model.style.Styling?) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processStringWithSpxCore"
  override fun BuildContext.build(input: processStringWithSpxCore.Input): processStringWithSpxCore.Output {
    val (ast, tokens, messages) = requireOutput(mb.pipe.run.ceres.spoofax.core.CoreParse::class.java, mb.pipe.run.ceres.spoofax.core.CoreParse.Input(input.config, input.text))
    val styling: mb.pipe.run.core.model.style.Styling? = if (ast == null || tokens == null) null else requireOutput(mb.pipe.run.ceres.spoofax.core.CoreStyle::class.java, mb.pipe.run.ceres.spoofax.core.CoreStyle.Input(input.config, tokens!!, ast!!)) as mb.pipe.run.core.model.style.Styling?
    return output(tuple(tokens, messages, styling))
  }
}


class CeresBuilderModule_spoofax : Module {
  override fun configure(binder: Binder) {
    val builders = binder.builderMapBinder()

    binder.bindBuilder<processStringWithSpxCore>(builders, "processStringWithSpxCore")
    binder.bindBuilder<processFileWithSpxCore>(builders, "processFileWithSpxCore")
    binder.bindBuilder<style>(builders, "style")
    binder.bindBuilder<emptyParse>(builders, "emptyParse")
    binder.bindBuilder<parse>(builders, "parse")
    binder.bindBuilder<processStringWithLangSpecConfig>(builders, "processStringWithLangSpecConfig")
    binder.bindBuilder<processFileWithLangSpecConfig>(builders, "processFileWithLangSpecConfig")
    binder.bindBuilder<emptyResult>(builders, "emptyResult")
    binder.bindBuilder<processString>(builders, "processString")
    binder.bindBuilder<processProject>(builders, "processProject")
    binder.bindBuilder<createWorkspaceConfig>(builders, "createWorkspaceConfig")
    binder.bindBuilder<spxCoreConfigForpath>(builders, "spxCoreConfigForpath")
    binder.bindBuilder<langSpecConfigForPath>(builders, "langSpecConfigForPath")
  }
}
