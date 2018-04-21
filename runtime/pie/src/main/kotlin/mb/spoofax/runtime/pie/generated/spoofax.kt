@file:Suppress("warnings")

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

class toMessage : Func<mb.spoofax.runtime.model.message.PathMsg, mb.spoofax.runtime.model.message.Msg> {
  companion object {
    val id = "toMessage"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: mb.spoofax.runtime.model.message.PathMsg): mb.spoofax.runtime.model.message.Msg = run {

    input
  }
}

class langSpecConfigForPath : Func<langSpecConfigForPath.Input, mb.spoofax.runtime.impl.cfg.LangSpecConfig?> {
  companion object {
    val id = "langSpecConfigForPath"
  }

  data class Input(val path: PPath, val root: PPath) : Tuple2<PPath, PPath> {
    constructor(tuple: Tuple2<PPath, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: langSpecConfigForPath.Input): mb.spoofax.runtime.impl.cfg.LangSpecConfig? = run {
    val workspace = requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.Companion.id, input.root);
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

class spxCoreConfigForPath : Func<spxCoreConfigForPath.Input, mb.spoofax.runtime.impl.cfg.SpxCoreConfig?> {
  companion object {
    val id = "spxCoreConfigForPath"
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

class createWorkspaceConfig : Func<PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig?> {
  companion object {
    val id = "createWorkspaceConfig"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): mb.spoofax.runtime.impl.cfg.WorkspaceConfig? = run {
    val cfgLang = mb.spoofax.runtime.impl.cfg.ImmutableSpxCoreConfig.of(PPathImpl(java.nio.file.FileSystems.getDefault().getPath("/Users/gohla/metaborg/repo/pie/spoofax-pie/lang/cfg/langspec")), false, list("cfg"));
    val workspaceFile = input.resolve("root/workspace.cfg");
    if(!requireOutput(Exists::class, Exists.Companion.id, workspaceFile)) return null;
    val text = requireOutput(Read::class, Read.Companion.id, workspaceFile)!!;
    val workspaceConfig = requireOutput(mb.spoofax.runtime.pie.builder.GenerateWorkspaceConfig::class, mb.spoofax.runtime.pie.builder.GenerateWorkspaceConfig.Companion.id, mb.spoofax.runtime.pie.builder.GenerateWorkspaceConfig.Input(text, workspaceFile, input, cfgLang))
    workspaceConfig
  }
}

class processWorkspace : Func<PPath, ArrayList<Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>?>> {
  companion object {
    val id = "processWorkspace"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): ArrayList<Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>?> = run {

    requireOutput(ListContents::class, ListContents.Companion.id, ListContents.Input(input, PPaths.regexPathMatcher("^[^.]((?!src-gen).)*\$"))).map { project -> requireOutput(processProject::class, processProject.Companion.id, processProject.Input(project, input)) }.toCollection(ArrayList<Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>?>())
  }
}

class processProject : Func<processProject.Input, processProject.Output?> {
  companion object {
    val id = "processProject"
  }

  data class Input(val project: PPath, val root: PPath) : Tuple2<PPath, PPath> {
    constructor(tuple: Tuple2<PPath, PPath>) : this(tuple.component1(), tuple.component2())
  }

  data class Output(val _1: ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, val _2: ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>) : Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>> {
    constructor(tuple: Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>) : this(tuple.component1(), tuple.component2())
  }

  private fun output(tuple: Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>?) = if(tuple == null) null else Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processProject.Input): processProject.Output? = run {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.Companion.id, input.root);
    val noResults: Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>? = null;
    if(workspaceConfig == null) return output(noResults);
    val workspace = workspaceConfig!!;
    val langSpecResults = workspace.langSpecConfigs().map { langSpec -> requireOutput(processLangSpecInProject::class, processLangSpecInProject.Companion.id, processLangSpecInProject.Input(input.project, langSpec, input.root)) }.toCollection(ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>());
    val spxCoreResults = requireOutput(WalkContents::class, WalkContents.Companion.id, WalkContents.Input(input.project, PPaths.extensionsPathWalker(workspace.spxCoreExtensions()))).map { file -> requireOutput(processFileWithSpxCore::class, processFileWithSpxCore.Companion.id, processFileWithSpxCore.Input(file, input.project, workspace)) }.toCollection(ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>())
    output(tuple(langSpecResults, spxCoreResults))
  }
}

class processLangSpecInProject : Func<processLangSpecInProject.Input, processLangSpecInProject.Output> {
  companion object {
    val id = "processLangSpecInProject"
  }

  data class Input(val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val root: PPath) : Tuple3<PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, PPath> {
    constructor(tuple: Tuple3<PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, val _2: ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) : Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>> {
    constructor(tuple: Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>) : this(tuple.component1(), tuple.component2())
  }

  private fun output(tuple: Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processLangSpecInProject.Input): processLangSpecInProject.Output = run {
    val results = requireOutput(WalkContents::class, WalkContents.Companion.id, WalkContents.Input(input.project, PPaths.extensionsPathWalker(input.langSpec.extensions()))).map { file -> requireOutput(processFile::class, processFile.Companion.id, processFile.Input(file, input.project, input.root)) }.toCollection(ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>());
    val partialSolutions = mb.spoofax.runtime.pie.builder.filterNullPartialSolutions(results.map { result -> mb.spoofax.runtime.pie.builder.extractPartialSolution(result) }.toCollection(ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>()));
    val solutions = if(input.langSpec.natsRootScopePerFile()) partialSolutions.map { partialSolution -> requireOutput(solve::class, solve.Companion.id, solve.Input(list(partialSolution), input.project, input.langSpec.firstExtension(), input.root)) }.toCollection(ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>()) else list(requireOutput(solve::class, solve.Companion.id, solve.Input(partialSolutions, input.project, input.langSpec.firstExtension(), input.root)))
    output(tuple(results, solutions))
  }
}

class processEditor : Func<processEditor.Input, processEditor.Output?> {
  companion object {
    val id = "processEditor"
  }

  data class Input(val text: String, val file: PPath, val project: PPath, val root: PPath) : Tuple4<String, PPath, PPath, PPath> {
    constructor(tuple: Tuple4<String, PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?, val _4: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?, val _5: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?) : Tuple5<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple5<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>?) = if(tuple == null) null else Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processEditor.Input): processEditor.Output? = run {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.Companion.id, input.root);
    if(workspaceConfig == null) return null;
    val workspace = workspaceConfig!!;
    val extension = input.file.extension();
    if(extension == null) return null;
    val langSpecConfig = workspace.langSpecConfigForExt(extension!!);
    if(langSpecConfig != null) run {
      val langSpec = langSpecConfig!!;
      val (tokens, messages, styling, partialSolution) = requireOutput(processString::class, processString.Companion.id, processString.Input(input.text, input.file, input.project, input.root));
      val otherPartialSolutions = requireOutput(getOtherPartialSolutions::class, getOtherPartialSolutions.Companion.id, getOtherPartialSolutions.Input(input.file, input.project, langSpec, input.root));
      val partialSolutions = mb.spoofax.runtime.pie.builder.filterNullPartialSolutions(list(partialSolution) + otherPartialSolutions);
      val solution: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = if(partialSolution == null) null else requireOutput(solve::class, solve.Companion.id, solve.Input(partialSolutions, input.project, extension!!, input.root));
      return output(tuple(tokens, messages, styling, partialSolution, solution))
    };
    val spxCoreConfig = workspace.spxCoreConfigForExt(extension!!);
    if(spxCoreConfig != null) run {
      val (tokens, messages, styling) = requireOutput(processStringWithSpxCore::class, processStringWithSpxCore.Companion.id, processStringWithSpxCore.Input(input.text, input.file, spxCoreConfig!!));
      val noPartialSolution: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? = null;
      val noSolution: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = null;
      return output(tuple(tokens, messages, styling, noPartialSolution, noSolution) as Tuple5<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>?)
    }
    null
  }
}

class getOtherPartialSolutions : Func<getOtherPartialSolutions.Input, ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>> {
  companion object {
    val id = "getOtherPartialSolutions"
  }

  data class Input(val fileToIgnore: PPath, val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val root: PPath) : Tuple4<PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, PPath> {
    constructor(tuple: Tuple4<PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: getOtherPartialSolutions.Input): ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> = run {
    val projectResults = requireOutput(WalkContents::class, WalkContents.Companion.id, WalkContents.Input(input.project, PPaths.extensionsPathWalker(input.langSpec.extensions()))).map { file -> requireOutput(processFile::class, processFile.Companion.id, processFile.Input(file, input.project, input.root)) }.toCollection(ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>());
    val partialSolutions = projectResults.map { result -> mb.spoofax.runtime.pie.builder.extractOrRemovePartialSolution(input.fileToIgnore, result) }.toCollection(ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>())
    partialSolutions
  }
}

class processFile : Func<processFile.Input, processFile.Output> {
  companion object {
    val id = "processFile"
  }

  data class Input(val file: PPath, val project: PPath, val root: PPath) : Tuple3<PPath, PPath, PPath> {
    constructor(tuple: Tuple3<PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?, val _5: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?) : Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    constructor(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processFile.Input): processFile.Output = run {
    if(!mb.spoofax.runtime.pie.builder.shouldProcessFile(input.file)) run {
      return output(requireOutput(emptyFileResult::class, emptyFileResult.Companion.id, input.file))
    };
    if(!requireOutput(Exists::class, Exists.Companion.id, input.file)) run {
      return output(requireOutput(emptyFileResult::class, emptyFileResult.Companion.id, input.file))
    };
    val langSpec = requireOutput(langSpecConfigForPath::class, langSpecConfigForPath.Companion.id, langSpecConfigForPath.Input(input.file, input.root))
    output(if(langSpec != null) run {
      val text = requireOutput(Read::class, Read.Companion.id, input.file)!!;
      val (tokens, messages, styling, partialSolution) = requireOutput(processString::class, processString.Companion.id, processString.Input(text, input.file, input.project, input.root));
      tuple(input.file, tokens, messages, styling, partialSolution)
    } else run {
      requireOutput(emptyFileResult::class, emptyFileResult.Companion.id, input.file)
    })
  }
}

class emptyFileResult : Func<PPath, emptyFileResult.Output> {
  companion object {
    val id = "emptyFileResult"
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?, val _5: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?) : Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    constructor(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): emptyFileResult.Output = run {
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null;
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list();
    val emptyStyling: mb.spoofax.runtime.model.style.Styling? = null;
    val emptyPartialSolution: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? = null
    output(tuple(input, emptyTokens, emptyMessages, emptyStyling, emptyPartialSolution))
  }
}

class processString : Func<processString.Input, processString.Output> {
  companion object {
    val id = "processString"
  }

  data class Input(val text: String, val file: PPath, val project: PPath, val root: PPath) : Tuple4<String, PPath, PPath, PPath> {
    constructor(tuple: Tuple4<String, PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?, val _4: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?) : Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    constructor(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processString.Input): processString.Output = run {
    val langSpecExt = input.file.extension()
    output(if(langSpecExt != null) run {
      val (ast, tokenStream, messages) = requireOutput(parse::class, parse.Companion.id, parse.Input(input.text, input.file, langSpecExt!!, input.root));
      val styling: mb.spoofax.runtime.model.style.Styling? = if(tokenStream == null) null else requireOutput(style::class, style.Companion.id, style.Input(tokenStream!!, langSpecExt!!, input.root));
      val partialSolution: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? = if(ast == null) null else requireOutput(partialSolve::class, partialSolve.Companion.id, partialSolve.Input(ast!!, input.file, langSpecExt!!, input.root));
      tuple(tokenStream, messages, styling, partialSolution)
    } else run {
      requireOutput(emptyResult::class, emptyResult.Companion.id, None.instance)
    })
  }
}

class emptyResult : Func<None, emptyResult.Output> {
  companion object {
    val id = "emptyResult"
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?, val _4: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?) : Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    constructor(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: None): emptyResult.Output = run {
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null;
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list();
    val emptyStyling: mb.spoofax.runtime.model.style.Styling? = null;
    val emptyPartialSolution: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? = null
    output(tuple(emptyTokens, emptyMessages, emptyStyling, emptyPartialSolution))
  }
}

class parse : Func<parse.Input, parse.Output> {
  companion object {
    val id = "parse"
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
    val parseTable = requireOutput(mb.spoofax.runtime.pie.builder.GenerateTable::class, mb.spoofax.runtime.pie.builder.GenerateTable.Companion.id, mb.spoofax.runtime.pie.builder.GenerateTable.Input(input.langSpecExt, input.root));
    if(parseTable == null) return output(requireOutput(emptyParse::class, emptyParse.Companion.id, None.instance))
    output(requireOutput(mb.spoofax.runtime.pie.builder.Parse::class, mb.spoofax.runtime.pie.builder.Parse.Companion.id, mb.spoofax.runtime.pie.builder.Parse.Input(input.text, parseTable!!, input.file, input.langSpecExt, input.root)))
  }
}

class emptyParse : Func<None, emptyParse.Output> {
  companion object {
    val id = "emptyParse"
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

class createSignatures : Func<createSignatures.Input, mb.spoofax.runtime.impl.sdf.Signatures?> {
  companion object {
    val id = "createSignatures"
  }

  data class Input(val langSpecExt: String, val root: PPath) : Tuple2<String, PPath> {
    constructor(tuple: Tuple2<String, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: createSignatures.Input): mb.spoofax.runtime.impl.sdf.Signatures? = run {

    requireOutput(mb.spoofax.runtime.pie.builder.GenerateSignatures::class, mb.spoofax.runtime.pie.builder.GenerateSignatures.Companion.id, mb.spoofax.runtime.pie.builder.GenerateSignatures.Input(input.langSpecExt, input.root))
  }
}

class style : Func<style.Input, mb.spoofax.runtime.model.style.Styling?> {
  companion object {
    val id = "style"
  }

  data class Input(val tokenStream: ArrayList<mb.spoofax.runtime.model.parse.Token>, val langSpecExt: String, val root: PPath) : Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>, String, PPath> {
    constructor(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>, String, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: style.Input): mb.spoofax.runtime.model.style.Styling? = run {
    val syntaxStyler = requireOutput(mb.spoofax.runtime.pie.builder.GenerateStylerRules::class, mb.spoofax.runtime.pie.builder.GenerateStylerRules.Companion.id, mb.spoofax.runtime.pie.builder.GenerateStylerRules.Input(input.langSpecExt, input.root));
    if(syntaxStyler == null) return null
    requireOutput(mb.spoofax.runtime.pie.builder.Style::class, mb.spoofax.runtime.pie.builder.Style.Companion.id, mb.spoofax.runtime.pie.builder.Style.Input(input.tokenStream, syntaxStyler!!)) as mb.spoofax.runtime.model.style.Styling?
  }
}

class createConstraintGenerator : Func<createConstraintGenerator.Input, mb.spoofax.runtime.impl.nabl.ConstraintGenerator?> {
  companion object {
    val id = "createConstraintGenerator"
  }

  data class Input(val langSpecExt: String, val root: PPath) : Tuple2<String, PPath> {
    constructor(tuple: Tuple2<String, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: createConstraintGenerator.Input): mb.spoofax.runtime.impl.nabl.ConstraintGenerator? = run {

    requireOutput(mb.spoofax.runtime.pie.builder.NaBL2GenerateConstraintGenerator::class, mb.spoofax.runtime.pie.builder.NaBL2GenerateConstraintGenerator.Companion.id, mb.spoofax.runtime.pie.builder.NaBL2GenerateConstraintGenerator.Input(input.langSpecExt, input.root))
  }
}

class partialSolve : Func<partialSolve.Input, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
  companion object {
    val id = "partialSolve"
  }

  data class Input(val ast: org.spoofax.interpreter.terms.IStrategoTerm, val file: PPath, val langSpecExt: String, val root: PPath) : Tuple4<org.spoofax.interpreter.terms.IStrategoTerm, PPath, String, PPath> {
    constructor(tuple: Tuple4<org.spoofax.interpreter.terms.IStrategoTerm, PPath, String, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: partialSolve.Input): org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? = run {
    val generator = requireOutput(createConstraintGenerator::class, createConstraintGenerator.Companion.id, createConstraintGenerator.Input(input.langSpecExt, input.root));
    return null;
    if(generator == null) return null;
    val initialResult = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2InitialResult::class, mb.spoofax.runtime.pie.builder.NaBL2InitialResult.Companion.id, generator!!);
    val unitResult = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2UnitResult::class, mb.spoofax.runtime.pie.builder.NaBL2UnitResult.Companion.id, mb.spoofax.runtime.pie.builder.NaBL2UnitResult.Input(generator!!, initialResult, input.ast, input.file));
    val partialSolution = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2PartialSolve::class, mb.spoofax.runtime.pie.builder.NaBL2PartialSolve.Companion.id, mb.spoofax.runtime.pie.builder.NaBL2PartialSolve.Input(initialResult, unitResult, input.file))
    partialSolution as org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?
  }
}

class solve : Func<solve.Input, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?> {
  companion object {
    val id = "solve"
  }

  data class Input(val partialSolutions: ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution>, val project: PPath, val langSpecExt: String, val root: PPath) : Tuple4<ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution>, PPath, String, PPath> {
    constructor(tuple: Tuple4<ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution>, PPath, String, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: solve.Input): mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = run {
    val generator = requireOutput(createConstraintGenerator::class, createConstraintGenerator.Companion.id, createConstraintGenerator.Input(input.langSpecExt, input.root));
    return null;
    if(generator == null) return null;
    val initialResult = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2InitialResult::class, mb.spoofax.runtime.pie.builder.NaBL2InitialResult.Companion.id, generator!!);
    val solution = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2Solve::class, mb.spoofax.runtime.pie.builder.NaBL2Solve.Companion.id, mb.spoofax.runtime.pie.builder.NaBL2Solve.Input(initialResult, input.partialSolutions, input.project))
    solution as mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?
  }
}

class processFileWithSpxCore : Func<processFileWithSpxCore.Input, processFileWithSpxCore.Output> {
  companion object {
    val id = "processFileWithSpxCore"
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
    if(!mb.spoofax.runtime.pie.builder.shouldProcessFile(input.file)) run {
      return output(requireOutput(emptySpxCoreFile::class, emptySpxCoreFile.Companion.id, input.file))
    };
    if(!requireOutput(Exists::class, Exists.Companion.id, input.file)) run {
      return output(requireOutput(emptySpxCoreFile::class, emptySpxCoreFile.Companion.id, input.file))
    };
    val config = requireOutput(spxCoreConfigForPath::class, spxCoreConfigForPath.Companion.id, spxCoreConfigForPath.Input(input.workspace, input.file))
    output(if(config != null) run {
      val text = requireOutput(Read::class, Read.Companion.id, input.file)!!;
      val (tokens, messages, styling) = requireOutput(processStringWithSpxCore::class, processStringWithSpxCore.Companion.id, processStringWithSpxCore.Input(text, input.file, config!!));
      tuple(input.file, tokens, messages, styling)
    } else run {
      requireOutput(emptySpxCoreFile::class, emptySpxCoreFile.Companion.id, input.file)
    })
  }
}

class emptySpxCoreFile : Func<PPath, emptySpxCoreFile.Output> {
  companion object {
    val id = "emptySpxCoreFile"
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

class processStringWithSpxCore : Func<processStringWithSpxCore.Input, processStringWithSpxCore.Output> {
  companion object {
    val id = "processStringWithSpxCore"
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
    val (ast, tokens, messages, _) = requireOutput(mb.spoofax.runtime.pie.builder.core.CoreParse::class, mb.spoofax.runtime.pie.builder.core.CoreParse.Companion.id, mb.spoofax.runtime.pie.builder.core.CoreParse.Input(input.config, input.text, input.file));
    val styling: mb.spoofax.runtime.model.style.Styling? = if(ast == null || tokens == null) null else requireOutput(mb.spoofax.runtime.pie.builder.core.CoreStyle::class, mb.spoofax.runtime.pie.builder.core.CoreStyle.Companion.id, mb.spoofax.runtime.pie.builder.core.CoreStyle.Input(input.config, tokens!!, ast!!)) as mb.spoofax.runtime.model.style.Styling?
    output(tuple(tokens, messages, styling))
  }
}


class PieBuilderModule_spoofax : Module {
  override fun configure(binder: Binder) {
    val funcs = binder.funcsMapBinder()

    binder.bindFunc<processStringWithSpxCore>(funcs, "processStringWithSpxCore")
    binder.bindFunc<emptySpxCoreFile>(funcs, "emptySpxCoreFile")
    binder.bindFunc<processFileWithSpxCore>(funcs, "processFileWithSpxCore")
    binder.bindFunc<solve>(funcs, "solve")
    binder.bindFunc<partialSolve>(funcs, "partialSolve")
    binder.bindFunc<createConstraintGenerator>(funcs, "createConstraintGenerator")
    binder.bindFunc<style>(funcs, "style")
    binder.bindFunc<createSignatures>(funcs, "createSignatures")
    binder.bindFunc<emptyParse>(funcs, "emptyParse")
    binder.bindFunc<parse>(funcs, "parse")
    binder.bindFunc<emptyResult>(funcs, "emptyResult")
    binder.bindFunc<processString>(funcs, "processString")
    binder.bindFunc<emptyFileResult>(funcs, "emptyFileResult")
    binder.bindFunc<processFile>(funcs, "processFile")
    binder.bindFunc<getOtherPartialSolutions>(funcs, "getOtherPartialSolutions")
    binder.bindFunc<processEditor>(funcs, "processEditor")
    binder.bindFunc<processLangSpecInProject>(funcs, "processLangSpecInProject")
    binder.bindFunc<processProject>(funcs, "processProject")
    binder.bindFunc<processWorkspace>(funcs, "processWorkspace")
    binder.bindFunc<createWorkspaceConfig>(funcs, "createWorkspaceConfig")
    binder.bindFunc<spxCoreConfigForPath>(funcs, "spxCoreConfigForPath")
    binder.bindFunc<langSpecConfigForPath>(funcs, "langSpecConfigForPath")
    binder.bindFunc<toMessage>(funcs, "toMessage")
  }
}
