@file:Suppress("warnings")

package mb.spoofax.runtime.pie.generated

import com.google.inject.Binder
import com.google.inject.Module
import mb.pie.api.*
import mb.pie.lang.runtime.path.*
import mb.pie.lang.runtime.util.*
import mb.pie.taskdefs.guice.bindTaskDef
import mb.pie.taskdefs.guice.taskDefsBinder
import mb.pie.vfs.path.*
import mb.spoofax.runtime.impl.cfg.*
import mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution
import mb.spoofax.runtime.impl.sdf.Signatures
import mb.spoofax.runtime.model.message.Msg
import mb.spoofax.runtime.model.message.PathMsg
import mb.spoofax.runtime.model.parse.Token
import mb.spoofax.runtime.model.style.Styling
import java.nio.file.FileSystems

class toMessage : TaskDef<PathMsg, Msg> {
  companion object {
    const val id = "toMessage"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: mb.spoofax.runtime.model.message.PathMsg): mb.spoofax.runtime.model.message.Msg = run {

    input
  }
}

class langSpecConfigForPath : TaskDef<langSpecConfigForPath.Input, LangSpecConfig?> {
  companion object {
    const val id = "langSpecConfigForPath"
  }

  data class Input(val path: PPath, val root: PPath) : Tuple2<PPath, PPath> {
    constructor(tuple: Tuple2<PPath, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: langSpecConfigForPath.Input): mb.spoofax.runtime.impl.cfg.LangSpecConfig? = run {
    val workspace = requireOutput(createWorkspaceConfig::class.java, createWorkspaceConfig.id, input.root);
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

class spxCoreConfigForPath : TaskDef<spxCoreConfigForPath.Input, SpxCoreConfig?> {
  companion object {
    const val id = "spxCoreConfigForPath"
  }

  data class Input(val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig, val path: PPath) : Tuple2<mb.spoofax.runtime.impl.cfg.WorkspaceConfig, PPath> {
    constructor(tuple: Tuple2<mb.spoofax.runtime.impl.cfg.WorkspaceConfig, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: spxCoreConfigForPath.Input): mb.spoofax.runtime.impl.cfg.SpxCoreConfig? = run {
    val extension = input.path.extension();
    if(extension == null) return null
    input.workspace.spxCoreConfigForExt(extension!!)
  }
}

class createWorkspaceConfig : TaskDef<PPath, WorkspaceConfig?> {
  companion object {
    const val id = "createWorkspaceConfig"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): mb.spoofax.runtime.impl.cfg.WorkspaceConfig? = run {
    val cfgLang = mb.spoofax.runtime.impl.cfg.ImmutableSpxCoreConfig.of(PPathImpl(FileSystems.getDefault().getPath("/Users/gohla/metaborg/repo/pie/spoofax-pie/lang/cfg/langspec")), false, list("cfg"));
    val workspaceFile = input.resolve("root/workspace.cfg");
    if(!requireOutput(Exists::class.java, Exists.id, workspaceFile)) return null;
    val text = requireOutput(Read::class.java, Read.id, workspaceFile)!!;
    val workspaceConfig = requireOutput(mb.spoofax.runtime.pie.config.ParseWorkspaceCfg::class.java, mb.spoofax.runtime.pie.config.ParseWorkspaceCfg.id, mb.spoofax.runtime.pie.config.ParseWorkspaceCfg.Input(text, workspaceFile, input, cfgLang))
    workspaceConfig
  }
}

class processWorkspace : TaskDef<PPath, ArrayList<Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?, ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?>>>>> {
  companion object {
    const val id = "processWorkspace"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): ArrayList<Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>> = run {

    requireOutput(ListContents::class.java, ListContents.id, ListContents.Input(input, PPaths.regexPathMatcher("^[^.]((?!src-gen).)*\$"))).map { project -> requireOutput(processProject::class.java, processProject.id, processProject.Input(project, input)) }.toCollection(ArrayList<Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>>())
  }
}

class processProject : TaskDef<processProject.Input, processProject.Output> {
  companion object {
    const val id = "processProject"
  }

  data class Input(val project: PPath, val root: PPath) : Tuple2<PPath, PPath> {
    constructor(tuple: Tuple2<PPath, PPath>) : this(tuple.component1(), tuple.component2())
  }

  data class Output(val _1: ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, val _2: ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>) : Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>> {
    constructor(tuple: Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>) : this(tuple.component1(), tuple.component2())
  }

  private fun output(tuple: Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processProject.Input): processProject.Output = run {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class.java, createWorkspaceConfig.id, input.root);
    val noLangSpecResults: ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>> = list();
    val noSpxCoreResults: ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>> = list();
    if(workspaceConfig == null) return output(tuple(noLangSpecResults, noSpxCoreResults));
    val workspace = workspaceConfig!!;
    val langSpecResults = workspace.langSpecConfigs().map { langSpec -> requireOutput(processLangSpecInProject::class.java, processLangSpecInProject.id, processLangSpecInProject.Input(input.project, langSpec, input.root)) }.toCollection(ArrayList<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>());
    val spxCoreResults = requireOutput(WalkContents::class.java, WalkContents.id, WalkContents.Input(input.project, PPaths.extensionsPathWalker(workspace.spxCoreExtensions()))).map { file -> requireOutput(processFileWithSpxCore::class.java, processFileWithSpxCore.id, processFileWithSpxCore.Input(file, input.project, workspace)) }.toCollection(ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>())
    output(tuple(langSpecResults, spxCoreResults))
  }
}

class processLangSpecInProject : TaskDef<processLangSpecInProject.Input, ArrayList<Tuple5<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?, ConstraintSolverSolution?>>> {
  companion object {
    const val id = "processLangSpecInProject"
  }

  data class Input(val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val root: PPath) : Tuple3<PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, PPath> {
    constructor(tuple: Tuple3<PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: processLangSpecInProject.Input): ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>> = run {

    requireOutput(WalkContents::class.java, WalkContents.id, WalkContents.Input(input.project, PPaths.extensionsPathWalker(input.langSpec.extensions()))).map { file -> requireOutput(processFile::class.java, processFile.id, processFile.Input(file, input.project, input.root)) }.toCollection(ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>())
  }
}

class processEditor : TaskDef<processEditor.Input, processEditor.Output?> {
  companion object {
    const val id = "processEditor"
  }

  data class Input(val text: String, val file: PPath, val project: PPath, val root: PPath) : Tuple4<String, PPath, PPath, PPath> {
    constructor(tuple: Tuple4<String, PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?, val _4: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?) : Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>?) = if(tuple == null) null else Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processEditor.Input): processEditor.Output? = run {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class.java, createWorkspaceConfig.id, input.root);
    if(workspaceConfig == null) return null;
    val workspace = workspaceConfig!!;
    val extension = input.file.extension();
    if(extension == null) return null;
    val langSpecConfig = workspace.langSpecConfigForExt(extension!!);
    if(langSpecConfig != null) run {
      val langSpec = langSpecConfig!!;
      val (tokens, messages, styling, solution) = requireOutput(processString::class.java, processString.id, processString.Input(input.text, input.file, input.project, input.root));
      return output(tuple(tokens, messages, styling, solution))
    };
    val spxCoreConfig = workspace.spxCoreConfigForExt(extension!!);
    if(spxCoreConfig != null) run {
      val (tokens, messages, styling) = requireOutput(processStringWithSpxCore::class.java, processStringWithSpxCore.id, processStringWithSpxCore.Input(input.text, input.file, spxCoreConfig!!));
      val noSolution: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = null;
      return output(tuple(tokens, messages, styling, noSolution) as Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>?)
    }
    null
  }
}

class processFile : TaskDef<processFile.Input, processFile.Output> {
  companion object {
    const val id = "processFile"
  }

  data class Input(val file: PPath, val project: PPath, val root: PPath) : Tuple3<PPath, PPath, PPath> {
    constructor(tuple: Tuple3<PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?, val _5: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?) : Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processFile.Input): processFile.Output = run {
    if(!mb.spoofax.runtime.pie.shouldProcessFile(input.file)) run {
      return output(requireOutput(emptyFileResult::class.java, emptyFileResult.id, input.file))
    };
    if(!requireOutput(Exists::class.java, Exists.id, input.file)) run {
      return output(requireOutput(emptyFileResult::class.java, emptyFileResult.id, input.file))
    };
    val langSpec = requireOutput(langSpecConfigForPath::class.java, langSpecConfigForPath.id, langSpecConfigForPath.Input(input.file, input.root))
    output(if(langSpec != null) run {
      val text = requireOutput(Read::class.java, Read.id, input.file)!!;
      val (tokens, messages, styling, solution) = requireOutput(processString::class.java, processString.id, processString.Input(text, input.file, input.project, input.root));
      tuple(input.file, tokens, messages, styling, solution)
    } else run {
      requireOutput(emptyFileResult::class.java, emptyFileResult.id, input.file)
    })
  }
}

class emptyFileResult : TaskDef<PPath, emptyFileResult.Output> {
  companion object {
    const val id = "emptyFileResult"
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?, val _5: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?) : Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): emptyFileResult.Output = run {
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null;
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list();
    val emptyStyling: mb.spoofax.runtime.model.style.Styling? = null;
    val emptySolution: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = null
    output(tuple(input, emptyTokens, emptyMessages, emptyStyling, emptySolution))
  }
}

class processString : TaskDef<processString.Input, processString.Output> {
  companion object {
    const val id = "processString"
  }

  data class Input(val text: String, val file: PPath, val project: PPath, val root: PPath) : Tuple4<String, PPath, PPath, PPath> {
    constructor(tuple: Tuple4<String, PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?, val _4: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?) : Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processString.Input): processString.Output = run {
    val langSpecExt = input.file.extension()
    output(if(langSpecExt != null) run {
      val (ast, tokenStream, messages) = requireOutput(parse::class.java, parse.id, parse.Input(input.text, input.file, langSpecExt!!, input.root));
      val styling: mb.spoofax.runtime.model.style.Styling? = if(tokenStream == null) null else requireOutput(style::class.java, style.id, style.Input(tokenStream!!, langSpecExt!!, input.root));
      val solution: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = if(ast == null) null else requireOutput(solveFile::class.java, solveFile.id, solveFile.Input(ast!!, input.file, input.project, langSpecExt!!, input.root));
      tuple(tokenStream, messages, styling, solution)
    } else run {
      requireOutput(emptyResult::class.java, emptyResult.id, None.instance)
    })
  }
}

class emptyResult : TaskDef<None, emptyResult.Output> {
  companion object {
    const val id = "emptyResult"
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?, val _4: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?) : Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: None): emptyResult.Output = run {
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null;
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list();
    val emptyStyling: mb.spoofax.runtime.model.style.Styling? = null;
    val emptySolution: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = null
    output(tuple(emptyTokens, emptyMessages, emptyStyling, emptySolution))
  }
}

class parse : TaskDef<parse.Input, parse.Output> {
  companion object {
    const val id = "parse"
  }

  data class Input(val text: String, val file: PPath, val langSpecExt: String, val root: PPath) : Tuple4<String, PPath, String, PPath> {
    constructor(tuple: Tuple4<String, PPath, String, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: parse.Input): parse.Output = run {
    val parseTable = requireOutput(mb.spoofax.runtime.pie.sdf3.CompileParseTable::class.java, mb.spoofax.runtime.pie.sdf3.CompileParseTable.id, mb.spoofax.runtime.pie.sdf3.CompileParseTable.Input(input.langSpecExt, input.root));
    if(parseTable == null) return output(requireOutput(emptyParse::class.java, emptyParse.id, None.instance))
    output(requireOutput(mb.spoofax.runtime.pie.sdf3.Parse::class.java, mb.spoofax.runtime.pie.sdf3.Parse.id, mb.spoofax.runtime.pie.sdf3.Parse.Input(input.text, parseTable!!, input.file, input.langSpecExt, input.root)))
  }
}

class emptyParse : TaskDef<None, emptyParse.Output> {
  companion object {
    const val id = "emptyParse"
  }

  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: None): emptyParse.Output = run {
    val emptyAst: org.spoofax.interpreter.terms.IStrategoTerm? = null;
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null;
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list()
    output(tuple(emptyAst, emptyTokens, emptyMessages))
  }
}

class createSignatures : TaskDef<createSignatures.Input, Signatures?> {
  companion object {
    const val id = "createSignatures"
  }

  data class Input(val langSpecExt: String, val root: PPath) : Tuple2<String, PPath> {
    constructor(tuple: Tuple2<String, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: createSignatures.Input): mb.spoofax.runtime.impl.sdf.Signatures? = run {

    requireOutput(mb.spoofax.runtime.pie.sdf3.GenerateStrategoSignatures::class.java, mb.spoofax.runtime.pie.sdf3.GenerateStrategoSignatures.id, mb.spoofax.runtime.pie.sdf3.GenerateStrategoSignatures.Input(input.langSpecExt, input.root))
  }
}

class style : TaskDef<style.Input, Styling?> {
  companion object {
    const val id = "style"
  }

  data class Input(val tokenStream: ArrayList<mb.spoofax.runtime.model.parse.Token>, val langSpecExt: String, val root: PPath) : Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>, String, PPath> {
    constructor(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>, String, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: style.Input): mb.spoofax.runtime.model.style.Styling? = run {
    val syntaxStyler = requireOutput(mb.spoofax.runtime.pie.esv.CompileStyler::class.java, mb.spoofax.runtime.pie.esv.CompileStyler.id, mb.spoofax.runtime.pie.esv.CompileStyler.Input(input.langSpecExt, input.root));
    if(syntaxStyler == null) return null
    requireOutput(mb.spoofax.runtime.pie.esv.Style::class.java, mb.spoofax.runtime.pie.esv.Style.id, mb.spoofax.runtime.pie.esv.Style.Input(input.tokenStream, syntaxStyler!!)) as mb.spoofax.runtime.model.style.Styling?
  }
}

class solveFile : TaskDef<solveFile.Input, ConstraintSolverSolution?> {
  companion object {
    const val id = "solveFile"
  }

  data class Input(val ast: org.spoofax.interpreter.terms.IStrategoTerm, val file: PPath, val project: PPath, val langSpecExt: String, val root: PPath) : Tuple5<org.spoofax.interpreter.terms.IStrategoTerm, PPath, PPath, String, PPath> {
    constructor(tuple: Tuple5<org.spoofax.interpreter.terms.IStrategoTerm, PPath, PPath, String, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: solveFile.Input): mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = run {
    val globalConstraints = requireOutput(mb.spoofax.runtime.pie.nabl2.CGenGlobal::class.java, mb.spoofax.runtime.pie.nabl2.CGenGlobal.id, mb.spoofax.runtime.pie.nabl2.CGenGlobal.Input(input.langSpecExt, input.root));
    if(globalConstraints == null) return null;
    val globalSolution = requireOutput(mb.spoofax.runtime.pie.nabl2.SolveGlobal::class.java, mb.spoofax.runtime.pie.nabl2.SolveGlobal.id, globalConstraints!!);
    if(globalSolution == null) return null;
    val documentConstraints = requireOutput(mb.spoofax.runtime.pie.nabl2.CGenDocument::class.java, mb.spoofax.runtime.pie.nabl2.CGenDocument.id, mb.spoofax.runtime.pie.nabl2.CGenDocument.Input(globalConstraints!!, input.ast, input.file, input.langSpecExt, input.root));
    if(documentConstraints == null) return null;
    val documentSolution = requireOutput(mb.spoofax.runtime.pie.nabl2.SolveDocument::class.java, mb.spoofax.runtime.pie.nabl2.SolveDocument.id, mb.spoofax.runtime.pie.nabl2.SolveDocument.Input(documentConstraints!!, globalConstraints!!, globalSolution!!));
    if(documentSolution == null) return null;
    val solution = requireOutput(mb.spoofax.runtime.pie.nabl2.SolveFinal::class.java, mb.spoofax.runtime.pie.nabl2.SolveFinal.id, mb.spoofax.runtime.pie.nabl2.SolveFinal.Input(list(documentSolution!!), globalSolution!!, input.project))
    solution
  }
}

class processFileWithSpxCore : TaskDef<processFileWithSpxCore.Input, processFileWithSpxCore.Output> {
  companion object {
    const val id = "processFileWithSpxCore"
  }

  data class Input(val file: PPath, val project: PPath, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple3<PPath, PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<PPath, PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?) : Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?> {
    constructor(tuple: Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processFileWithSpxCore.Input): processFileWithSpxCore.Output = run {
    if(!mb.spoofax.runtime.pie.shouldProcessFile(input.file)) run {
      return output(requireOutput(emptySpxCoreFile::class.java, emptySpxCoreFile.id, input.file))
    };
    if(!requireOutput(Exists::class.java, Exists.id, input.file)) run {
      return output(requireOutput(emptySpxCoreFile::class.java, emptySpxCoreFile.id, input.file))
    };
    val config = requireOutput(spxCoreConfigForPath::class.java, spxCoreConfigForPath.id, spxCoreConfigForPath.Input(input.workspace, input.file))
    output(if(config != null) run {
      val text = requireOutput(Read::class.java, Read.id, input.file)!!;
      val (tokens, messages, styling) = requireOutput(processStringWithSpxCore::class.java, processStringWithSpxCore.id, processStringWithSpxCore.Input(text, input.file, config!!));
      tuple(input.file, tokens, messages, styling)
    } else run {
      requireOutput(emptySpxCoreFile::class.java, emptySpxCoreFile.id, input.file)
    })
  }
}

class emptySpxCoreFile : TaskDef<PPath, emptySpxCoreFile.Output> {
  companion object {
    const val id = "emptySpxCoreFile"
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?) : Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?> {
    constructor(tuple: Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): emptySpxCoreFile.Output = run {
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null;
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list();
    val emptyStyling: mb.spoofax.runtime.model.style.Styling? = null
    output(tuple(input, emptyTokens, emptyMessages, emptyStyling))
  }
}

class processStringWithSpxCore : TaskDef<processStringWithSpxCore.Input, processStringWithSpxCore.Output> {
  companion object {
    const val id = "processStringWithSpxCore"
  }

  data class Input(val text: String, val file: PPath, val config: mb.spoofax.runtime.impl.cfg.SpxCoreConfig) : Tuple3<String, PPath, mb.spoofax.runtime.impl.cfg.SpxCoreConfig> {
    constructor(tuple: Tuple3<String, PPath, mb.spoofax.runtime.impl.cfg.SpxCoreConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?) : Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processStringWithSpxCore.Input): processStringWithSpxCore.Output = run {
    val (ast, tokens, messages, _) = requireOutput(mb.spoofax.runtime.pie.legacy.CoreParse::class.java, mb.spoofax.runtime.pie.legacy.CoreParse.id, mb.spoofax.runtime.pie.legacy.CoreParse.Input(input.config, input.text, input.file));
    val styling: mb.spoofax.runtime.model.style.Styling? = if(ast == null || tokens == null) null else requireOutput(mb.spoofax.runtime.pie.legacy.CoreStyle::class.java, mb.spoofax.runtime.pie.legacy.CoreStyle.id, mb.spoofax.runtime.pie.legacy.CoreStyle.Input(input.config, tokens!!, ast!!)) as mb.spoofax.runtime.model.style.Styling?
    output(tuple(tokens, messages, styling))
  }
}


class PieBuilderModule_spoofax : Module {
  override fun configure(binder: Binder) {
    val taskDefsBinder = binder.taskDefsBinder()

    binder.bindTaskDef<processStringWithSpxCore>(taskDefsBinder, "processStringWithSpxCore")
    binder.bindTaskDef<emptySpxCoreFile>(taskDefsBinder, "emptySpxCoreFile")
    binder.bindTaskDef<processFileWithSpxCore>(taskDefsBinder, "processFileWithSpxCore")
    binder.bindTaskDef<solveFile>(taskDefsBinder, "solveFile")
    binder.bindTaskDef<style>(taskDefsBinder, "style")
    binder.bindTaskDef<createSignatures>(taskDefsBinder, "createSignatures")
    binder.bindTaskDef<emptyParse>(taskDefsBinder, "emptyParse")
    binder.bindTaskDef<parse>(taskDefsBinder, "parse")
    binder.bindTaskDef<emptyResult>(taskDefsBinder, "emptyResult")
    binder.bindTaskDef<processString>(taskDefsBinder, "processString")
    binder.bindTaskDef<emptyFileResult>(taskDefsBinder, "emptyFileResult")
    binder.bindTaskDef<processFile>(taskDefsBinder, "processFile")
    binder.bindTaskDef<processEditor>(taskDefsBinder, "processEditor")
    binder.bindTaskDef<processLangSpecInProject>(taskDefsBinder, "processLangSpecInProject")
    binder.bindTaskDef<processProject>(taskDefsBinder, "processProject")
    binder.bindTaskDef<processWorkspace>(taskDefsBinder, "processWorkspace")
    binder.bindTaskDef<createWorkspaceConfig>(taskDefsBinder, "createWorkspaceConfig")
    binder.bindTaskDef<spxCoreConfigForPath>(taskDefsBinder, "spxCoreConfigForPath")
    binder.bindTaskDef<langSpecConfigForPath>(taskDefsBinder, "langSpecConfigForPath")
    binder.bindTaskDef<toMessage>(taskDefsBinder, "toMessage")
  }
}
