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

class toMessage : Builder<mb.spoofax.runtime.model.message.PathMsg, mb.spoofax.runtime.model.message.Msg> {
  override val id = "toMessage"
  override fun BuildContext.build(input: mb.spoofax.runtime.model.message.PathMsg): mb.spoofax.runtime.model.message.Msg {
    return input
  }
}

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

class spxCoreConfigForPath : Builder<spxCoreConfigForPath.Input, mb.spoofax.runtime.impl.cfg.SpxCoreConfig?> {
  data class Input(val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig, val path: PPath) : Tuple2<mb.spoofax.runtime.impl.cfg.WorkspaceConfig, PPath> {
    constructor(tuple: Tuple2<mb.spoofax.runtime.impl.cfg.WorkspaceConfig, PPath>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "spxCoreConfigForPath"
  override fun BuildContext.build(input: spxCoreConfigForPath.Input): mb.spoofax.runtime.impl.cfg.SpxCoreConfig? {
    val extension = input.path.extension()
    if (extension == null) return null
    return input.workspace.spxCoreConfigForExt(extension!!)
  }
}

class createWorkspaceConfig : Builder<PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig?> {
  override val id = "createWorkspaceConfig"
  override fun BuildContext.build(input: PPath): mb.spoofax.runtime.impl.cfg.WorkspaceConfig? {
    val cfgLang = mb.spoofax.runtime.impl.cfg.ImmutableSpxCoreConfig.of(PPathImpl(java.nio.file.FileSystems.getDefault().getPath("/Users/gohla/metaborg/repo/pie/spoofax-pie/lang/cfg/langspec")), false, list("cfg"))
    val workspaceFile = input.resolve(PPathImpl(java.nio.file.FileSystems.getDefault().getPath("./root/workspace.cfg")))
    if (!requireOutput(Exists::class.java, workspaceFile)) return null
    val text = requireOutput(Read::class.java, workspaceFile)
    val workspaceConfig = requireOutput(mb.spoofax.runtime.pie.builder.GenerateWorkspaceConfig::class.java, mb.spoofax.runtime.pie.builder.GenerateWorkspaceConfig.Input(text, workspaceFile, input, cfgLang))
    return workspaceConfig
  }
}

class processWorkspace : Builder<PPath, ArrayList<Tuple3<PPath, ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>>> {
  override val id = "processWorkspace"
  override fun BuildContext.build(input: PPath): ArrayList<Tuple3<PPath, ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>> {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class.java, input)
    val noResults: ArrayList<Tuple3<PPath, ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>> = list()
    if (workspaceConfig == null) return noResults
    val workspace = workspaceConfig!!
    return requireOutput(ListContents::class.java, ListContents.Input(input, PPaths.regexPathMatcher("^[^.].+\$"))).map { project -> requireOutput(processProject::class.java, processProject.Input(project, workspace)) }.toCollection(ArrayList<Tuple3<PPath, ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>>())
  }
}

class processProject : Builder<processProject.Input, processProject.Output> {
  data class Input(val project: PPath, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple2<PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple2<PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2())
  }

  data class Output(val _1: PPath, val _2: ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, val _3: ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>) : Tuple3<PPath, ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>> {
    constructor(tuple: Tuple3<PPath, ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<PPath, ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>>) = Output(tuple)

  override val id = "processProject"
  override fun BuildContext.build(input: processProject.Input): processProject.Output {
    val langSpecResults = input.workspace.langSpecConfigs().map { langSpec -> requireOutput(processLangSpecInProject::class.java, processLangSpecInProject.Input(input.project, langSpec, input.workspace)) }.toCollection(ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>>())
    val spxCoreResults = requireOutput(WalkContents::class.java, WalkContents.Input(input.project, PPaths.extensionsPathWalker(input.workspace.spxCoreExtensions()))).map { file -> requireOutput(processFileWithSpxCore::class.java, processFileWithSpxCore.Input(file, input.project, input.workspace)) }.toCollection(ArrayList<Tuple4<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>>())
    return output(tuple(input.project, langSpecResults, spxCoreResults))
  }
}

class processLangSpecInProject : Builder<processLangSpecInProject.Input, processLangSpecInProject.Output> {
  data class Input(val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple3<PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, val _2: ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) : Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>> {
    constructor(tuple: Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>) : this(tuple.component1(), tuple.component2())
  }

  private fun output(tuple: Tuple2<ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>, ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>>) = Output(tuple)

  override val id = "processLangSpecInProject"
  override fun BuildContext.build(input: processLangSpecInProject.Input): processLangSpecInProject.Output {
    val results = requireOutput(WalkContents::class.java, WalkContents.Input(input.project, PPaths.extensionsPathWalker(input.langSpec.extensions()))).map { file -> requireOutput(processFileWithLangSpecConfig::class.java, processFileWithLangSpecConfig.Input(file, input.project, input.workspace)) }.toCollection(ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>())
    val partialSolutions = mb.spoofax.runtime.pie.builder.filterNullPartialSolutions(results.map { result -> requireOutput(extractPartialSolution::class.java, result) }.toCollection(ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>()))
    val solutions = if (input.langSpec.natsRootScopePerFile()) partialSolutions.map { partialSolution -> requireOutput(solve::class.java, solve.Input(list(partialSolution), input.project, input.langSpec, input.workspace)) }.toCollection(ArrayList<mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>()) else list(requireOutput(solve::class.java, solve.Input(partialSolutions, input.project, input.langSpec, input.workspace)))
    return output(tuple(results, solutions))
  }
}

class extractPartialSolution : Builder<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
  override val id = "extractPartialSolution"
  override fun BuildContext.build(input: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>): org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? {
    val (file, tokens, messages, styling, partialSolution) = input
    return partialSolution
  }
}

class processString : Builder<processString.Input, processString.Output?> {
  data class Input(val text: String, val file: PPath, val project: PPath, val root: PPath) : Tuple4<String, PPath, PPath, PPath> {
    constructor(tuple: Tuple4<String, PPath, PPath, PPath>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?, val _4: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?, val _5: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?) : Tuple5<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?> {
    constructor(tuple: Tuple5<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>?) = if (tuple == null) null else Output(tuple)

  override val id = "processString"
  override fun BuildContext.build(input: processString.Input): processString.Output? {
    val workspaceConfig = requireOutput(createWorkspaceConfig::class.java, input.root)
    if (workspaceConfig == null) return null
    val workspace = workspaceConfig!!
    val extension = input.file.extension()
    if (extension == null) return null
    val langSpecConfig = workspace.langSpecConfigForExt(extension!!)
    if (langSpecConfig != null) {
      val langSpec = langSpecConfig!!
      val (tokens, messages, styling, partialSolution) = requireOutput(processStringWithLangSpecConfig::class.java, processStringWithLangSpecConfig.Input(input.text, input.file, input.project, langSpec, workspace))
      val otherPartialSolutions = requireOutput(getOtherPartialSolutions::class.java, getOtherPartialSolutions.Input(input.file, input.project, langSpec, workspace))
      val partialSolutions = mb.spoofax.runtime.pie.builder.filterNullPartialSolutions(list(partialSolution) + otherPartialSolutions)
      val solution: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = if (partialSolution == null) null else requireOutput(solve::class.java, solve.Input(partialSolutions, input.project, langSpec, workspace))
      return output(tuple(tokens, messages, styling, partialSolution, solution))
    }
    val spxCoreConfig = workspace.spxCoreConfigForExt(extension!!)
    if (spxCoreConfig != null) {
      val (tokens, messages, styling) = requireOutput(processStringWithSpxCore::class.java, processStringWithSpxCore.Input(input.text, input.file, spxCoreConfig!!))
      val noPartialSolution: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? = null
      val noSolution: mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? = null
      return output(tuple(tokens, messages, styling, noPartialSolution, noSolution) as Tuple5<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?>?)
    }
    return null
  }
}

class getOtherPartialSolutions : Builder<getOtherPartialSolutions.Input, ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>> {
  data class Input(val fileToIgnore: PPath, val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple4<PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = "getOtherPartialSolutions"
  override fun BuildContext.build(input: getOtherPartialSolutions.Input): ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    val projectResults = requireOutput(WalkContents::class.java, WalkContents.Input(input.project, PPaths.extensionsPathWalker(input.langSpec.extensions()))).map { file -> requireOutput(processFileWithLangSpecConfig::class.java, processFileWithLangSpecConfig.Input(file, input.project, input.workspace)) }.toCollection(ArrayList<Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>())
    val partialSolutions = projectResults.map { result -> requireOutput(extractOrRemovePartialSolution::class.java, extractOrRemovePartialSolution.Input(input.fileToIgnore, result)) }.toCollection(ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>())
    return partialSolutions
  }
}

class extractOrRemovePartialSolution : Builder<extractOrRemovePartialSolution.Input, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
  data class Input(val fileToIgnore: PPath, val result: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) : Tuple2<PPath, Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>> {
    constructor(tuple: Tuple2<PPath, Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "extractOrRemovePartialSolution"
  override fun BuildContext.build(input: extractOrRemovePartialSolution.Input): org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? {
    val (file, tokens, messages, styling, partialSolution) = input.result
    return if (file == input.fileToIgnore) null else partialSolution
  }
}

class emptyResult : Builder<PPath, emptyResult.Output> {
  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?, val _5: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?) : Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    constructor(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) = Output(tuple)

  override val id = "emptyResult"
  override fun BuildContext.build(input: PPath): emptyResult.Output {
    val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null
    val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list()
    val emptyStyling: mb.spoofax.runtime.model.style.Styling? = null
    val emptyPartialSolution: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? = null
    return output(tuple(input, emptyTokens, emptyMessages, emptyStyling, emptyPartialSolution))
  }
}

class processFileWithLangSpecConfig : Builder<processFileWithLangSpecConfig.Input, processFileWithLangSpecConfig.Output> {
  data class Input(val file: PPath, val project: PPath, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple3<PPath, PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple3<PPath, PPath, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: PPath, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _4: mb.spoofax.runtime.model.style.Styling?, val _5: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?) : Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    constructor(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  private fun output(tuple: Tuple5<PPath, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) = Output(tuple)

  override val id = "processFileWithLangSpecConfig"
  override fun BuildContext.build(input: processFileWithLangSpecConfig.Input): processFileWithLangSpecConfig.Output {
    val langSpec = requireOutput(langSpecConfigForPath::class.java, langSpecConfigForPath.Input(input.workspace, input.file))
    if (langSpec != null) {
      val text = requireOutput(Read::class.java, input.file)
      val (tokens, messages, styling, partialSolution) = requireOutput(processStringWithLangSpecConfig::class.java, processStringWithLangSpecConfig.Input(text, input.file, input.project, langSpec!!, input.workspace))
      return output(tuple(input.file, tokens, messages, styling, partialSolution))
    } else {
      return output(requireOutput(emptyResult::class.java, input.file))
    }
  }
}

class processStringWithLangSpecConfig : Builder<processStringWithLangSpecConfig.Input, processStringWithLangSpecConfig.Output> {
  data class Input(val text: String, val file: PPath, val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple5<String, PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple5<String, PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?, val _4: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?) : Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
    constructor(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  private fun output(tuple: Tuple4<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?>) = Output(tuple)

  override val id = "processStringWithLangSpecConfig"
  override fun BuildContext.build(input: processStringWithLangSpecConfig.Input): processStringWithLangSpecConfig.Output {
    val (ast, tokenStream, messages) = requireOutput(parse::class.java, parse.Input(input.text, input.file, input.project, input.langSpec, input.workspace))
    val styling: mb.spoofax.runtime.model.style.Styling? = if (tokenStream == null) null else requireOutput(style::class.java, style.Input(tokenStream!!, input.langSpec, input.workspace))
    val partialSolution: org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? = if (ast == null) null else requireOutput(partialSolve::class.java, partialSolve.Input(ast!!, input.file, input.langSpec, input.workspace))
    return output(tuple(tokenStream, messages, styling, partialSolution))
  }
}

class parse : Builder<parse.Input, parse.Output> {
  data class Input(val text: String, val file: PPath, val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple5<String, PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple5<String, PPath, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4(), tuple.component5())
  }

  data class Output(val _1: org.spoofax.interpreter.terms.IStrategoTerm?, val _2: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _3: ArrayList<mb.spoofax.runtime.model.message.Msg>) : Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>> {
    constructor(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<org.spoofax.interpreter.terms.IStrategoTerm?, ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>>) = Output(tuple)

  override val id = "parse"
  override fun BuildContext.build(input: parse.Input): parse.Output {
    val sdfLang = input.workspace.spxCoreConfigForExt("sdf3")
    if (sdfLang == null) return output(requireOutput(emptyParse::class.java, None.instance))
    val files = input.langSpec.syntaxParseFiles()
    val mainFile = input.langSpec.syntaxParseMainFile()
    val startSymbol = input.langSpec.syntaxParseStartSymbolId()
    if (mainFile == null || startSymbol == null) return output(requireOutput(emptyParse::class.java, None.instance))
    val parseTable = requireOutput(mb.spoofax.runtime.pie.builder.GenerateTable::class.java, mb.spoofax.runtime.pie.builder.GenerateTable.Input(sdfLang!!, input.project, files, mainFile!!))
    if (parseTable == null) return output(requireOutput(emptyParse::class.java, None.instance))
    return output(requireOutput(mb.spoofax.runtime.pie.builder.Parse::class.java, mb.spoofax.runtime.pie.builder.Parse.Input(input.text, startSymbol!!, parseTable!!, input.file)))
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

class createSignatures : Builder<createSignatures.Input, mb.spoofax.runtime.impl.sdf.Signatures?> {
  data class Input(val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple2<mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple2<mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "createSignatures"
  override fun BuildContext.build(input: createSignatures.Input): mb.spoofax.runtime.impl.sdf.Signatures? {
    val sdfLang = input.workspace.spxCoreConfigForExt("sdf3")
    if (sdfLang == null) return null
    val files = input.langSpec.syntaxSignatureFiles()
    return requireOutput(mb.spoofax.runtime.pie.builder.GenerateSignatures::class.java, mb.spoofax.runtime.pie.builder.GenerateSignatures.Input(sdfLang!!, input.langSpec.dir(), files)) as mb.spoofax.runtime.impl.sdf.Signatures?
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
    val mainFile = input.langSpec.syntaxStyleFile()
    if (mainFile == null) return null
    val syntaxStyler = requireOutput(mb.spoofax.runtime.pie.builder.GenerateStylerRules::class.java, mb.spoofax.runtime.pie.builder.GenerateStylerRules.Input(esvLang!!, mainFile!!, list()))
    if (syntaxStyler == null) return null
    return requireOutput(mb.spoofax.runtime.pie.builder.Style::class.java, mb.spoofax.runtime.pie.builder.Style.Input(input.tokenStream, syntaxStyler!!)) as mb.spoofax.runtime.model.style.Styling?
  }
}

class createConstraintGenerator : Builder<createConstraintGenerator.Input, mb.spoofax.runtime.impl.nabl.ConstraintGenerator?> {
  data class Input(val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple2<mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple2<mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2())
  }

  override val id = "createConstraintGenerator"
  override fun BuildContext.build(input: createConstraintGenerator.Input): mb.spoofax.runtime.impl.nabl.ConstraintGenerator? {
    val nabl2Lang = input.workspace.spxCoreConfigForExt("nabl2")
    if (nabl2Lang == null) return null
    val nabl2Files = input.langSpec.natsNaBL2Files()
    val strategoConfig = input.langSpec.natsStrategoConfig()
    if (strategoConfig == null) return null
    val strategyStrategyId = input.langSpec.natsStrategoStrategyId()
    if (strategyStrategyId == null) return null
    val signatures = requireOutput(createSignatures::class.java, createSignatures.Input(input.langSpec, input.workspace))
    if (signatures == null) return null
    return requireOutput(mb.spoofax.runtime.pie.builder.NaBL2GenerateConstraintGenerator::class.java, mb.spoofax.runtime.pie.builder.NaBL2GenerateConstraintGenerator.Input(nabl2Lang!!, input.langSpec.dir(), nabl2Files, strategoConfig!!, strategyStrategyId!!, signatures!!)) as mb.spoofax.runtime.impl.nabl.ConstraintGenerator?
  }
}

class partialSolve : Builder<partialSolve.Input, org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?> {
  data class Input(val ast: org.spoofax.interpreter.terms.IStrategoTerm, val file: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple4<org.spoofax.interpreter.terms.IStrategoTerm, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<org.spoofax.interpreter.terms.IStrategoTerm, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = "partialSolve"
  override fun BuildContext.build(input: partialSolve.Input): org.metaborg.meta.nabl2.solver.ImmutablePartialSolution? {
    val generator = requireOutput(createConstraintGenerator::class.java, createConstraintGenerator.Input(input.langSpec, input.workspace))
    if (generator == null) return null
    val initialResult = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2InitialResult::class.java, generator!!)
    val unitResult = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2UnitResult::class.java, mb.spoofax.runtime.pie.builder.NaBL2UnitResult.Input(generator!!, initialResult, input.ast, input.file))
    val partialSolution = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2PartialSolve::class.java, mb.spoofax.runtime.pie.builder.NaBL2PartialSolve.Input(initialResult, unitResult, input.file))
    return partialSolution as org.metaborg.meta.nabl2.solver.ImmutablePartialSolution?
  }
}

class solve : Builder<solve.Input, mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?> {
  data class Input(val partialSolutions: ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution>, val project: PPath, val langSpec: mb.spoofax.runtime.impl.cfg.LangSpecConfig, val workspace: mb.spoofax.runtime.impl.cfg.WorkspaceConfig) : Tuple4<ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution>, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig> {
    constructor(tuple: Tuple4<ArrayList<org.metaborg.meta.nabl2.solver.ImmutablePartialSolution>, PPath, mb.spoofax.runtime.impl.cfg.LangSpecConfig, mb.spoofax.runtime.impl.cfg.WorkspaceConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3(), tuple.component4())
  }

  override val id = "solve"
  override fun BuildContext.build(input: solve.Input): mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution? {
    val generator = requireOutput(createConstraintGenerator::class.java, createConstraintGenerator.Input(input.langSpec, input.workspace))
    if (generator == null) return null
    val initialResult = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2InitialResult::class.java, generator!!)
    val solution = requireOutput(mb.spoofax.runtime.pie.builder.NaBL2Solve::class.java, mb.spoofax.runtime.pie.builder.NaBL2Solve.Input(initialResult, input.partialSolutions, input.project))
    return solution as mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution?
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
    val config = requireOutput(spxCoreConfigForPath::class.java, spxCoreConfigForPath.Input(input.workspace, input.file))
    if (config != null) {
      val text = requireOutput(Read::class.java, input.file)
      val (tokens, messages, styling) = requireOutput(processStringWithSpxCore::class.java, processStringWithSpxCore.Input(text, input.file, config!!))
      return output(tuple(input.file, tokens, messages, styling))
    } else {
      val emptyTokens: ArrayList<mb.spoofax.runtime.model.parse.Token>? = null
      val emptyMessages: ArrayList<mb.spoofax.runtime.model.message.Msg> = list()
      val emptyStyling: mb.spoofax.runtime.model.style.Styling? = null
      return output(tuple(input.file, emptyTokens, emptyMessages, emptyStyling))
    }
  }
}

class processStringWithSpxCore : Builder<processStringWithSpxCore.Input, processStringWithSpxCore.Output> {
  data class Input(val text: String, val file: PPath, val config: mb.spoofax.runtime.impl.cfg.SpxCoreConfig) : Tuple3<String, PPath, mb.spoofax.runtime.impl.cfg.SpxCoreConfig> {
    constructor(tuple: Tuple3<String, PPath, mb.spoofax.runtime.impl.cfg.SpxCoreConfig>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  data class Output(val _1: ArrayList<mb.spoofax.runtime.model.parse.Token>?, val _2: ArrayList<mb.spoofax.runtime.model.message.Msg>, val _3: mb.spoofax.runtime.model.style.Styling?) : Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?> {
    constructor(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) : this(tuple.component1(), tuple.component2(), tuple.component3())
  }

  private fun output(tuple: Tuple3<ArrayList<mb.spoofax.runtime.model.parse.Token>?, ArrayList<mb.spoofax.runtime.model.message.Msg>, mb.spoofax.runtime.model.style.Styling?>) = Output(tuple)

  override val id = "processStringWithSpxCore"
  override fun BuildContext.build(input: processStringWithSpxCore.Input): processStringWithSpxCore.Output {
    val (ast, tokens, messages, _) = requireOutput(mb.spoofax.runtime.pie.builder.core.CoreParse::class.java, mb.spoofax.runtime.pie.builder.core.CoreParse.Input(input.config, input.text, input.file))
    val styling: mb.spoofax.runtime.model.style.Styling? = if (ast == null || tokens == null) null else requireOutput(mb.spoofax.runtime.pie.builder.core.CoreStyle::class.java, mb.spoofax.runtime.pie.builder.core.CoreStyle.Input(input.config, tokens!!, ast!!)) as mb.spoofax.runtime.model.style.Styling?
    return output(tuple(tokens, messages, styling))
  }
}


class PieBuilderModule_spoofax : Module {
  override fun configure(binder: Binder) {
    val builders = binder.builderMapBinder()

    binder.bindBuilder<processStringWithSpxCore>(builders, "processStringWithSpxCore")
    binder.bindBuilder<processFileWithSpxCore>(builders, "processFileWithSpxCore")
    binder.bindBuilder<solve>(builders, "solve")
    binder.bindBuilder<partialSolve>(builders, "partialSolve")
    binder.bindBuilder<createConstraintGenerator>(builders, "createConstraintGenerator")
    binder.bindBuilder<style>(builders, "style")
    binder.bindBuilder<createSignatures>(builders, "createSignatures")
    binder.bindBuilder<emptyParse>(builders, "emptyParse")
    binder.bindBuilder<parse>(builders, "parse")
    binder.bindBuilder<processStringWithLangSpecConfig>(builders, "processStringWithLangSpecConfig")
    binder.bindBuilder<processFileWithLangSpecConfig>(builders, "processFileWithLangSpecConfig")
    binder.bindBuilder<emptyResult>(builders, "emptyResult")
    binder.bindBuilder<extractOrRemovePartialSolution>(builders, "extractOrRemovePartialSolution")
    binder.bindBuilder<getOtherPartialSolutions>(builders, "getOtherPartialSolutions")
    binder.bindBuilder<processString>(builders, "processString")
    binder.bindBuilder<extractPartialSolution>(builders, "extractPartialSolution")
    binder.bindBuilder<processLangSpecInProject>(builders, "processLangSpecInProject")
    binder.bindBuilder<processProject>(builders, "processProject")
    binder.bindBuilder<processWorkspace>(builders, "processWorkspace")
    binder.bindBuilder<createWorkspaceConfig>(builders, "createWorkspaceConfig")
    binder.bindBuilder<spxCoreConfigForPath>(builders, "spxCoreConfigForPath")
    binder.bindBuilder<langSpecConfigForPath>(builders, "langSpecConfigForPath")
    binder.bindBuilder<toMessage>(builders, "toMessage")
  }
}
