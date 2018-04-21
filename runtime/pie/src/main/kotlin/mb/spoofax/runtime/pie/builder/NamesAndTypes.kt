package mb.spoofax.runtime.pie.builder

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.builtin.util.Tuple5
import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.PathStampers
import mb.spoofax.runtime.impl.cfg.ImmutableStrategoConfig
import mb.spoofax.runtime.impl.nabl.*
import mb.spoofax.runtime.impl.stratego.StrategoRuntimeBuilder
import mb.spoofax.runtime.impl.stratego.primitive.ScopeGraphPrimitiveLibrary
import mb.spoofax.runtime.model.message.Msg
import mb.spoofax.runtime.model.parse.Token
import mb.spoofax.runtime.model.style.Styling
import mb.spoofax.runtime.pie.builder.core.*
import mb.spoofax.runtime.pie.builder.stratego.CompileStratego
import mb.spoofax.runtime.pie.generated.createWorkspaceConfig
import mb.vfs.path.PPath
import org.metaborg.core.action.CompileGoal
import org.metaborg.meta.nabl2.solver.ImmutablePartialSolution
import org.metaborg.meta.nabl2.spoofax.analysis.ImmutableInitialResult
import org.metaborg.meta.nabl2.spoofax.analysis.ImmutableUnitResult
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable
import java.util.*

class NaBL2ToStratego
@Inject constructor(
  log: Logger
) : Func<NaBL2ToStratego.Input, None> {
  val log = log.forContext(NaBL2ToStratego::class.java)

  companion object {
    val id = "spoofaxNaBL2ToStratego"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): None {
    val (langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.Companion.id, root)
        ?: throw ExecException("Could not create workspace config for root $root")
    val metaLangExt = "nabl2"
    val metaLangConfig = workspace.spxCoreConfigForExt(metaLangExt)
      ?: throw ExecException("Could not get meta-language config for extension $metaLangExt")
    val metaLangImpl = buildOrLoad(metaLangConfig)
    val langSpec =
      workspace.langSpecConfigForExt(input.langSpecExt)
        ?: throw ExecException("Could not get language specification config for extension $langSpecExt")
    val langSpecProject = loadProj(langSpec.dir())
    val files = langSpec.natsNaBL2Files() ?: return None.instance
    val outputs = process(files, metaLangImpl, langSpecProject, true, CompileGoal(), log)
    outputs.reqFiles.forEach { require(it, PathStampers.hash) }
    outputs.genFiles.forEach { generate(it, PathStampers.hash) }

    return None.instance
  }
}

class NaBL2GenerateConstraintGenerator
@Inject constructor(
  log: Logger
) : Func<NaBL2GenerateConstraintGenerator.Input, ConstraintGenerator?> {
  val log = log.forContext(NaBL2GenerateConstraintGenerator::class.java)

  companion object {
    val id = "spoofaxGenerateConstraintGenerator"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ConstraintGenerator? {
    val (langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.Companion.id, root)
        ?: throw ExecException("Could not create workspace config for root $root")
    val langSpec =
      workspace.langSpecConfigForExt(input.langSpecExt)
        ?: throw ExecException("Could not get language specification config for extension $langSpecExt")
    val langSpecProject = loadProj(langSpec.dir())

    // Generate Stratego files from NaBL2 files
    val genStrFromNablApp = FuncApp(NaBL2ToStratego::class.java, NaBL2ToStratego.Companion.id, NaBL2ToStratego.Input(langSpecExt, root))
    requireExec(genStrFromNablApp)

    // Generate Stratego signatures from SDF3.
    val genSigApp = FuncApp(GenerateSignatures::class.java, GenerateSignatures.Companion.id, GenerateSignatures.Input(langSpecExt, root))
    val signatures = requireOutput(genSigApp)

    // Compile Stratego
    val strategoConfig = langSpec.natsStrategoConfig() ?: return null
    val strategoConfigBuilder = ImmutableStrategoConfig.builder().from(strategoConfig)
    if(signatures != null) {
      strategoConfigBuilder.addIncludeDirs(signatures.includeDir())
    }
    val finalStrategoConfig = strategoConfigBuilder.build()
    val strategoCtree = requireOutput(CompileStratego::class, CompileStratego.Companion.id, CompileStratego.Input(
      finalStrategoConfig, arrayListOf(genStrFromNablApp, genSigApp)
    ))
    val strategoStrategyName = langSpec.natsStrategoStrategyId() ?: return null
    val constraintGenerator = ConstraintGenerator(strategoCtree, strategoStrategyName)
    return constraintGenerator
  }
}

class NaBL2InitialResult
@Inject constructor(
  private val primitiveLibrary: ScopeGraphPrimitiveLibrary
) : Func<ConstraintGenerator, ImmutableInitialResult> {
  companion object {
    val id = "spoofaxNaBL2InitialResult"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: ConstraintGenerator): ImmutableInitialResult {
    val strategoRuntime = input.createSuitableRuntime(StrategoRuntimeBuilder(), primitiveLibrary)
    require(input.strategoCtree(), PathStampers.hash)
    return input.initialResult(strategoRuntime)
  }
}

class NaBL2UnitResult
@Inject constructor(
  private val primitiveLibrary: ScopeGraphPrimitiveLibrary
) : Func<NaBL2UnitResult.Input, ImmutableUnitResult> {
  companion object {
    val id = "NaBL2UnitResult"
  }

  data class Input(
    val generator: ConstraintGenerator,
    val initialResult: ImmutableInitialResult,
    val ast: IStrategoTerm,
    val file: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ImmutableUnitResult {
    val (generator, initialResult, ast, file) = input
    val strategoRuntime = generator.createSuitableRuntime(StrategoRuntimeBuilder(), primitiveLibrary)
    require(generator.strategoCtree(), PathStampers.hash)
    return generator.unitResult(initialResult, ast, file.toString(), strategoRuntime)
  }
}

class NaBL2PartialSolve
@Inject constructor(
  private val solver: ConstraintSolver
) : Func<NaBL2PartialSolve.Input, ImmutablePartialSolution> {
  companion object {
    val id = "NaBL2PartialSolve"
  }

  data class Input(
    val initialResult: ImmutableInitialResult,
    val unitResult: ImmutableUnitResult,
    val file: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ImmutablePartialSolution {
    val (initialResult, unitResult, file) = input
    return solver.solvePartial(initialResult, unitResult, file)
  }
}

class NaBL2Solve
@Inject constructor(
  private val solver: ConstraintSolver
) : Func<NaBL2Solve.Input, ConstraintSolverSolution> {
  companion object {
    val id = "NaBL2Solve"
  }

  data class Input(
    val initialResult: ImmutableInitialResult,
    val partialSolutions: ArrayList<ImmutablePartialSolution>,
    val project: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ConstraintSolverSolution {
    val (initialResult, partialSolutions, project) = input
    return solver.solve(initialResult, partialSolutions, project)
  }
}


fun filterNullPartialSolutions(partialSolutions: ArrayList<ImmutablePartialSolution?>): ArrayList<ImmutablePartialSolution> {
  return partialSolutions.filterNotNull().toCollection(ArrayList())
}

fun extractPartialSolution(result: Tuple5<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?, ImmutablePartialSolution?>): ImmutablePartialSolution? {
  return result.component5()
}

fun extractOrRemovePartialSolution(fileToIgnore: PPath, result: Tuple5<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?, ImmutablePartialSolution?>): ImmutablePartialSolution? {
  val (file, _, _, _, partialSolution) = result
  return if(file == fileToIgnore) null else partialSolution
}
