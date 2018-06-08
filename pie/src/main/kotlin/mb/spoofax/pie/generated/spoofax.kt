@file:Suppress("warnings")

package mb.spoofax.pie.generated

import java.io.Serializable
import java.nio.file.Paths
import com.google.inject.*
import com.google.inject.multibindings.MapBinder
import mb.pie.api.*
import mb.pie.vfs.path.*
import mb.pie.lang.runtime.path.*
import mb.pie.lang.runtime.util.*
import mb.pie.taskdefs.guice.*

class toMessage @Inject constructor(

) : TaskDef<mb.spoofax.api.message.PathMsg, mb.spoofax.api.message.Msg> {
  companion object {
    const val id = "toMessage"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: mb.spoofax.api.message.PathMsg): mb.spoofax.api.message.Msg = run {

    input
  }
}

class langSpecConfigForPath @Inject constructor(
  private val _createWorkspaceConfig: createWorkspaceConfig
) : TaskDef<langSpecConfigForPath.Input, mb.spoofax.runtime.cfg.LangSpecConfig?> {
  companion object {
    const val id = "langSpecConfigForPath"
  }

  data class Input(val path: PPath, val root: PPath) : Tuple2<PPath, PPath> {
    constructor(tuple: Tuple2<PPath, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: langSpecConfigForPath.Input): mb.spoofax.runtime.cfg.LangSpecConfig? = run {
    val workspace = require(_createWorkspaceConfig, input.root);
    if(workspace == null) run {
      return null
    };
    val extension = input.path.extension();
    if(extension == null) run {
      return null
    }
    workspace!!.langSpecConfigForExt(extension!!)
  }
}

class spxCoreConfigForPath @Inject constructor(

) : TaskDef<spxCoreConfigForPath.Input, mb.spoofax.runtime.cfg.SpxCoreConfig?> {
  companion object {
    const val id = "spxCoreConfigForPath"
  }

  data class Input(val workspace: mb.spoofax.runtime.cfg.WorkspaceConfig, val path: PPath) : Tuple2<mb.spoofax.runtime.cfg.WorkspaceConfig, PPath> {
    constructor(tuple: Tuple2<mb.spoofax.runtime.cfg.WorkspaceConfig, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: spxCoreConfigForPath.Input): mb.spoofax.runtime.cfg.SpxCoreConfig? = run {
    val extension = input.path.extension();
    if(extension == null) return null
    input.workspace.spxCoreConfigForExt(extension!!)
  }
}

class createWorkspaceConfig @Inject constructor(
  private val _mb_spoofax_pie_config_ParseWorkspaceCfg: mb.spoofax.pie.config.ParseWorkspaceCfg,
  private val read: Read,
  private val exists: Exists
) : TaskDef<PPath, mb.spoofax.runtime.cfg.WorkspaceConfig?> {
  companion object {
    const val id = "createWorkspaceConfig"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): mb.spoofax.runtime.cfg.WorkspaceConfig? = run {
    val cfgLang = mb.spoofax.runtime.cfg.ImmutableSpxCoreConfig.of(PPathImpl(java.nio.file.FileSystems.getDefault().getPath("/Users/gohla/metaborg/repo/pie/spoofax-pie/lang/cfg/langspec")), false, list("cfg"));
    val workspaceFile = input.resolve("root/workspace.cfg");
    if(!require(exists, workspaceFile)) return null;
    val text = require(read, workspaceFile)!!;
    val workspaceConfig = require(_mb_spoofax_pie_config_ParseWorkspaceCfg, mb.spoofax.pie.config.ParseWorkspaceCfg.Input(text, workspaceFile, input, cfgLang))
    workspaceConfig
  }
}

class processWorkspace @Inject constructor(
  private val listContents: ListContents,
  private val _processProject: processProject
) : TaskDef<PPath, ArrayList<Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>>>>> {
  companion object {
    const val id = "processWorkspace"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): ArrayList<Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>>>> = run {

    require(listContents, ListContents.Input(input, PPaths.regexPathMatcher("^[^.]((?!src-gen).)*\$"))).map { project -> require(_processProject, processProject.Input(project, input)) }.toCollection(ArrayList<Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>>>>())
  }
}

class processProject @Inject constructor(
  private val walkContents: WalkContents,
  private val _processFileWithSpxCore: processFileWithSpxCore,
  private val _processLangSpecInProject: processLangSpecInProject,
  private val _createWorkspaceConfig: createWorkspaceConfig
) : TaskDef<processProject.Input, processProject.Output> {
  companion object {
    const val id = "processProject"
  }

  data class Input(val project: PPath, val root: PPath) : Tuple2<PPath, PPath> {
    constructor(tuple: Tuple2<PPath, PPath>) : this(tuple.component1(), tuple.component2())
  }

  data class Output(val _1: ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>>>, val _2: ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>>) : Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>>> {
    constructor(tuple: Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>>>) : this(tuple.component1(), tuple.component2())
  }

  private fun output(tuple: Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>>>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processProject.Input): processProject.Output = run {
    val workspaceConfig = require(_createWorkspaceConfig, input.root);
    val noLangSpecResults: ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>>> = list();
    val noSpxCoreResults: ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>> = list();
    if(workspaceConfig == null) return output(tuple(noLangSpecResults, noSpxCoreResults));
    val workspace = workspaceConfig!!;
    val langSpecResults = workspace.langSpecConfigs().map { langSpec -> require(_processLangSpecInProject, processLangSpecInProject.Input(input.project, langSpec, input.root)) }.toCollection(ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>>>());
    val spxCoreResults = require(walkContents, WalkContents.Input(input.project, PPaths.extensionsPathWalker(workspace.spxCoreExtensions()))).map { file -> require(_processFileWithSpxCore, processFileWithSpxCore.Input(file, input.project, workspace)) }.toCollection(ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>>())
    output(tuple(langSpecResults, spxCoreResults))
  }
}

class processLangSpecInProject @Inject constructor(
  private val walkContents: WalkContents,
  private val _processFile: processFile
) : TaskDef<processLangSpecInProject.Input, ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>>> {
  companion object {
    const val id = "processLangSpecInProject"
  }

  data class Input(val project: PPath, val langSpec: mb.spoofax.runtime.cfg.LangSpecConfig, val root: PPath) : Tuple3<PPath, mb.spoofax.runtime.cfg.LangSpecConfig, PPath> {
    constructor(tuple: Tuple3<PPath, mb.spoofax.runtime.cfg.LangSpecConfig, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: processLangSpecInProject.Input): ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>> = run {

    require(walkContents, WalkContents.Input(input.project, PPaths.extensionsPathWalker(input.langSpec.extensions()))).map { file -> require(_processFile, processFile.Input(file, input.project, input.root)) }.toCollection(ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>>())
  }
}

class processEditor @Inject constructor(
  private val _processStringWithSpxCore: processStringWithSpxCore,
  private val _processString: processString,
  private val _createWorkspaceConfig: createWorkspaceConfig
) : TaskDef<processEditor.Input, processEditor.Output?> {
  companion object {
    const val id = "processEditor"
  }

  data class Input(val text: String, val file: PPath, val project: PPath, val root: PPath) : Tuple4<String, PPath, PPath, PPath> {
    constructor(tuple: Tuple4<String, PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.spoofax.api.parse.Token>?, val _2: ArrayList<mb.spoofax.api.message.Msg>, val _3: mb.spoofax.api.style.Styling?, val _4: mb.spoofax.runtime.nabl.ConstraintSolverSolution?) : Tuple4<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple4<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>?) = if(tuple == null) null else Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processEditor.Input): processEditor.Output? = run {
    val workspaceConfig = require(_createWorkspaceConfig, input.root);
    if(workspaceConfig == null) return null;
    val workspace = workspaceConfig!!;
    val extension = input.file.extension();
    if(extension == null) return null;
    val langSpecConfig = workspace.langSpecConfigForExt(extension!!);
    if(langSpecConfig != null) run {
      val langSpec = langSpecConfig!!;
      val (tokens, messages, styling, solution) = require(_processString, processString.Input(input.text, input.file, input.project, input.root));
      return output(tuple(tokens, messages, styling, solution))
    };
    val spxCoreConfig = workspace.spxCoreConfigForExt(extension!!);
    if(spxCoreConfig != null) run {
      val (tokens, messages, styling) = require(_processStringWithSpxCore, processStringWithSpxCore.Input(input.text, input.file, spxCoreConfig!!));
      val noSolution: mb.spoofax.runtime.nabl.ConstraintSolverSolution? = null;
      return output(tuple(tokens, messages, styling, noSolution) as Tuple4<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>?)
    }
    null
  }
}

class processFile @Inject constructor(
  private val _processString: processString,
  private val read: Read,
  private val _langSpecConfigForPath: langSpecConfigForPath,
  private val exists: Exists,
  private val _emptyFileResult: emptyFileResult
) : TaskDef<processFile.Input, processFile.Output> {
  companion object {
    const val id = "processFile"
  }

  data class Input(val file: PPath, val project: PPath, val root: PPath) : Tuple3<PPath, PPath, PPath> {
    constructor(tuple: Tuple3<PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.api.parse.Token>?, val _3: ArrayList<mb.spoofax.api.message.Msg>, val _4: mb.spoofax.api.style.Styling?, val _5: mb.spoofax.runtime.nabl.ConstraintSolverSolution?) : Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processFile.Input): processFile.Output = run {
    if(!mb.spoofax.pie.shouldProcessFile(input.file)) run {
      return output(require(_emptyFileResult, input.file))
    };
    if(!require(exists, input.file)) run {
      return output(require(_emptyFileResult, input.file))
    };
    val langSpec = require(_langSpecConfigForPath, langSpecConfigForPath.Input(input.file, input.root))
    output(if(langSpec != null) run {
      val text = require(read, input.file)!!;
      val (tokens, messages, styling, solution) = require(_processString, processString.Input(text, input.file, input.project, input.root));
      tuple(input.file, tokens, messages, styling, solution)
    } else run {
      require(_emptyFileResult, input.file)
    })
  }
}

class emptyFileResult @Inject constructor(

) : TaskDef<PPath, emptyFileResult.Output> {
  companion object {
    const val id = "emptyFileResult"
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.api.parse.Token>?, val _3: ArrayList<mb.spoofax.api.message.Msg>, val _4: mb.spoofax.api.style.Styling?, val _5: mb.spoofax.runtime.nabl.ConstraintSolverSolution?) : Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): emptyFileResult.Output = run {
    val emptyTokens: ArrayList<mb.spoofax.api.parse.Token>? = null;
    val emptyMessages: ArrayList<mb.spoofax.api.message.Msg> = list();
    val emptyStyling: mb.spoofax.api.style.Styling? = null;
    val emptySolution: mb.spoofax.runtime.nabl.ConstraintSolverSolution? = null
    output(tuple(input, emptyTokens, emptyMessages, emptyStyling, emptySolution))
  }
}

class processString @Inject constructor(
  private val _emptyResult: emptyResult,
  private val _solveFile: solveFile,
  private val _style: style,
  private val _parse: parse
) : TaskDef<processString.Input, processString.Output> {
  companion object {
    const val id = "processString"
  }

  data class Input(val text: String, val file: PPath, val project: PPath, val root: PPath) : Tuple4<String, PPath, PPath, PPath> {
    constructor(tuple: Tuple4<String, PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.spoofax.api.parse.Token>?, val _2: ArrayList<mb.spoofax.api.message.Msg>, val _3: mb.spoofax.api.style.Styling?, val _4: mb.spoofax.runtime.nabl.ConstraintSolverSolution?) : Tuple4<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple4<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processString.Input): processString.Output = run {
    val langSpecExt = input.file.extension()
    output(if(langSpecExt != null) run {
      val (ast, tokenStream, messages) = require(_parse, parse.Input(input.text, input.file, langSpecExt!!, input.root));
      val styling: mb.spoofax.api.style.Styling? = if(tokenStream == null) null else require(_style, style.Input(tokenStream!!, langSpecExt!!, input.root));
      val solution: mb.spoofax.runtime.nabl.ConstraintSolverSolution? = if(ast == null) null else require(_solveFile, solveFile.Input(ast!!, input.file, input.project, langSpecExt!!, input.root));
      tuple(tokenStream, messages, styling, solution)
    } else run {
      require(_emptyResult, None.instance)
    })
  }
}

class emptyResult @Inject constructor(

) : TaskDef<None, emptyResult.Output> {
  companion object {
    const val id = "emptyResult"
  }

  data class Output(val _1: ArrayList<mb.spoofax.api.parse.Token>?, val _2: ArrayList<mb.spoofax.api.message.Msg>, val _3: mb.spoofax.api.style.Styling?, val _4: mb.spoofax.runtime.nabl.ConstraintSolverSolution?) : Tuple4<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple4<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?, mb.spoofax.runtime.nabl.ConstraintSolverSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: None): emptyResult.Output = run {
    val emptyTokens: ArrayList<mb.spoofax.api.parse.Token>? = null;
    val emptyMessages: ArrayList<mb.spoofax.api.message.Msg> = list();
    val emptyStyling: mb.spoofax.api.style.Styling? = null;
    val emptySolution: mb.spoofax.runtime.nabl.ConstraintSolverSolution? = null
    output(tuple(emptyTokens, emptyMessages, emptyStyling, emptySolution))
  }
}

class parse @Inject constructor(
  private val _mb_spoofax_pie_jsglr_JSGLRParse: mb.spoofax.pie.jsglr.JSGLRParse,
  private val _emptyParse: emptyParse,
  private val _mb_spoofax_pie_sdf3_SDF3ToJSGLRParseTable: mb.spoofax.pie.sdf3.SDF3ToJSGLRParseTable
) : TaskDef<parse.Input, parse.Output> {
  companion object {
    const val id = "parse"
  }

  data class Input(val text: String, val file: PPath, val langSpecExt: String, val root: PPath) : Tuple4<String, PPath, String, PPath> {
    constructor(tuple: Tuple4<String, PPath, String, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.spoofax.api.parse.Token>?, val _3: ArrayList<mb.spoofax.api.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: parse.Input): parse.Output = run {
    val parseTable = require(_mb_spoofax_pie_sdf3_SDF3ToJSGLRParseTable, mb.spoofax.pie.sdf3.SDF3ToJSGLRParseTable.Input(input.langSpecExt, input.root));
    if(parseTable == null) return output(require(_emptyParse, None.instance))
    output(require(_mb_spoofax_pie_jsglr_JSGLRParse, mb.spoofax.pie.jsglr.JSGLRParse.Input(input.text, parseTable!!, input.file, input.langSpecExt, input.root)))
  }
}

class emptyParse @Inject constructor(

) : TaskDef<None, emptyParse.Output> {
  companion object {
    const val id = "emptyParse"
  }

  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.spoofax.api.parse.Token>?, val _3: ArrayList<mb.spoofax.api.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: None): emptyParse.Output = run {
    val emptyAst: org.spoofax.interpreter.terms.IStrategoTerm? = null;
    val emptyTokens: ArrayList<mb.spoofax.api.parse.Token>? = null;
    val emptyMessages: ArrayList<mb.spoofax.api.message.Msg> = list()
    output(tuple(emptyAst, emptyTokens, emptyMessages))
  }
}

class createSignatures @Inject constructor(
  private val _mb_spoofax_pie_sdf3_SDF3ToStrategoSignatures: mb.spoofax.pie.sdf3.SDF3ToStrategoSignatures
) : TaskDef<createSignatures.Input, mb.spoofax.runtime.sdf.Signatures?> {
  companion object {
    const val id = "createSignatures"
  }

  data class Input(val langSpecExt: String, val root: PPath) : Tuple2<String, PPath> {
    constructor(tuple: Tuple2<String, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: createSignatures.Input): mb.spoofax.runtime.sdf.Signatures? = run {

    require(_mb_spoofax_pie_sdf3_SDF3ToStrategoSignatures, mb.spoofax.pie.sdf3.SDF3ToStrategoSignatures.Input(input.langSpecExt, input.root))
  }
}

class style @Inject constructor(
  private val _mb_spoofax_pie_style_SpoofaxStyle: mb.spoofax.pie.style.SpoofaxStyle,
  private val _mb_spoofax_pie_esv_ESVToStylingRules: mb.spoofax.pie.esv.ESVToStylingRules
) : TaskDef<style.Input, mb.spoofax.api.style.Styling?> {
  companion object {
    const val id = "style"
  }

  data class Input(val tokenStream: ArrayList<mb.spoofax.api.parse.Token>, val langSpecExt: String, val root: PPath) : Tuple3<ArrayList<mb.spoofax.api.parse.Token>, String, PPath> {
    constructor(tuple: Tuple3<ArrayList<mb.spoofax.api.parse.Token>, String, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: style.Input): mb.spoofax.api.style.Styling? = run {
    val syntaxStyler = require(_mb_spoofax_pie_esv_ESVToStylingRules, mb.spoofax.pie.esv.ESVToStylingRules.Input(input.langSpecExt, input.root));
    if(syntaxStyler == null) return null
    require(_mb_spoofax_pie_style_SpoofaxStyle, mb.spoofax.pie.style.SpoofaxStyle.Input(input.tokenStream, syntaxStyler!!)) as mb.spoofax.api.style.Styling?
  }
}

class solveFile @Inject constructor(
  private val _mb_spoofax_pie_constraint_CSolveFinal: mb.spoofax.pie.constraint.CSolveFinal,
  private val _mb_spoofax_pie_constraint_CSolveDocument: mb.spoofax.pie.constraint.CSolveDocument,
  private val _mb_spoofax_pie_constraint_CGenDocument: mb.spoofax.pie.constraint.CGenDocument,
  private val _mb_spoofax_pie_constraint_CSolveGlobal: mb.spoofax.pie.constraint.CSolveGlobal,
  private val _mb_spoofax_pie_constraint_CGenGlobal: mb.spoofax.pie.constraint.CGenGlobal
) : TaskDef<solveFile.Input, mb.spoofax.runtime.nabl.ConstraintSolverSolution?> {
  companion object {
    const val id = "solveFile"
  }

  data class Input(val ast: org.spoofax.interpreter.terms.IStrategoTerm, val file: PPath, val project: PPath, val langSpecExt: String, val root: PPath) : Tuple5<org.spoofax.interpreter.terms.IStrategoTerm, PPath, PPath, String, PPath> {
    constructor(tuple: Tuple5<org.spoofax.interpreter.terms.IStrategoTerm, PPath, PPath, String, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: solveFile.Input): mb.spoofax.runtime.nabl.ConstraintSolverSolution? = run {
    val globalConstraints = require(_mb_spoofax_pie_constraint_CGenGlobal, mb.spoofax.pie.constraint.CGenGlobal.Input(input.langSpecExt, input.root));
    if(globalConstraints == null) return null;
    val globalSolution = require(_mb_spoofax_pie_constraint_CSolveGlobal, globalConstraints!!);
    if(globalSolution == null) return null;
    val documentConstraints = require(_mb_spoofax_pie_constraint_CGenDocument, mb.spoofax.pie.constraint.CGenDocument.Input(globalConstraints!!, input.ast, input.file, input.langSpecExt, input.root));
    if(documentConstraints == null) return null;
    val documentSolution = require(_mb_spoofax_pie_constraint_CSolveDocument, mb.spoofax.pie.constraint.CSolveDocument.Input(documentConstraints!!, globalConstraints!!, globalSolution!!));
    if(documentSolution == null) return null;
    val solution = require(_mb_spoofax_pie_constraint_CSolveFinal, mb.spoofax.pie.constraint.CSolveFinal.Input(list(documentSolution!!), globalSolution!!, input.project))
    solution
  }
}

class processFileWithSpxCore @Inject constructor(
  private val _processStringWithSpxCore: processStringWithSpxCore,
  private val read: Read,
  private val _spxCoreConfigForPath: spxCoreConfigForPath,
  private val exists: Exists,
  private val _emptySpxCoreFile: emptySpxCoreFile
) : TaskDef<processFileWithSpxCore.Input, processFileWithSpxCore.Output> {
  companion object {
    const val id = "processFileWithSpxCore"
  }

  data class Input(val file: PPath, val project: PPath, val workspace: mb.spoofax.runtime.cfg.WorkspaceConfig) : Tuple3<PPath, PPath, mb.spoofax.runtime.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<PPath, PPath, mb.spoofax.runtime.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.api.parse.Token>?, val _3: ArrayList<mb.spoofax.api.message.Msg>, val _4: mb.spoofax.api.style.Styling?) : Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?> {
    constructor(tuple: Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processFileWithSpxCore.Input): processFileWithSpxCore.Output = run {
    if(!mb.spoofax.pie.shouldProcessFile(input.file)) run {
      return output(require(_emptySpxCoreFile, input.file))
    };
    if(!require(exists, input.file)) run {
      return output(require(_emptySpxCoreFile, input.file))
    };
    val config = require(_spxCoreConfigForPath, spxCoreConfigForPath.Input(input.workspace, input.file))
    output(if(config != null) run {
      val text = require(read, input.file)!!;
      val (tokens, messages, styling) = require(_processStringWithSpxCore, processStringWithSpxCore.Input(text, input.file, config!!));
      tuple(input.file, tokens, messages, styling)
    } else run {
      require(_emptySpxCoreFile, input.file)
    })
  }
}

class emptySpxCoreFile @Inject constructor(

) : TaskDef<PPath, emptySpxCoreFile.Output> {
  companion object {
    const val id = "emptySpxCoreFile"
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.api.parse.Token>?, val _3: ArrayList<mb.spoofax.api.message.Msg>, val _4: mb.spoofax.api.style.Styling?) : Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?> {
    constructor(tuple: Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<PPath, ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): emptySpxCoreFile.Output = run {
    val emptyTokens: ArrayList<mb.spoofax.api.parse.Token>? = null;
    val emptyMessages: ArrayList<mb.spoofax.api.message.Msg> = list();
    val emptyStyling: mb.spoofax.api.style.Styling? = null
    output(tuple(input, emptyTokens, emptyMessages, emptyStyling))
  }
}

class processStringWithSpxCore @Inject constructor(
  private val _mb_spoofax_pie_legacy_LegacyStyle: mb.spoofax.pie.legacy.LegacyStyle,
  private val _mb_spoofax_pie_legacy_LegacyParse: mb.spoofax.pie.legacy.LegacyParse
) : TaskDef<processStringWithSpxCore.Input, processStringWithSpxCore.Output> {
  companion object {
    const val id = "processStringWithSpxCore"
  }

  data class Input(val text: String, val file: PPath, val config: mb.spoofax.runtime.cfg.SpxCoreConfig) : Tuple3<String, PPath, mb.spoofax.runtime.cfg.SpxCoreConfig> {
    constructor(tuple: Tuple3<String, PPath, mb.spoofax.runtime.cfg.SpxCoreConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: ArrayList<mb.spoofax.api.parse.Token>?, val _2: ArrayList<mb.spoofax.api.message.Msg>, val _3: mb.spoofax.api.style.Styling?) : Tuple3<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.spoofax.api.parse.Token>?, ArrayList<mb.spoofax.api.message.Msg>, mb.spoofax.api.style.Styling?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processStringWithSpxCore.Input): processStringWithSpxCore.Output = run {
    val (ast, tokens, messages, _) = require(_mb_spoofax_pie_legacy_LegacyParse, mb.spoofax.pie.legacy.LegacyParse.Input(input.config, input.text, input.file));
    val styling: mb.spoofax.api.style.Styling? = if(ast == null || tokens == null) null else require(_mb_spoofax_pie_legacy_LegacyStyle, mb.spoofax.pie.legacy.LegacyStyle.Input(input.config, tokens!!, ast!!)) as mb.spoofax.api.style.Styling?
    output(tuple(tokens, messages, styling))
  }
}


class TaskDefsModule_spoofax : TaskDefsModule() {
  override fun Binder.bindTaskDefs(taskDefsBinder: MapBinder<String, TaskDef<*, *>>) {
    bindTaskDef<processStringWithSpxCore>(taskDefsBinder, "processStringWithSpxCore")
    bindTaskDef<emptySpxCoreFile>(taskDefsBinder, "emptySpxCoreFile")
    bindTaskDef<processFileWithSpxCore>(taskDefsBinder, "processFileWithSpxCore")
    bindTaskDef<solveFile>(taskDefsBinder, "solveFile")
    bindTaskDef<style>(taskDefsBinder, "style")
    bindTaskDef<createSignatures>(taskDefsBinder, "createSignatures")
    bindTaskDef<emptyParse>(taskDefsBinder, "emptyParse")
    bindTaskDef<parse>(taskDefsBinder, "parse")
    bindTaskDef<emptyResult>(taskDefsBinder, "emptyResult")
    bindTaskDef<processString>(taskDefsBinder, "processString")
    bindTaskDef<emptyFileResult>(taskDefsBinder, "emptyFileResult")
    bindTaskDef<processFile>(taskDefsBinder, "processFile")
    bindTaskDef<processEditor>(taskDefsBinder, "processEditor")
    bindTaskDef<processLangSpecInProject>(taskDefsBinder, "processLangSpecInProject")
    bindTaskDef<processProject>(taskDefsBinder, "processProject")
    bindTaskDef<processWorkspace>(taskDefsBinder, "processWorkspace")
    bindTaskDef<createWorkspaceConfig>(taskDefsBinder, "createWorkspaceConfig")
    bindTaskDef<spxCoreConfigForPath>(taskDefsBinder, "spxCoreConfigForPath")
    bindTaskDef<langSpecConfigForPath>(taskDefsBinder, "langSpecConfigForPath")
    bindTaskDef<toMessage>(taskDefsBinder, "toMessage")
  }
}
