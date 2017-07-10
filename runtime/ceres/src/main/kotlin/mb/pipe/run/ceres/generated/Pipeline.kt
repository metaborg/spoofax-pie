package mb.pipe.run.ceres.generated

import mb.ceres.*
import mb.pipe.run.core.*
import mb.pipe.run.ceres.util.*
import com.google.inject.*
import java.io.Serializable

class langSpecConfigForPath : Builder<langSpecConfigForPath.Input, mb.pipe.run.spoofax.cfg.LangSpecConfig?> {
  data class Input(val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig, val path: mb.pipe.run.core.path.PPath) : Tuple2<mb.pipe.run.spoofax.cfg.WorkspaceConfig, mb.pipe.run.core.path.PPath> {
    constructor(tuple: Tuple2<mb.pipe.run.spoofax.cfg.WorkspaceConfig, mb.pipe.run.core.path.PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "langSpecConfigForPath"
  override fun BuildContext.build(input: langSpecConfigForPath.Input): mb.pipe.run.spoofax.cfg.LangSpecConfig? {
    var extension = input.path.extension()
    if (extension == null)
      return null
    return input.workspace.langSpecConfigForExt(extension!!)
  }
}

class spxCoreConfigForPath : Builder<spxCoreConfigForPath.Input, mb.pipe.run.spoofax.cfg.SpxCoreConfig?> {
  data class Input(val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig, val path: mb.pipe.run.core.path.PPath) : Tuple2<mb.pipe.run.spoofax.cfg.WorkspaceConfig, mb.pipe.run.core.path.PPath> {
    constructor(tuple: Tuple2<mb.pipe.run.spoofax.cfg.WorkspaceConfig, mb.pipe.run.core.path.PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "spxCoreConfigForPath"
  override fun BuildContext.build(input: spxCoreConfigForPath.Input): mb.pipe.run.spoofax.cfg.SpxCoreConfig? {
    var extension = input.path.extension()
    if (extension == null)
      return null
    return input.workspace.spxCoreConfigForExt(extension!!)
  }
}

class createWorkspaceConfig : Builder<mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.WorkspaceConfig?> {
  override val id = "createWorkspaceConfig"
  override fun BuildContext.build(input: mb.pipe.run.core.path.PPath): mb.pipe.run.spoofax.cfg.WorkspaceConfig? {
    var cfgLang = mb.pipe.run.spoofax.cfg.SpxCoreConfig.create(mb.pipe.run.ceres.path.resolve("/Users/gohla/metaborg/repo/pipeline/cfg/langspec"), false, list("cfg"))
    var workspaceFile = input.resolve("root/workspace.cfg")
    if (!requireOutput(mb.pipe.run.ceres.path.Exists::class.java, workspaceFile))
      return null
    var text = requireOutput(mb.pipe.run.ceres.path.Read::class.java, workspaceFile)
    var workspaceConfig = requireOutput(mb.pipe.run.ceres.spoofax.GenerateWorkspaceConfig::class.java, mb.pipe.run.ceres.spoofax.GenerateWorkspaceConfig.Input(text, input, cfgLang))
    return workspaceConfig
  }
}

class processWorkspace : Builder<mb.pipe.run.core.path.PPath, ArrayList<Tuple4<mb.pipe.run.core.path.PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>>> {
  override val id = "processWorkspace"
  override fun BuildContext.build(input: mb.pipe.run.core.path.PPath): ArrayList<Tuple4<mb.pipe.run.core.path.PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>> {
    var workspaceConfig = requireOutput(createWorkspaceConfig::class.java, input)
    var results: ArrayList<Tuple4<mb.pipe.run.core.path.PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>> = list()
    if (workspaceConfig == null)
      return results
    var workspace = workspaceConfig!!
    for (project in requireOutput(mb.pipe.run.ceres.path.ListContents::class.java, mb.pipe.run.ceres.path.ListContents.Input(input, mb.pipe.run.core.path.PPaths.directoryPathMatcher() as mb.pipe.run.core.path.PathMatcher?))) {
      for (file in requireOutput(mb.pipe.run.ceres.path.WalkContents::class.java, mb.pipe.run.ceres.path.WalkContents.Input(project, mb.pipe.run.core.path.PPaths.extensionsPathWalker(workspace.langSpecExtensions()) as mb.pipe.run.core.path.PathWalker?))) {
        var config = requireOutput(langSpecConfigForPath::class.java, langSpecConfigForPath.Input(workspace, file))
        if (config != null) {
          var (tokens, messages, styling) = requireOutput(processFileWithLangSpecConfig::class.java, processFileWithLangSpecConfig.Input(file, project, config!!, workspace))
          results = results.append(tuple(file, tokens, messages, styling))
        } else {
          results = results.append(requireOutput(emptyResult::class.java, file))
        }
      }
      for (file in requireOutput(mb.pipe.run.ceres.path.WalkContents::class.java, mb.pipe.run.ceres.path.WalkContents.Input(project, mb.pipe.run.core.path.PPaths.extensionsPathWalker(workspace.spxCoreExtensions()) as mb.pipe.run.core.path.PathWalker?))) {
        var config = requireOutput(spxCoreConfigForPath::class.java, spxCoreConfigForPath.Input(workspace, file))
        if (config != null) {
          var (tokens, messages, styling) = requireOutput(processFileWithSpxCore::class.java, processFileWithSpxCore.Input(file, config!!))
          results = results.append(tuple(file, tokens, messages, styling))
        } else {
          results = results.append(requireOutput(emptyResult::class.java, file))
        }
      }
    }
    return results
  }
}

class processString : Builder<processString.Input, processString.Output?> {
  data class Input(val text: String, val associatedFile: mb.pipe.run.core.path.PPath, val associatedProject: mb.pipe.run.core.path.PPath, val root: mb.pipe.run.core.path.PPath) : Tuple4<String, mb.pipe.run.core.path.PPath, mb.pipe.run.core.path.PPath, mb.pipe.run.core.path.PPath> {
    constructor(tuple: Tuple4<String, mb.pipe.run.core.path.PPath, mb.pipe.run.core.path.PPath, mb.pipe.run.core.path.PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _2: ArrayList<mb.pipe.run.core.model.message.Msg>, val _3: mb.pipe.run.core.model.style.Styling?) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>?) = if (tuple == null) null else Output(tuple)

  override val id = "processString"
  override fun BuildContext.build(input: processString.Input): processString.Output? {
    var workspaceConfig = requireOutput(createWorkspaceConfig::class.java, input.root)
    if (workspaceConfig == null)
      return null
    var workspace = workspaceConfig!!
    var extension = input.associatedFile.extension()
    if (extension == null)
      return null
    var langSpecConfig = workspace.langSpecConfigForExt(extension!!)
    if (langSpecConfig != null) {
      return output(requireOutput(processStringWithLangSpecConfig::class.java, processStringWithLangSpecConfig.Input(input.text, input.associatedProject, langSpecConfig!!, workspace)) as Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>?)
    }
    var spxCoreConfig = workspace.spxCoreConfigForExt(extension!!)
    if (spxCoreConfig != null) {
      return output(requireOutput(processStringWithSpxCore::class.java, processStringWithSpxCore.Input(input.text, input.associatedFile, spxCoreConfig!!)) as Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>?)
    }
    return null
  }
}

class emptyResult : Builder<mb.pipe.run.core.path.PPath, emptyResult.Output> {
  data class Output(val _1: mb.pipe.run.core.path.PPath, val _2: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _3: ArrayList<mb.pipe.run.core.model.message.Msg>, val _4: mb.pipe.run.core.model.style.Styling?) : Tuple4<mb.pipe.run.core.path.PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple4<mb.pipe.run.core.path.PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<mb.pipe.run.core.path.PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "emptyResult"
  override fun BuildContext.build(input: mb.pipe.run.core.path.PPath): emptyResult.Output {
    var emptyTokens: ArrayList<mb.pipe.run.core.model.parse.Token>? = null
    var emptyMessages: ArrayList<mb.pipe.run.core.model.message.Msg> = list()
    var emptyStyling: mb.pipe.run.core.model.style.Styling? = null
    return output(tuple(input, emptyTokens, emptyMessages, emptyStyling))
  }
}

class processFileWithLangSpecConfig : Builder<processFileWithLangSpecConfig.Input, processFileWithLangSpecConfig.Output> {
  data class Input(val file: mb.pipe.run.core.path.PPath, val project: mb.pipe.run.core.path.PPath, val langSpec: mb.pipe.run.spoofax.cfg.LangSpecConfig, val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig) : Tuple4<mb.pipe.run.core.path.PPath, mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<mb.pipe.run.core.path.PPath, mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _2: ArrayList<mb.pipe.run.core.model.message.Msg>, val _3: mb.pipe.run.core.model.style.Styling?) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processFileWithLangSpecConfig"
  override fun BuildContext.build(input: processFileWithLangSpecConfig.Input): processFileWithLangSpecConfig.Output {
    var text = requireOutput(mb.pipe.run.ceres.path.Read::class.java, input.file)
    return output(requireOutput(processStringWithLangSpecConfig::class.java, processStringWithLangSpecConfig.Input(text, input.project, input.langSpec, input.workspace)))
  }
}

class processStringWithLangSpecConfig : Builder<processStringWithLangSpecConfig.Input, processStringWithLangSpecConfig.Output> {
  data class Input(val text: String, val associatedProject: mb.pipe.run.core.path.PPath, val langSpec: mb.pipe.run.spoofax.cfg.LangSpecConfig, val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig) : Tuple4<String, mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<String, mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _2: ArrayList<mb.pipe.run.core.model.message.Msg>, val _3: mb.pipe.run.core.model.style.Styling?) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processStringWithLangSpecConfig"
  override fun BuildContext.build(input: processStringWithLangSpecConfig.Input): processStringWithLangSpecConfig.Output {
    var (ast, tokenStream, messages) = requireOutput(parse::class.java, parse.Input(input.text, input.associatedProject, input.langSpec, input.workspace))
    var styling: mb.pipe.run.core.model.style.Styling?
    if (tokenStream != null) {
      styling = requireOutput(style::class.java, style.Input(tokenStream!!, input.langSpec, input.workspace))
    } else {
      styling = null
    }
    return output(tuple(tokenStream, messages, styling))
  }
}

class parse : Builder<parse.Input, parse.Output> {
  data class Input(val text: String, val associatedProject: mb.pipe.run.core.path.PPath, val langSpec: mb.pipe.run.spoofax.cfg.LangSpecConfig, val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig) : Tuple4<String, mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<String, mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _3: ArrayList<mb.pipe.run.core.model.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>>) = Output(tuple)

  override val id = "parse"
  override fun BuildContext.build(input: parse.Input): parse.Output {
    var sdfLang = input.workspace.spxCoreConfigForExt("sdf3")
    if (sdfLang == null)
      return output(requireOutput(emptyParse::class.java, None.instance))
    var mainFile = input.langSpec.getSyntaxMainFile()
    var startSymbol = input.langSpec.getSyntaxStartSymbol()
    if (mainFile == null || startSymbol == null)
      return output(requireOutput(emptyParse::class.java, None.instance))
    var parseTable = requireOutput(mb.pipe.run.ceres.spoofax.GenerateTable::class.java, mb.pipe.run.ceres.spoofax.GenerateTable.Input(sdfLang!!, input.associatedProject, mainFile!!, list()))
    if (parseTable == null)
      return output(requireOutput(emptyParse::class.java, None.instance))
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
    var emptyAst: org.spoofax.interpreter.terms.IStrategoTerm? = null
    var emptyTokens: ArrayList<mb.pipe.run.core.model.parse.Token>? = null
    var emptyMessages: ArrayList<mb.pipe.run.core.model.message.Msg> = list()
    return output(tuple(emptyAst, emptyTokens, emptyMessages))
  }
}

class style : Builder<style.Input, mb.pipe.run.core.model.style.Styling?> {
  data class Input(val tokenStream: ArrayList<mb.pipe.run.core.model.parse.Token>, val langSpec: mb.pipe.run.spoofax.cfg.LangSpecConfig, val workspace: mb.pipe.run.spoofax.cfg.WorkspaceConfig) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>, mb.pipe.run.spoofax.cfg.LangSpecConfig, mb.pipe.run.spoofax.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = "style"
  override fun BuildContext.build(input: style.Input): mb.pipe.run.core.model.style.Styling? {
    var esvLang = input.workspace.spxCoreConfigForExt("esv")
    if (esvLang == null)
      return null
    var mainFile = input.langSpec.getSyntaxBasedStylingFile()
    if (mainFile == null)
      return null
    var syntaxStyler = requireOutput(mb.pipe.run.ceres.spoofax.GenerateStylerRules::class.java, mb.pipe.run.ceres.spoofax.GenerateStylerRules.Input(esvLang!!, mainFile!!, list()))
    if (syntaxStyler == null)
      return null
    return requireOutput(mb.pipe.run.ceres.spoofax.Style::class.java, mb.pipe.run.ceres.spoofax.Style.Input(input.tokenStream, syntaxStyler!!)) as mb.pipe.run.core.model.style.Styling?
  }
}

class processFileWithSpxCore : Builder<processFileWithSpxCore.Input, processFileWithSpxCore.Output> {
  data class Input(val file: mb.pipe.run.core.path.PPath, val config: mb.pipe.run.spoofax.cfg.SpxCoreConfig) : Tuple2<mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.SpxCoreConfig> {
    constructor(tuple: Tuple2<mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.SpxCoreConfig>) : this(tuple.component1(), tuple.component2())
  }

  data class Output(val _1: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _2: ArrayList<mb.pipe.run.core.model.message.Msg>, val _3: mb.pipe.run.core.model.style.Styling?) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processFileWithSpxCore"
  override fun BuildContext.build(input: processFileWithSpxCore.Input): processFileWithSpxCore.Output {
    var text = requireOutput(mb.pipe.run.ceres.path.Read::class.java, input.file)
    return output(requireOutput(processStringWithSpxCore::class.java, processStringWithSpxCore.Input(text, input.file, input.config)))
  }
}

class processStringWithSpxCore : Builder<processStringWithSpxCore.Input, processStringWithSpxCore.Output> {
  data class Input(val text: String, val associatedFile: mb.pipe.run.core.path.PPath, val config: mb.pipe.run.spoofax.cfg.SpxCoreConfig) : Tuple3<String, mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.SpxCoreConfig> {
    constructor(tuple: Tuple3<String, mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.SpxCoreConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _2: ArrayList<mb.pipe.run.core.model.message.Msg>, val _3: mb.pipe.run.core.model.style.Styling?) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processStringWithSpxCore"
  override fun BuildContext.build(input: processStringWithSpxCore.Input): processStringWithSpxCore.Output {
    var (ast, tokens, messages) = requireOutput(mb.pipe.run.ceres.spoofax.core.CoreParse::class.java, mb.pipe.run.ceres.spoofax.core.CoreParse.Input(input.config, input.text))
    var styling: mb.pipe.run.core.model.style.Styling?
    if (ast != null && tokens != null) {
      styling = requireOutput(mb.pipe.run.ceres.spoofax.core.CoreStyle::class.java, mb.pipe.run.ceres.spoofax.core.CoreStyle.Input(input.config, tokens!!, ast!!)) as mb.pipe.run.core.model.style.Styling?
    } else {
      styling = null
    }
    return output(tuple(tokens, messages, styling))
  }
}


class CeresBuilderModule : Module {
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
    binder.bindBuilder<processWorkspace>(builders, "processWorkspace")
    binder.bindBuilder<createWorkspaceConfig>(builders, "createWorkspaceConfig")
    binder.bindBuilder<spxCoreConfigForPath>(builders, "spxCoreConfigForPath")
    binder.bindBuilder<langSpecConfigForPath>(builders, "langSpecConfigForPath")
  }
}
