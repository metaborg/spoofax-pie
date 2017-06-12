package mb.pipe.run.ceres.generated

import com.google.inject.Binder
import com.google.inject.Module
import mb.ceres.*
import mb.pipe.run.ceres.util.*
import mb.pipe.run.core.PipeRunEx
import mb.pipe.run.core.model.Context
import mb.pipe.run.core.model.message.Msg
import mb.pipe.run.core.model.parse.Token
import mb.pipe.run.core.model.style.Styling
import mb.pipe.run.core.path.PPath
import org.spoofax.interpreter.terms.IStrategoTerm

class parse : Builder<parse.Input, parse.Output> {
  data class Input(val text: String, val context: Context) : Tuple2<String, Context> {
    constructor(tuple: Tuple2<String, Context>) : this(tuple.component1(), tuple.component2())
  }

  data class Output(val _1: IStrategoTerm?, val _2: List<Token>?, val _3: List<Msg>) : Tuple3<IStrategoTerm?, List<Token>?, List<Msg>> {
    constructor(tuple: Tuple3<IStrategoTerm?, List<Token>?, List<Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = "parse"
  override fun BuildContext.build(input: parse.Input): parse.Output {
    var currentDir = input.context.currentDir()
    var langLoc = mb.pipe.run.ceres.path.resolve("/Users/gohla/spoofax/master/repo/spoofax-releng/sdf/org.metaborg.meta.lang.template")
    var specDir = currentDir
    var mainFile = currentDir.resolve("syntax/minimal.sdf3")
    var includedFiles: List<PPath> = listOf()
    var parseTable = requireOutput(mb.pipe.run.ceres.spoofax.GenerateTable::class.java, mb.pipe.run.ceres.spoofax.GenerateTable.Input(langLoc, specDir, mainFile, includedFiles))
    if (parseTable == null)
      throw BuildException("Unable to build parse table".toString())
    return Output(requireOutput(mb.pipe.run.ceres.spoofax.Parse::class.java, mb.pipe.run.ceres.spoofax.Parse.Input(input.text, "Start", parseTable)))
  }
}

class style : Builder<style.Input, Styling> {
  data class Input(val tokenStream: List<Token>, val context: Context) : Tuple2<List<Token>, Context> {
    constructor(tuple: Tuple2<List<Token>, Context>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "style"
  override fun BuildContext.build(input: style.Input): Styling {
    var currentDir = input.context.currentDir()
    var langLoc = mb.pipe.run.ceres.path.resolve("/Users/gohla/spoofax/master/repo/spoofax-releng/esv/org.metaborg.meta.lang.esv")
    var specDir = currentDir
    var mainFile = currentDir.resolve("editor/Main.esv")
    var includedFiles: List<PPath> = listOf()
    var syntaxStyler = requireOutput(mb.pipe.run.ceres.spoofax.GenerateStylerRules::class.java, mb.pipe.run.ceres.spoofax.GenerateStylerRules.Input(langLoc, specDir, mainFile, includedFiles))
    if (syntaxStyler == null)
      throw BuildException("Unable to build syntax styler".toString())
    return requireOutput(mb.pipe.run.ceres.spoofax.Style::class.java, mb.pipe.run.ceres.spoofax.Style.Input(input.tokenStream, syntaxStyler))
  }
}

class processFile : Builder<processFile.Input, processFile.Output> {
  data class Input(val file: PPath, val context: Context) : Tuple2<PPath, Context> {
    constructor(tuple: Tuple2<PPath, Context>) : this(tuple.component1(), tuple.component2())
  }

  data class Output(val _1: PPath, val _2: String, val _3: IStrategoTerm?, val _4: List<Token>?, val _5: List<Msg>, val _6: Styling?) : Tuple6<PPath, String, IStrategoTerm?, List<Token>?, List<Msg>, Styling?> {
    constructor(tuple: Tuple6<PPath, String, IStrategoTerm?, List<Token>?, List<Msg>, Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5(), tuple.component6())
  }

  override val id = "processFile"
  override fun BuildContext.build(input: processFile.Input): processFile.Output {
    var text = requireOutput(mb.pipe.run.ceres.path.Read::class.java, input.file)
    var (_, ast, tokenStream, messages, styling) = requireOutput(processString::class.java, processString.Input(text, input.context))
    return Output(tuple(input.file, text, ast, tokenStream, messages, styling))
  }
}

class processString : Builder<processString.Input, processString.Output> {
  data class Input(val text: String, val context: Context) : Tuple2<String, Context> {
    constructor(tuple: Tuple2<String, Context>) : this(tuple.component1(), tuple.component2())
  }

  data class Output(val _1: String, val _2: IStrategoTerm?, val _3: List<Token>?, val _4: List<Msg>, val _5: Styling?) : Tuple5<String, IStrategoTerm?, List<Token>?, List<Msg>, Styling?> {
    constructor(tuple: Tuple5<String, IStrategoTerm?, List<Token>?, List<Msg>, Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  override val id = "processString"
  override fun BuildContext.build(input: processString.Input): processString.Output {
    var (ast, tokenStream, messages) = requireOutput(parse::class.java, parse.Input(input.text, input.context))
    var styling: Styling?
    if (tokenStream != null) {
      styling = requireOutput(style::class.java, style.Input(tokenStream, input.context))
    } else {
      styling = null
    }
    return Output(tuple(input.text, ast, tokenStream, messages, styling))
  }
}


class CeresBuilderModule : Module {
  override fun configure(binder: Binder) {
    val builders = binder.builderMapBinder()

    binder.bindBuilder<processString>(builders, "processString")
    binder.bindBuilder<processFile>(builders, "processFile")
    binder.bindBuilder<style>(builders, "style")
    binder.bindBuilder<parse>(builders, "parse")
  }
}
