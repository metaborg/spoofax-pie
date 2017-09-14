package mb.spoofax.runtime.pie.generated

import java.io.Serializable
import java.nio.file.Paths
import com.google.inject.*

import mb.log.*
import mb.vfs.path.*
import mb.pie.runtime.core.*
import mb.pie.runtime.builtin.*
import mb.pie.runtime.builtin.path.*
import mb.pie.runtime.builtin.util.*

class langSpecConfigForPath : Builder<langSpecConfigForPath.Input, mb.spoofax.runtime.impl.cfg.LangSpecConfig?> {
  data class Input(val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig, val path: PPath) : Tuple2<mb.spoofax.runtime.impl.cfg.WorkspaceConfig, PPath> {
    constructor(tuple: Tuple2<mb.spoofax.runtime.impl.cfg.WorkspaceConfig, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "langSpecConfigForPath"
  override fun BuildContext.build(input: langSpecConfigForPath.Input): mb.spoofax.runtime.impl.cfg.LangSpecConfig? {
    val extension = input.path.extension()
    if (extension == null) return null
    return input.workspace.langSpecConfigForExt(extension!!)
  }
}

class spxCoreConfigForpath : Builder<spxCoreConfigForpath.Input, mb.spoofax.runtime.impl.cfg.SpxCoreConfig?> {
  data class Input(val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig, val path: PPath) : Tuple2<mb.spoofax.runtime.impl.cfg.WorkspaceConfig, PPath> {
    constructor(tuple: Tuple2<mb.spoofax.runtime.impl.cfg.WorkspaceConfig, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "spxCoreConfigForpath"
  override fun BuildContext.build(input: spxCoreConfigForpath.Input): mb.spoofax.runtime.impl.cfg.SpxCoreConfig? {
    val extension = input.path.extension()
    if (extension == null) return null
    return input.workspace.spxCoreConfigForExt(extension!!)
  }
}

class createWorkspaceConfig : Builder<PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig?> {
  override val id = "createWorkspaceConfig"
  override fun BuildContext.build(input: PPath): mb.spoofax.runtime.impl.cfg.WorkspaceConfig? {
    val cfgLang = mb.spoofax.runtime.impl.cfg.SpxCoreConfig.create(PPathImpl(java.nio.file.FileSystems.getDefault().getPath("/Users/gohla/metaborg/repo/pipeline/cfg/langspec")), false, list("cfg"))
    val workspaceFile = input.resolve(PPathImpl(java.nio.file.FileSystems.getDefault().getPath("./root/workspace.cfg")))
    if (!requireOutput(Exists::class.java, workspaceFile)) return null
    val text = requireOutput(Read::class.java, workspaceFile)
    val workspaceConfig = requireOutput(mb.spoofax.runtime.pie.builder.GenerateWorkspaceConfig::class.java, mb.spoofax.runtime.pie.builder.GenerateWorkspaceConfig.Input(text, input, cfgLang))
    return workspaceConfig
  }
}

class processWorkspace : Builder<PPath, ArrayList<ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>> {
  override val id = "processWorkspace"
  override fun BuildContext.build(input: PPath): ArrayList<ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>> {
    return requireOutput(ListContents::class.java, ListContents.Input(input, PPaths.regexPathMatcher("[^.].+"))).map { project -> requireOutput(processProject::class.java, processProject.Input(input, project)) }.toCollection(ArrayList<ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>())
  }
}

class processProject : Builder<processProject.Input, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>> {
  data class Input(val root: PPath, val project: PPath) : Tuple2<PPath, PPath> {
    constructor(tuple: Tuple2<PPath, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "processProject"
  override fun BuildContext.build(input: processProject.Input): ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>> {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class.java, input.root)
    if (workspaceConfig == null) return list()
    val workspace = workspaceConfig!!
    val langSpecResults = requireOutput(WalkContents::class.java, WalkContents.Input(input.project, PPaths.extensionsPathWalker(workspace.langSpecExtensions()))).map { file -> requireOutput(processFileWithLangSpecConfig::class.java, processFileWithLangSpecConfig.Input(file, input.project, workspace)) }.toCollection(ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>())
    val spxCoreResults = requireOutput(WalkContents::class.java, WalkContents.Input(input.project, PPaths.extensionsPathWalker(workspace.spxCoreExtensions()))).map { file -> requireOutput(processFileWithSpxCore::class.java, processFileWithSpxCore.Input(file, input.project, workspace)) }.toCollection(ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>())
    return langSpecResults + spxCoreResults
  }
}

class processString : Builder<processString.Input, processString.Output?> {
  data class Input(val text: String, val associatedFile: PPath, val associatedProject: PPath, val root: PPath) : Tuple4<String, PPath, PPath, PPath> {
    constructor(tuple: Tuple4<String, PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?) : Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>?) = if (tuple == null) null else Output(tuple)

  override val id = "processString"
  override fun BuildContext.build(input: processString.Input): processString.Output? {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class.java, input.root)
    if (workspaceConfig == null) return null
    val workspace = workspaceConfig!!
    val extension = input.associatedFile.extension()
    if (extension == null) return null
    val langSpecConfig = workspace.langSpecConfigForExt(extension!!)
    if (langSpecConfig != null) {
      return output(requireOutput(processStringWithLangSpecConfig::class.java, processStringWithLangSpecConfig.Input(input.text, input.associatedProject, langSpecConfig!!, workspace)) as Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>?)
    }
    val spxCoreConfig = workspace.spxCoreConfigForExt(extension!!)
    if (spxCoreConfig != null) {
      return output(requireOutput(processStringWithSpxCore::class.java, processStringWithSpxCore.Input(input.text, input.associatedFile, spxCoreConfig!!)) as Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>?)
    }
    return null
  }
}

class emptyResult : Builder<PPath, emptyResult.Output> {
  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?) : Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?> {
    constructor(tuple: Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) = Output(tuple)

  override val id = "emptyResult"
  override fun BuildContext.build(input: PPath): emptyResult.Output {
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list()
    val emptyStyling: mb.spoofax.runtime.model.style.Styling? = null
    return output(tuple(input, emptyTokens, emptyMessages, emptyStyling))
  }
}

class processFileWithLangSpecConfig : Builder<processFileWithLangSpecConfig.Input, processFileWithLangSpecConfig.Output> {
  data class Input(val file: PPath, val project: PPath, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple3<PPath, PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<PPath, PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?) : Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?> {
    constructor(tuple: Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) = Output(tuple)

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
  data class Input(val text: String, val associatedProject: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple4<String, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<String, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?) : Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) = Output(tuple)

  override val id = "processStringWithLangSpecConfig"
  override fun BuildContext.build(input: processStringWithLangSpecConfig.Input): processStringWithLangSpecConfig.Output {
    val (ast, tokenStream, messages) = requireOutput(parse::class.java, parse.Input(input.text, input.associatedProject, input.langSpec, input.workspace))
    val styling: mb.spoofax.runtime.model.style.Styling? = if (tokenStream == null) null else requireOutput(style::class.java, style.Input(tokenStream!!, input.langSpec, input.workspace))
    return output(tuple(tokenStream, messages, styling))
  }
}

class parse : Builder<parse.Input, parse.Output> {
  data class Input(val text: String, val associatedProject: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple4<String, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<String, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) = Output(tuple)

  override val id = "parse"
  override fun BuildContext.build(input: parse.Input): parse.Output {
    val sdfLang = input.workspace.spxCoreConfigForExt("sdf3")
    if (sdfLang == null) return output(requireOutput(emptyParse::class.java, None.instance))
    val mainFile = input.langSpec.getSyntaxMainFile()
    val startSymbol = input.langSpec.getSyntaxStartSymbol()
    if (mainFile == null || startSymbol == null) return output(requireOutput(emptyParse::class.java, None.instance))
    val parseTable = requireOutput(mb.spoofax.runtime.pie.builder.GenerateTable::class.java, mb.spoofax.runtime.pie.builder.GenerateTable.Input(sdfLang!!, input.associatedProject, mainFile!!, list()))
    if (parseTable == null) return output(requireOutput(emptyParse::class.java, None.instance))
    return output(requireOutput(mb.spoofax.runtime.pie.builder.Parse::class.java, mb.spoofax.runtime.pie.builder.Parse.Input(input.text, startSymbol!!, parseTable!!)))
  }
}

class emptyParse : Builder<None, emptyParse.Output> {
  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) = Output(tuple)

  override val id = "emptyParse"
  override fun BuildContext.build(input: None): emptyParse.Output {
    val emptyAst: org.spoofax.interpreter.terms.IStrategoTerm? = null
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list()
    return output(tuple(emptyAst, emptyTokens, emptyMessages))
  }
}

class style : Builder<style.Input, mb.spoofax.runtime.model.style.Styling?> {
  data class Input(val tokenStream: ArrayList<mb.spoofax.runtime.model.parse.Token>, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = "style"
  override fun BuildContext.build(input: style.Input): mb.spoofax.runtime.model.style.Styling? {
    val esvLang = input.workspace.spxCoreConfigForExt("esv")
    if (esvLang == null) return null
    val mainFile = input.langSpec.getSyntaxBasedStylingFile()
    if (mainFile == null) return null
    val syntaxStyler = requireOutput(mb.spoofax.runtime.pie.builder.GenerateStylerRules::class.java, mb.spoofax.runtime.pie.builder.GenerateStylerRules.Input(esvLang!!, mainFile!!, list()))
    if (syntaxStyler == null) return null
    return requireOutput(mb.spoofax.runtime.pie.builder.Style::class.java, mb.spoofax.runtime.pie.builder.Style.Input(input.tokenStream, syntaxStyler!!)) as mb.spoofax.runtime.model.style.Styling?
  }
}

class processFileWithSpxCore : Builder<processFileWithSpxCore.Input, processFileWithSpxCore.Output> {
  data class Input(val file: PPath, val project: PPath, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple3<PPath, PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<PPath, PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?) : Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?> {
    constructor(tuple: Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) = Output(tuple)

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
  data class Input(val text: String, val associatedFile: PPath, val config: mb.spoofax.runtime.impl.cfg.SpxCoreConfig) : Tuple3<String, PPath, mb.spoofax.runtime.impl.cfg.SpxCoreConfig> {
    constructor(tuple: Tuple3<String, PPath, mb.spoofax.runtime.impl.cfg.SpxCoreConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?) : Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) = Output(tuple)

  override val id = "processStringWithSpxCore"
  override fun BuildContext.build(input: processStringWithSpxCore.Input): processStringWithSpxCore.Output {
    val (ast, tokens, messages) = requireOutput(mb.spoofax.runtime.pie.builder.core.CoreParse::class.java, mb.spoofax.runtime.pie.builder.core.CoreParse.Input(input.config, input.text))
    val styling: mb.spoofax.runtime.model.style.Styling? = if (ast == null || tokens == null) null else requireOutput(mb.spoofax.runtime.pie.builder.core.CoreStyle::class.java, mb.spoofax.runtime.pie.builder.core.CoreStyle.Input(input.config, tokens!!, ast!!)) as mb.spoofax.runtime.model.style.Styling?
    return output(tuple(tokens, messages, styling))
  }
}


class PieBuilderModule_spoofax : Module {
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
    binder.bindBuilder<processWorkspace>(builders, "processWorkspace")
    binder.bindBuilder<createWorkspaceConfig>(builders, "createWorkspaceConfig")
    binder.bindBuilder<spxCoreConfigForpath>(builders, "spxCoreConfigForpath")
    binder.bindBuilder<langSpecConfigForPath>(builders, "langSpecConfigForPath")
  }
}
