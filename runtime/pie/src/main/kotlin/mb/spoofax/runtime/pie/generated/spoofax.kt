@file:Suppress("warnings")

package mb.spoofax.runtime.pie.generated

import com.google.inject.Binder
import com.google.inject.Module
import mb.pie.runtime.builtin.path.*
import mb.pie.runtime.builtin.util.*
import mb.pie.runtime.core.*
import mb.vfs.path.*

class toMessage : Func<mb.spoofax.runtime.model.message.PathMsg, mb.spoofax.runtime.model.message.Msg> {
  companion object {
    val id = "toMessage"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: mb.spoofax.runtime.model.message.PathMsg): mb.spoofax.runtime.model.message.Msg {
    return input
  }
}

class langSpecConfigForPath : Func<langSpecConfigForPath.Input, mb.spoofax.runtime.impl.cfg.LangSpecConfig?> {
  companion object {
    val id = "langSpecConfigForPath"
  }

  data class Input(val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig, val path: PPath) : Tuple2<mb.spoofax.runtime.impl.cfg.WorkspaceConfig, PPath> {
    constructor(tuple: Tuple2<mb.spoofax.runtime.impl.cfg.WorkspaceConfig, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: langSpecConfigForPath.Input): mb.spoofax.runtime.impl.cfg.LangSpecConfig? {
    val extension = input.path.extension()
    if(extension == null) return null
    return input.workspace.langSpecConfigForExt(extension!!)
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
  override fun ExecContext.exec(input: spxCoreConfigForPath.Input): mb.spoofax.runtime.impl.cfg.SpxCoreConfig? {
    val extension = input.path.extension()
    if(extension == null) return null
    return input.workspace.spxCoreConfigForExt(extension!!)
  }
}

class createWorkspaceConfig : Func<PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig?> {
  companion object {
    val id = "createWorkspaceConfig"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): mb.spoofax.runtime.impl.cfg.WorkspaceConfig? {
    val cfgLang = mb.spoofax.runtime.impl.cfg.ImmutableSpxCoreConfig.of(PPathImpl(java.nio.file.FileSystems.getDefault().getPath("/Users/gohla/metaborg/repo/pie/spoofax-pie/lang/cfg/langspec")), false, list("cfg"))
    val workspaceFile = input.resolve("root/workspace.cfg")
    if(!requireOutput(Exists::class, Exists.Companion.id, workspaceFile)) return null
    val text = requireOutput(Read::class, Read.Companion.id, workspaceFile)!!
    val workspaceConfig = requireOutput(mb.spoofax.runtime.pie.builder.GenerateWorkspaceConfig::class, mb.spoofax.runtime.pie.builder.GenerateWorkspaceConfig.Companion.id, mb.spoofax.runtime.pie.builder.GenerateWorkspaceConfig.Input(text, workspaceFile, input, cfgLang))
    return workspaceConfig
  }
}

class processWorkspace : Func<PPath, ArrayList<Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>?>> {
  companion object {
    val id = "processWorkspace"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): ArrayList<Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>?> {
    return requireOutput(ListContents::class, ListContents.Companion.id, ListContents.Input(input, PPaths.regexPathMatcher("^[^.].+\$"))).map { project -> requireOutput(processProject::class, processProject.Companion.id, processProject.Input(project, input)) }.toCollection(ArrayList<Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>?>())
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
  override fun ExecContext.exec(input: processProject.Input): processProject.Output? {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.Companion.id, input.root)
    val noResults: Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>? = null
    if(workspaceConfig == null) return output(noResults)
    val workspace = workspaceConfig!!
    val langSpecResults = workspace.langSpecConfigs().map { langSpec -> requireOutput(processLangSpecInProject::class, processLangSpecInProject.Companion.id, processLangSpecInProject.Input(input.project, langSpec, workspace)) }.toCollection(ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>())
    val spxCoreResults = requireOutput(WalkContents::class, WalkContents.Companion.id, WalkContents.Input(input.project, PPaths.extensionsPathWalker(workspace.spxCoreExtensions()))).map { file -> requireOutput(processFileWithSpxCore::class, processFileWithSpxCore.Companion.id, processFileWithSpxCore.Input(file, input.project, workspace)) }.toCollection(ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>())
    return output(tuple(langSpecResults, spxCoreResults))
  }
}

class processLangSpecInProject : Func<processLangSpecInProject.Input, processLangSpecInProject.Output> {
  companion object {
    val id = "processLangSpecInProject"
  }

  data class Input(val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple3<PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, val _2: ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) : Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>> {
    constructor(tuple: Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>) : this(tuple.component1(), tuple.component2())
  }

  private fun output(tuple: Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processLangSpecInProject.Input): processLangSpecInProject.Output {
    val results = requireOutput(WalkContents::class, WalkContents.Companion.id, WalkContents.Input(input.project, PPaths.extensionsPathWalker(input.langSpec.extensions()))).map { file -> requireOutput(processFileWithLangSpecConfig::class, processFileWithLangSpecConfig.Companion.id, processFileWithLangSpecConfig.Input(file, input.project, input.workspace)) }.toCollection(ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>())
    val partialSolutions = mb.spoofax.runtime.pie.builder.filterNullPartialSolutions(results.map { result -> mb.spoofax.runtime.pie.builder.extractPartialSolution(result) }.toCollection(ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>()))
    val solutions = if(input.langSpec.natsRootScopePerFile()) partialSolutions.map { partialSolution -> requireOutput(solve::class, solve.Companion.id, solve.Input(list(partialSolution), input.project, input.langSpec, input.workspace)) }.toCollection(ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>()) else list(requireOutput(solve::class, solve.Companion.id, solve.Input(partialSolutions, input.project, input.langSpec, input.workspace)))
    return output(tuple(results, solutions))
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
  override fun ExecContext.exec(input: processEditor.Input): processEditor.Output? {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.Companion.id, input.root)
    if(workspaceConfig == null) return null
    val workspace = workspaceConfig!!
    val extension = input.file.extension()
    if(extension == null) return null
    val langSpecConfig = workspace.langSpecConfigForExt(extension!!)
    if(langSpecConfig != null) {
      val langSpec = langSpecConfig!!
      val (tokens, messages, styling, partialSolution) = requireOutput(processStringWithLangSpecConfig::class, processStringWithLangSpecConfig.Companion.id, processStringWithLangSpecConfig.Input(input.text, input.file, input.project, langSpec, workspace))
      val otherPartialSolutions = requireOutput(getOtherPartialSolutions::class, getOtherPartialSolutions.Companion.id, getOtherPartialSolutions.Input(input.file, input.project, langSpec, workspace))
      val partialSolutions = mb.spoofax.runtime.pie.builder.filterNullPartialSolutions(list(partialSolution) + otherPartialSolutions)
      val solution: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = if(partialSolution == null) null else requireOutput(solve::class, solve.Companion.id, solve.Input(partialSolutions, input.project, langSpec, workspace))
      return output(tuple(tokens, messages, styling, partialSolution, solution))
    }
    val spxCoreConfig = workspace.spxCoreConfigForExt(extension!!)
    if(spxCoreConfig != null) {
      val (tokens, messages, styling) = requireOutput(processStringWithSpxCore::class, processStringWithSpxCore.Companion.id, processStringWithSpxCore.Input(input.text, input.file, spxCoreConfig!!))
      val noPartialSolution: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? = null
      val noSolution: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = null
      return output(tuple(tokens, messages, styling, noPartialSolution, noSolution) as Tuple5<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>?)
    }
    return null
  }
}

class getOtherPartialSolutions : Func<getOtherPartialSolutions.Input, ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>> {
  companion object {
    val id = "getOtherPartialSolutions"
  }

  data class Input(val fileToIgnore: PPath, val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple4<PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: getOtherPartialSolutions.Input): ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    val projectResults = requireOutput(WalkContents::class, WalkContents.Companion.id, WalkContents.Input(input.project, PPaths.extensionsPathWalker(input.langSpec.extensions()))).map { file -> requireOutput(processFileWithLangSpecConfig::class, processFileWithLangSpecConfig.Companion.id, processFileWithLangSpecConfig.Input(file, input.project, input.workspace)) }.toCollection(ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>())
    val partialSolutions = projectResults.map { result -> mb.spoofax.runtime.pie.builder.extractOrRemovePartialSolution(input.fileToIgnore, result) }.toCollection(ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>())
    return partialSolutions
  }
}

class processFileWithLangSpecConfig : Func<processFileWithLangSpecConfig.Input, processFileWithLangSpecConfig.Output> {
  companion object {
    val id = "processFileWithLangSpecConfig"
  }

  data class Input(val file: PPath, val project: PPath, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple3<PPath, PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<PPath, PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?, val _5: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?) : Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    constructor(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processFileWithLangSpecConfig.Input): processFileWithLangSpecConfig.Output {
    if(!requireOutput(Exists::class, Exists.Companion.id, input.file)) {
      return output(requireOutput(emptyResult::class, emptyResult.Companion.id, input.file))
    }
    val langSpec = requireOutput(langSpecConfigForPath::class, langSpecConfigForPath.Companion.id, langSpecConfigForPath.Input(input.workspace, input.file))
    if(langSpec != null) {
      val text = requireOutput(Read::class, Read.Companion.id, input.file)!!
      val (tokens, messages, styling, partialSolution) = requireOutput(processStringWithLangSpecConfig::class, processStringWithLangSpecConfig.Companion.id, processStringWithLangSpecConfig.Input(text, input.file, input.project, langSpec!!, input.workspace))
      return output(tuple(input.file, tokens, messages, styling, partialSolution))
    } else {
      return output(requireOutput(emptyResult::class, emptyResult.Companion.id, input.file))
    }
  }
}

class emptyResult : Func<PPath, emptyResult.Output> {
  companion object {
    val id = "emptyResult"
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?, val _5: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?) : Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    constructor(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): emptyResult.Output {
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list()
    val emptyStyling: mb.spoofax.runtime.model.style.Styling? = null
    val emptyPartialSolution: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? = null
    return output(tuple(input, emptyTokens, emptyMessages, emptyStyling, emptyPartialSolution))
  }
}

class processStringWithLangSpecConfig : Func<processStringWithLangSpecConfig.Input, processStringWithLangSpecConfig.Output> {
  companion object {
    val id = "processStringWithLangSpecConfig"
  }

  data class Input(val text: String, val file: PPath, val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple5<String, PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple5<String, PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?, val _4: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?) : Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    constructor(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: processStringWithLangSpecConfig.Input): processStringWithLangSpecConfig.Output {
    val (ast, tokenStream, messages) = requireOutput(parse::class, parse.Companion.id, parse.Input(input.text, input.file, input.project, input.langSpec, input.workspace))
    val styling: mb.spoofax.runtime.model.style.Styling? = if(tokenStream == null) null else requireOutput(style::class, style.Companion.id, style.Input(tokenStream!!, input.langSpec, input.workspace))
    val partialSolution: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? = if(ast == null) null else requireOutput(partialSolve::class, partialSolve.Companion.id, partialSolve.Input(ast!!, input.file, input.langSpec, input.workspace))
    return output(tuple(tokenStream, messages, styling, partialSolution))
  }
}

class parse : Func<parse.Input, parse.Output> {
  companion object {
    val id = "parse"
  }

  data class Input(val text: String, val file: PPath, val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple5<String, PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple5<String, PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) = Output(tuple)

  override val id = Companion.id
  override fun ExecContext.exec(input: parse.Input): parse.Output {
    val sdfLang = input.workspace.spxCoreConfigForExt("sdf3")
    if(sdfLang == null) return output(requireOutput(emptyParse::class, emptyParse.Companion.id, None.instance))
    val files = input.langSpec.syntaxParseFiles()
    val mainFile = input.langSpec.syntaxParseMainFile()
    val startSymbol = input.langSpec.syntaxParseStartSymbolId()
    if(mainFile == null || startSymbol == null) return output(requireOutput(emptyParse::class, emptyParse.Companion.id, None.instance))
    val parseTable = requireOutput(mb.spoofax.runtime.pie.builder.GenerateTable::class, mb.spoofax.runtime.pie.builder.GenerateTable.Companion.id, mb.spoofax.runtime.pie.builder.GenerateTable.Input(sdfLang!!, input.project, files, mainFile!!))
    if(parseTable == null) return output(requireOutput(emptyParse::class, emptyParse.Companion.id, None.instance))
    return output(requireOutput(mb.spoofax.runtime.pie.builder.Parse::class, mb.spoofax.runtime.pie.builder.Parse.Companion.id, mb.spoofax.runtime.pie.builder.Parse.Input(input.text, startSymbol!!, parseTable!!, input.file)))
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
  override fun ExecContext.exec(input: None): emptyParse.Output {
    val emptyAst: org.spoofax.interpreter.terms.IStrategoTerm? = null
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list()
    return output(tuple(emptyAst, emptyTokens, emptyMessages))
  }
}

class createSignatures : Func<createSignatures.Input, mb.spoofax.runtime.impl.sdf.Signatures?> {
  companion object {
    val id = "createSignatures"
  }

  data class Input(val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple2<mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple2<mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: createSignatures.Input): mb.spoofax.runtime.impl.sdf.Signatures? {
    val sdfLang = input.workspace.spxCoreConfigForExt("sdf3")
    if(sdfLang == null) return null
    val files = input.langSpec.syntaxSignatureFiles()
    return requireOutput(mb.spoofax.runtime.pie.builder.GenerateSignatures::class, mb.spoofax.runtime.pie.builder.GenerateSignatures.Companion.id, mb.spoofax.runtime.pie.builder.GenerateSignatures.Input(sdfLang!!, input.langSpec.dir(), files)) as mb.spoofax.runtime.impl.sdf.Signatures?
  }
}

class style : Func<style.Input, mb.spoofax.runtime.model.style.Styling?> {
  companion object {
    val id = "style"
  }

  data class Input(val tokenStream: ArrayList<mb.spoofax.runtime.model.parse.Token>, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: style.Input): mb.spoofax.runtime.model.style.Styling? {
    val esvLang = input.workspace.spxCoreConfigForExt("esv")
    if(esvLang == null) return null
    val mainFile = input.langSpec.syntaxStyleFile()
    if(mainFile == null) return null
    val syntaxStyler = requireOutput(mb.spoofax.runtime.pie.builder.GenerateStylerRules::class, mb.spoofax.runtime.pie.builder.GenerateStylerRules.Companion.id, mb.spoofax.runtime.pie.builder.GenerateStylerRules.Input(esvLang!!, mainFile!!, list()))
    if(syntaxStyler == null) return null
    return requireOutput(mb.spoofax.runtime.pie.builder.Style::class, mb.spoofax.runtime.pie.builder.Style.Companion.id, mb.spoofax.runtime.pie.builder.Style.Input(input.tokenStream, syntaxStyler!!)) as mb.spoofax.runtime.model.style.Styling?
  }
}

class createConstraintGenerator : Func<createConstraintGenerator.Input, mb.spoofax.runtime.impl.nabl.ConstraintGenerator?> {
  companion object {
    val id = "createConstraintGenerator"
  }

  data class Input(val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple2<mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple2<mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: createConstraintGenerator.Input): mb.spoofax.runtime.impl.nabl.ConstraintGenerator? {
    val nabl2Lang = input.workspace.spxCoreConfigForExt("nabl2")
    if(nabl2Lang == null) return null
    val nabl2Files = input.langSpec.natsNaBL2Files()
    val strategoConfig = input.langSpec.natsStrategoConfig()
    if(strategoConfig == null) return null
    val strategyStrategyId = input.langSpec.natsStrategoStrategyId()
    if(strategyStrategyId == null) return null
    val signatures = requireOutput(createSignatures::class, createSignatures.Companion.id, createSignatures.Input(input.langSpec, input.workspace))
    if(signatures == null) return null
    return requireOutput(mb.spoofax.runtime.pie.builder.NaBL2GenerateConstraintGenerator::class, mb.spoofax.runtime.pie.builder.NaBL2GenerateConstraintGenerator.Companion.id, mb.spoofax.runtime.pie.builder.NaBL2GenerateConstraintGenerator.Input(nabl2Lang!!, input.langSpec.dir(), nabl2Files, strategoConfig!!, strategyStrategyId!!, signatures!!)) as mb.spoofax.runtime.impl.nabl.ConstraintGenerator?
  }
}

class partialSolve : Func<partialSolve.Input, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
  companion object {
    val id = "partialSolve"
  }

  data class Input(val ast: org.spoofax.interpreter.terms.IStrategoTerm, val file: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple4<org.spoofax.interpreter.terms.IStrategoTerm, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<org.spoofax.interpreter.terms.IStrategoTerm, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: partialSolve.Input): org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? {
    val generator = requireOutput(createConstraintGenerator::class, createConstraintGenerator.Companion.id, createConstraintGenerator.Input(input.langSpec, input.workspace))
    return null
    if(generator == null) return null
    val initialResult = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2InitialResult::class, mb.spoofax.runtime.pie.builder.NaBL2InitialResult.Companion.id, generator!!)
    val unitResult = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2UnitResult::class, mb.spoofax.runtime.pie.builder.NaBL2UnitResult.Companion.id, mb.spoofax.runtime.pie.builder.NaBL2UnitResult.Input(generator!!, initialResult, input.ast, input.file))
    val partialSolution = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2PartialSolve::class, mb.spoofax.runtime.pie.builder.NaBL2PartialSolve.Companion.id, mb.spoofax.runtime.pie.builder.NaBL2PartialSolve.Input(initialResult, unitResult, input.file))
    return partialSolution as org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?
  }
}

class solve : Func<solve.Input, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?> {
  companion object {
    val id = "solve"
  }

  data class Input(val partialSolutions: ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution>, val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple4<ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution>, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution>, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: solve.Input): mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? {
    val generator = requireOutput(createConstraintGenerator::class, createConstraintGenerator.Companion.id, createConstraintGenerator.Input(input.langSpec, input.workspace))
    return null
    if(generator == null) return null
    val initialResult = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2InitialResult::class, mb.spoofax.runtime.pie.builder.NaBL2InitialResult.Companion.id, generator!!)
    val solution = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2Solve::class, mb.spoofax.runtime.pie.builder.NaBL2Solve.Companion.id, mb.spoofax.runtime.pie.builder.NaBL2Solve.Input(initialResult, input.partialSolutions, input.project))
    return solution as mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?
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
  override fun ExecContext.exec(input: processFileWithSpxCore.Input): processFileWithSpxCore.Output {
    if(!requireOutput(Exists::class, Exists.Companion.id, input.file)) {
      return output(requireOutput(emptySpxCoreFile::class, emptySpxCoreFile.Companion.id, input.file))
    }
    val config = requireOutput(spxCoreConfigForPath::class, spxCoreConfigForPath.Companion.id, spxCoreConfigForPath.Input(input.workspace, input.file))
    if(config != null) {
      val text = requireOutput(Read::class, Read.Companion.id, input.file)!!
      val (tokens, messages, styling) = requireOutput(processStringWithSpxCore::class, processStringWithSpxCore.Companion.id, processStringWithSpxCore.Input(text, input.file, config!!))
      return output(tuple(input.file, tokens, messages, styling))
    } else {
      return output(requireOutput(emptySpxCoreFile::class, emptySpxCoreFile.Companion.id, input.file))
    }
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
  override fun ExecContext.exec(input: PPath): emptySpxCoreFile.Output {
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list()
    val emptyStyling: mb.spoofax.runtime.model.style.Styling? = null
    return output(tuple(input, emptyTokens, emptyMessages, emptyStyling))
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
  override fun ExecContext.exec(input: processStringWithSpxCore.Input): processStringWithSpxCore.Output {
    val (ast, tokens, messages, _) = requireOutput(mb.spoofax.runtime.pie.builder.core.CoreParse::class, mb.spoofax.runtime.pie.builder.core.CoreParse.Companion.id, mb.spoofax.runtime.pie.builder.core.CoreParse.Input(input.config, input.text, input.file))
    val styling: mb.spoofax.runtime.model.style.Styling? = if(ast == null || tokens == null) null else requireOutput(mb.spoofax.runtime.pie.builder.core.CoreStyle::class, mb.spoofax.runtime.pie.builder.core.CoreStyle.Companion.id, mb.spoofax.runtime.pie.builder.core.CoreStyle.Input(input.config, tokens!!, ast!!)) as mb.spoofax.runtime.model.style.Styling?
    return output(tuple(tokens, messages, styling))
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
    binder.bindFunc<processStringWithLangSpecConfig>(funcs, "processStringWithLangSpecConfig")
    binder.bindFunc<emptyResult>(funcs, "emptyResult")
    binder.bindFunc<processFileWithLangSpecConfig>(funcs, "processFileWithLangSpecConfig")
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
