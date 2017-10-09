package mb.spoofax.runtime.pie.builder

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.builtin.path.read
import mb.pie.runtime.core.BuildContext
import mb.pie.runtime.core.Builder
import mb.spoofax.runtime.impl.cfg.ImmutableStrategoConfig
import mb.spoofax.runtime.impl.cfg.SpxCoreConfig
import mb.spoofax.runtime.impl.nabl.*
import mb.spoofax.runtime.impl.sdf.Signatures
import mb.spoofax.runtime.impl.stratego.StrategoRuntimeBuilder
import mb.spoofax.runtime.impl.stratego.primitive.ScopeGraphPrimitiveLibrary
import mb.spoofax.runtime.pie.builder.core.*
import mb.spoofax.runtime.pie.builder.stratego.compileStratego
import mb.vfs.path.PPath
import org.metaborg.core.action.CompileGoal
import org.metaborg.meta.nabl2.solver.ImmutablePartialSolution
import org.metaborg.meta.nabl2.spoofax.analysis.ImmutableInitialResult
import org.metaborg.meta.nabl2.spoofax.analysis.ImmutableUnitResult
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable
import java.util.*

class NaBL2GenerateConstraintGenerator
@Inject constructor(log: Logger)
  : Builder<NaBL2GenerateConstraintGenerator.Input, ConstraintGenerator> {
  companion object {
    val id = "spoofaxGenerateConstraintGenerator"
  }

  data class Input(val nabl2LangConfig: SpxCoreConfig, val specDir: PPath, val nabl2Files: Iterable<PPath>, val strategoConfig: ImmutableStrategoConfig, val strategoStrategyName: String, val signatures: Signatures) : Serializable

  val log = log.forContext(NaBL2GenerateConstraintGenerator::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: Input): ConstraintGenerator {
    val (langConfig, projDir, files, strategoConfig, strategoStrategyName, signatures) = input

    // Read input files
    val textFilePairs = files.map {
      val text = read(it)
      CoreParseAll.TextFilePair(text, it)
    }

    // Parse input files
    val parsed = parseAll(langConfig, textFilePairs)
    parsed.forEach { if(it.ast == null) log.error("Unable to parse NaBL2 file ${it.file}, skipping") }

    // Load project, required for analysis and transformation.
    val proj = loadProj(projDir)

    // Analyze
    val analyzePairs = parsed
      .filter { it.ast != null }
      .map { CoreAnalyzeAll.AstFilePair(it.ast!!, it.file) }
    val analyzed = analyzeAll(langConfig, proj.path, analyzePairs)
    analyzed.forEach { if(it.ast == null) log.error("Unable to analyze NaBL2 file ${it.file}, skipping") }

    // Transform
    val transformGoal = CompileGoal()
    val transformPairs = analyzed
      .filter { it.ast != null }
      .map { CoreTransAll.AstFilePair(it.ast!!, it.file) }
    val transformed = transAll(langConfig, proj.path, transformGoal, transformPairs)
    transformed.forEach { if(it.ast == null || it.outputFile == null) log.error("Unable to transform NaBL2 file ${it.inputFile} with $transformGoal, skipping") }

    val finalStrategoConfig = ImmutableStrategoConfig.builder()
      .from(strategoConfig)
      .addIncludeDirs(signatures.includeDir())
      .build()
    val strategoCtree = compileStratego(finalStrategoConfig)
    val constraintGenerator = ConstraintGenerator(strategoCtree, strategoStrategyName)
    return constraintGenerator
  }
}

class NaBL2InitialResult
@Inject constructor(private val primitiveLibrary: ScopeGraphPrimitiveLibrary)
  : Builder<ConstraintGenerator, ImmutableInitialResult> {
  companion object {
    val id = "spoofaxNaBL2InitialResult"
  }

  override val id = Companion.id
  override fun BuildContext.build(input: ConstraintGenerator): ImmutableInitialResult {
    val strategoRuntime = input.createSuitableRuntime(StrategoRuntimeBuilder(), primitiveLibrary)
    require(input.strategoCtree())
    return input.initialResult(strategoRuntime)
  }
}

class NaBL2UnitResult
@Inject constructor(private val primitiveLibrary: ScopeGraphPrimitiveLibrary)
  : Builder<NaBL2UnitResult.Input, ImmutableUnitResult> {
  companion object {
    val id = "NaBL2UnitResult"
  }

  data class Input(val generator: ConstraintGenerator, val initialResult: ImmutableInitialResult, val ast: IStrategoTerm, val file: PPath) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): ImmutableUnitResult {
    val (generator, initialResult, ast, file) = input
    val strategoRuntime = generator.createSuitableRuntime(StrategoRuntimeBuilder(), primitiveLibrary)
    require(generator.strategoCtree())
    return generator.unitResult(initialResult, ast, file.toString(), strategoRuntime)
  }
}

class NaBL2PartialSolve
@Inject constructor(private val solver: ConstraintSolver)
  : Builder<NaBL2PartialSolve.Input, ImmutablePartialSolution> {
  companion object {
    val id = "NaBL2PartialSolve"
  }

  data class Input(val initialResult: ImmutableInitialResult, val unitResult: ImmutableUnitResult, val file: PPath) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): ImmutablePartialSolution {
    val (initialResult, unitResult, file) = input
    return solver.solvePartial(initialResult, unitResult, file)
  }
}

class NaBL2Solve
@Inject constructor(private val solver: ConstraintSolver)
  : Builder<NaBL2Solve.Input, ConstraintSolverSolution> {
  companion object {
    val id = "NaBL2Solve"
  }

  data class Input(val initialResult: ImmutableInitialResult, val partialSolutions: ArrayList<ImmutablePartialSolution>, val project: PPath) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): ConstraintSolverSolution {
    val (initialResult, partialSolutions, project) = input
    return solver.solve(initialResult, partialSolutions, project)
  }
}

fun filterNullPartialSolutions(partialSolutions: ArrayList<ImmutablePartialSolution?>): ArrayList<ImmutablePartialSolution> {
  return partialSolutions.filterNotNull().toCollection(ArrayList())
}
