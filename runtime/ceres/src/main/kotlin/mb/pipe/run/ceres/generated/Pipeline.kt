package mb.pipe.run.ceres.generated

import mb.ceres.*
import mb.pipe.run.core.*
import mb.pipe.run.ceres.util.*
import com.google.inject.*
import java.io.Serializable

class parse : Builder<parse.Input, parse.Output> {
  data class Input(val text: String, val context: mb.pipe.run.core.model.Context, val langSpecConfig: mb.pipe.run.spoofax.cfg.LangSpecConfig) : Tuple3<String, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.LangSpecConfig> {
    constructor(tuple: Tuple3<String, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.LangSpecConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _3: ArrayList<mb.pipe.run.core.model.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>>) = Output(tuple)

  override val id = "parse"
  override fun BuildContext.build(input: parse.Input): parse.Output {
    var mainFile = input.langSpecConfig.getSyntaxMainFile()
    var startSymbol = input.langSpecConfig.getSyntaxStartSymbol()
    if (mainFile == null || startSymbol == null) {
      return output(tuple(null, null, list()))
    }
    var langLoc = mb.pipe.run.ceres.path.resolve("/Users/gohla/spoofax/master/repo/spoofax-releng/sdf/org.metaborg.meta.lang.template")
    var specDir = input.context.currentDir()
    var includedFiles: ArrayList<mb.pipe.run.core.path.PPath> = list()
    var parseTable = requireOutput(mb.pipe.run.ceres.spoofax.GenerateTable::class.java, mb.pipe.run.ceres.spoofax.GenerateTable.Input(langLoc, specDir, mainFile!!, includedFiles))
    if (parseTable == null)
      throw PipeRunEx("Unable to build parse table".toString())
    return output(requireOutput(mb.pipe.run.ceres.spoofax.Parse::class.java, mb.pipe.run.ceres.spoofax.Parse.Input(input.text, startSymbol!!, parseTable!!)))
  }
}

class style : Builder<style.Input, mb.pipe.run.core.model.style.Styling?> {
  data class Input(val tokenStream: ArrayList<mb.pipe.run.core.model.parse.Token>, val context: mb.pipe.run.core.model.Context, val langSpecConfig: mb.pipe.run.spoofax.cfg.LangSpecConfig) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.LangSpecConfig> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.LangSpecConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = "style"
  override fun BuildContext.build(input: style.Input): mb.pipe.run.core.model.style.Styling? {
    var mainFile = input.langSpecConfig.getSyntaxBasedStylingFile()
    if (mainFile == null) {
      return null
    }
    var langLoc = mb.pipe.run.ceres.path.resolve("/Users/gohla/spoofax/master/repo/spoofax-releng/esv/org.metaborg.meta.lang.esv")
    var specDir = input.context.currentDir()
    var includedFiles: ArrayList<mb.pipe.run.core.path.PPath> = list()
    var syntaxStyler = requireOutput(mb.pipe.run.ceres.spoofax.GenerateStylerRules::class.java, mb.pipe.run.ceres.spoofax.GenerateStylerRules.Input(langLoc, specDir, mainFile!!, includedFiles))
    if (syntaxStyler == null)
      throw PipeRunEx("Unable to build syntax styler".toString())
    return requireOutput(mb.pipe.run.ceres.spoofax.Style::class.java, mb.pipe.run.ceres.spoofax.Style.Input(input.tokenStream, syntaxStyler!!))
  }
}

class processProject : Builder<processProject.Input, ArrayList<Tuple4<mb.pipe.run.core.path.PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>>> {
  data class Input(val context: mb.pipe.run.core.model.Context, val workbenchRoot: mb.pipe.run.core.path.PPath) : Tuple2<mb.pipe.run.core.model.Context, mb.pipe.run.core.path.PPath> {
    constructor(tuple: Tuple2<mb.pipe.run.core.model.Context, mb.pipe.run.core.path.PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "processProject"
  override fun BuildContext.build(input: processProject.Input): ArrayList<Tuple4<mb.pipe.run.core.path.PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>> {
    var workspaceConfig = requireOutput(createWorkspaceConfig::class.java, input.workbenchRoot)
    var results: ArrayList<Tuple4<mb.pipe.run.core.path.PPath, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>> = list()
    for (file in requireOutput(mb.pipe.run.ceres.path.WalkContents::class.java, mb.pipe.run.ceres.path.WalkContents.Input(input.context.currentDir(), mb.pipe.run.core.path.PPaths.extensionsPathWalker(workspaceConfig.langSpecExtensions())))) {
      var config = requireOutput(getLangSpecConfig::class.java, getLangSpecConfig.Input(file, input.workbenchRoot))
      if (config != null) {
        var (tokens, messages, styling) = requireOutput(processFileWithConfig::class.java, processFileWithConfig.Input(file, input.context, config!!))
        results = results.append(tuple(file, tokens, messages, styling))
      } else {
        results = results.append(tuple(file, null, list(), null))
      }
    }
    return results
  }
}

class processFileWithConfig : Builder<processFileWithConfig.Input, processFileWithConfig.Output> {
  data class Input(val file: mb.pipe.run.core.path.PPath, val context: mb.pipe.run.core.model.Context, val langSpecConfig: mb.pipe.run.spoofax.cfg.LangSpecConfig) : Tuple3<mb.pipe.run.core.path.PPath, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.LangSpecConfig> {
    constructor(tuple: Tuple3<mb.pipe.run.core.path.PPath, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.LangSpecConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _2: ArrayList<mb.pipe.run.core.model.message.Msg>, val _3: mb.pipe.run.core.model.style.Styling?) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processFileWithConfig"
  override fun BuildContext.build(input: processFileWithConfig.Input): processFileWithConfig.Output {
    var text = requireOutput(mb.pipe.run.ceres.path.Read::class.java, input.file)
    return output(requireOutput(processStringWithConfig::class.java, processStringWithConfig.Input(text, input.context, input.langSpecConfig)))
  }
}

class processString : Builder<processString.Input, processString.Output?> {
  data class Input(val text: String, val associatedFile: mb.pipe.run.core.path.PPath, val context: mb.pipe.run.core.model.Context, val workbenchRoot: mb.pipe.run.core.path.PPath) : Tuple4<String, mb.pipe.run.core.path.PPath, mb.pipe.run.core.model.Context, mb.pipe.run.core.path.PPath> {
    constructor(tuple: Tuple4<String, mb.pipe.run.core.path.PPath, mb.pipe.run.core.model.Context, mb.pipe.run.core.path.PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _2: ArrayList<mb.pipe.run.core.model.message.Msg>, val _3: mb.pipe.run.core.model.style.Styling?) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>?) = if (tuple == null) null else Output(tuple)

  override val id = "processString"
  override fun BuildContext.build(input: processString.Input): processString.Output? {
    var config = requireOutput(getLangSpecConfig::class.java, getLangSpecConfig.Input(input.associatedFile, input.workbenchRoot))
    if (config == null) {
      return null
    }
    return output(requireOutput(processStringWithConfig::class.java, processStringWithConfig.Input(input.text, input.context, config!!)) as Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>?)
  }
}

class processStringWithConfig : Builder<processStringWithConfig.Input, processStringWithConfig.Output> {
  data class Input(val text: String, val context: mb.pipe.run.core.model.Context, val langSpecConfig: mb.pipe.run.spoofax.cfg.LangSpecConfig) : Tuple3<String, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.LangSpecConfig> {
    constructor(tuple: Tuple3<String, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.LangSpecConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _2: ArrayList<mb.pipe.run.core.model.message.Msg>, val _3: mb.pipe.run.core.model.style.Styling?) : Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processStringWithConfig"
  override fun BuildContext.build(input: processStringWithConfig.Input): processStringWithConfig.Output {
    var (ast, tokenStream, messages) = requireOutput(parse::class.java, parse.Input(input.text, input.context, input.langSpecConfig))
    var styling: mb.pipe.run.core.model.style.Styling?
    if (tokenStream != null) {
      styling = requireOutput(style::class.java, style.Input(tokenStream!!, input.context, input.langSpecConfig))
    } else {
      styling = null
    }
    return output(tuple(tokenStream, messages, styling))
  }
}

class getLangSpecConfig : Builder<getLangSpecConfig.Input, mb.pipe.run.spoofax.cfg.LangSpecConfig?> {
  data class Input(val file: mb.pipe.run.core.path.PPath, val workbenchRoot: mb.pipe.run.core.path.PPath) : Tuple2<mb.pipe.run.core.path.PPath, mb.pipe.run.core.path.PPath> {
    constructor(tuple: Tuple2<mb.pipe.run.core.path.PPath, mb.pipe.run.core.path.PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "getLangSpecConfig"
  override fun BuildContext.build(input: getLangSpecConfig.Input): mb.pipe.run.spoofax.cfg.LangSpecConfig? {
    var workspaceConfig = requireOutput(createWorkspaceConfig::class.java, input.workbenchRoot)
    var extension = input.file.extension()
    if (extension == null) {
      throw PipeRunEx("Cannot process a file without an extension".toString())
    }
    var config = workspaceConfig.langSpecConfigForExt(extension!!)
    return config
  }
}

class getSpxCoreLangConfig : Builder<getSpxCoreLangConfig.Input, mb.pipe.run.spoofax.cfg.SpxCoreLangConfig?> {
  data class Input(val file: mb.pipe.run.core.path.PPath, val workbenchRoot: mb.pipe.run.core.path.PPath) : Tuple2<mb.pipe.run.core.path.PPath, mb.pipe.run.core.path.PPath> {
    constructor(tuple: Tuple2<mb.pipe.run.core.path.PPath, mb.pipe.run.core.path.PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "getSpxCoreLangConfig"
  override fun BuildContext.build(input: getSpxCoreLangConfig.Input): mb.pipe.run.spoofax.cfg.SpxCoreLangConfig? {
    var workspaceConfig = requireOutput(createWorkspaceConfig::class.java, input.workbenchRoot)
    var extension = input.file.extension()
    if (extension == null) {
      throw PipeRunEx("Cannot process a file without an extension".toString())
    }
    var config = workspaceConfig.spxCoreLangConfigForExt(extension!!)
    return config
  }
}

class createWorkspaceConfig : Builder<mb.pipe.run.core.path.PPath, mb.pipe.run.spoofax.cfg.WorkspaceConfig> {
  override val id = "createWorkspaceConfig"
  override fun BuildContext.build(input: mb.pipe.run.core.path.PPath): mb.pipe.run.spoofax.cfg.WorkspaceConfig {
    var langLoc = mb.pipe.run.ceres.path.resolve("/Users/gohla/metaborg/repo/pipeline/cfg/langspec")
    var configs: ArrayList<mb.pipe.run.spoofax.cfg.LangSpecConfig> = list()
    var rootConfigFile = input.resolve("langspec.cfg")
    if (requireOutput(mb.pipe.run.ceres.path.Exists::class.java, rootConfigFile)) {
      var config = requireOutput(mb.pipe.run.ceres.spoofax.GenerateLangSpecConfig::class.java, mb.pipe.run.ceres.spoofax.GenerateLangSpecConfig.Input(langLoc, rootConfigFile))
      if (config != null) {
        configs = configs.append(config!!)
      }
    }
    for (path in requireOutput(mb.pipe.run.ceres.path.ListContents::class.java, mb.pipe.run.ceres.path.ListContents.Input(input, null))) {
      var configFile = path.resolve("langspec.cfg")
      if (requireOutput(mb.pipe.run.ceres.path.Exists::class.java, configFile)) {
        var config = requireOutput(mb.pipe.run.ceres.spoofax.GenerateLangSpecConfig::class.java, mb.pipe.run.ceres.spoofax.GenerateLangSpecConfig.Input(langLoc, configFile))
        if (config != null) {
          configs = configs.append(config!!)
        }
      }
    }
    var spxCoreLangConfigs: ArrayList<mb.pipe.run.spoofax.cfg.SpxCoreLangConfig> = list(mb.pipe.run.spoofax.cfg.SpxCoreLangConfig.generate(mb.pipe.run.ceres.path.resolve("/Users/gohla/spoofax/master/repo/spoofax-releng/sdf/org.metaborg.meta.lang.template"), "sdf3"), mb.pipe.run.spoofax.cfg.SpxCoreLangConfig.generate(mb.pipe.run.ceres.path.resolve("/Users/gohla/spoofax/master/repo/spoofax-releng/esv/org.metaborg.meta.lang.esv"), "esv"))
    return mb.pipe.run.spoofax.cfg.WorkspaceConfig.generate(configs, spxCoreLangConfigs)
  }
}

class processFileWithSpxCore : Builder<processFileWithSpxCore.Input, processFileWithSpxCore.Output> {
  data class Input(val file: mb.pipe.run.core.path.PPath, val context: mb.pipe.run.core.model.Context, val config: mb.pipe.run.spoofax.cfg.SpxCoreLangConfig) : Tuple3<mb.pipe.run.core.path.PPath, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.SpxCoreLangConfig> {
    constructor(tuple: Tuple3<mb.pipe.run.core.path.PPath, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.SpxCoreLangConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: mb.pipe.run.core.path.PPath, val _2: String, val _3: org.spoofax.interpreter.terms.IStrategoTerm?, val _4: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _5: ArrayList<mb.pipe.run.core.model.message.Msg>, val _6: mb.pipe.run.core.model.style.Styling?) : Tuple6<mb.pipe.run.core.path.PPath, String, org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple6<mb.pipe.run.core.path.PPath, String, org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5(), tuple.component6())
  }

  private fun output(tuple: Tuple6<mb.pipe.run.core.path.PPath, String, org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processFileWithSpxCore"
  override fun BuildContext.build(input: processFileWithSpxCore.Input): processFileWithSpxCore.Output {
    var text = requireOutput(mb.pipe.run.ceres.path.Read::class.java, input.file)
    var (_, ast, tokenStream, messages, styling) = requireOutput(processStringWithSpxCore::class.java, processStringWithSpxCore.Input(text, input.file, input.context, input.config))
    return output(tuple(input.file, text, ast, tokenStream, messages, styling))
  }
}

class processStringWithSpxCore : Builder<processStringWithSpxCore.Input, processStringWithSpxCore.Output> {
  data class Input(val text: String, val associatedFile: mb.pipe.run.core.path.PPath, val context: mb.pipe.run.core.model.Context, val config: mb.pipe.run.spoofax.cfg.SpxCoreLangConfig) : Tuple4<String, mb.pipe.run.core.path.PPath, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.SpxCoreLangConfig> {
    constructor(tuple: Tuple4<String, mb.pipe.run.core.path.PPath, mb.pipe.run.core.model.Context, mb.pipe.run.spoofax.cfg.SpxCoreLangConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: String, val _2: org.spoofax.interpreter.terms.IStrategoTerm?, val _3: ArrayList<mb.pipe.run.core.model.parse.Token>?, val _4: ArrayList<mb.pipe.run.core.model.message.Msg>, val _5: mb.pipe.run.core.model.style.Styling?) : Tuple5<String, org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?> {
    constructor(tuple: Tuple5<String, org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<String, org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.pipe.run.core.model.parse.Token>?, ArrayList<mb.pipe.run.core.model.message.Msg>, mb.pipe.run.core.model.style.Styling?>) = Output(tuple)

  override val id = "processStringWithSpxCore"
  override fun BuildContext.build(input: processStringWithSpxCore.Input): processStringWithSpxCore.Output {
    var langImpl = requireOutput(mb.pipe.run.ceres.spoofax.core.CoreLoadLang::class.java, input.config.location())
    var langId = langImpl.v.id()
    var project = requireOutput(mb.pipe.run.ceres.spoofax.core.CoreLoadProj::class.java, input.context.currentDir())
    var projectDir = project.v.directory()
    var (ast, tokens, messages) = requireOutput(mb.pipe.run.ceres.spoofax.core.CoreParse::class.java, mb.pipe.run.ceres.spoofax.core.CoreParse.Input(langId, input.associatedFile, input.text))
    var styling: mb.pipe.run.core.model.style.Styling?
    if (ast != null) {
      styling = requireOutput(mb.pipe.run.ceres.spoofax.core.CoreStyle::class.java, mb.pipe.run.ceres.spoofax.core.CoreStyle.Input(langId, ast!!))
    } else {
      styling = null
    }
    return output(tuple(input.text, ast, tokens, messages, styling))
  }
}


class CeresBuilderModule : Module {
  override fun configure(binder: Binder) {
    val builders = binder.builderMapBinder()

    binder.bindBuilder<processStringWithSpxCore>(builders, "processStringWithSpxCore")
    binder.bindBuilder<processFileWithSpxCore>(builders, "processFileWithSpxCore")
    binder.bindBuilder<createWorkspaceConfig>(builders, "createWorkspaceConfig")
    binder.bindBuilder<getSpxCoreLangConfig>(builders, "getSpxCoreLangConfig")
    binder.bindBuilder<getLangSpecConfig>(builders, "getLangSpecConfig")
    binder.bindBuilder<processStringWithConfig>(builders, "processStringWithConfig")
    binder.bindBuilder<processString>(builders, "processString")
    binder.bindBuilder<processFileWithConfig>(builders, "processFileWithConfig")
    binder.bindBuilder<processProject>(builders, "processProject")
    binder.bindBuilder<style>(builders, "style")
    binder.bindBuilder<parse>(builders, "parse")
  }
}
