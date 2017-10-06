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
import org.metaborg.meta.nabl2.solver.PartialSolution
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult
import org.metaborg.meta.nabl2.spoofax.analysis.UnitResult
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable
import java.util.*
import org.metaborg.meta.nabl2.spoofax.analysis.ImmutableInitialResult
import org.metaborg.meta.nabl2.spoofax.analysis.ImmutableUnitResult

class NaBL2GenerateConstraintGenerator
@Inject constructor(log: Logger)
  : Builder<NaBL2GenerateConstraintGenerator.Input, ConstraintGenerator> {
  val log = log.forContext(NaBL2GenerateConstraintGenerator::class.java)

  companion object {
    val id = "spoofaxGenerateConstraintGenerator"
  }

  data class Input(val nabl2LangConfig: SpxCoreConfig, val specDir: PPath, val nabl2Files: Iterable<PPath>, val strategoConfig: ImmutableStrategoConfig, val strategoStrategyName: String, val signatures: Signatures) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): ConstraintGenerator {
    // Read input files
    val texts = mutableMapOf<PPath, String>()
    for(file in input.nabl2Files) {
      val text = read(file)
      texts.put(file, text)
    }

    // Parse input files
    val parsed = mutableMapOf<PPath, IStrategoTerm>()
    for((file, text) in texts) {
      val (ast, _, _) = parse(input.nabl2LangConfig, text)
      if(ast == null) {
        log.error("Unable to parse NaBL2 file $file, skipping file")
        continue
      }
      parsed.put(file, ast)
    }

    // Load project, required for analysis and transformation.
    val proj = loadProj(input.specDir)

    // Analyze
    val analyzed = mutableMapOf<PPath, IStrategoTerm>()
    for((file, parsedAst) in parsed) {
      val result = analyze(CoreAnalyze.Input(input.nabl2LangConfig, proj.path, file, parsedAst))
      if(result.ast == null) {
        log.error("Unable to analyze NaBL2 file $file, skipping file")
        continue
      }
      analyzed.put(file, result.ast)
    }

    // Transform
    val transformGoal = CompileGoal()
    val nablStrFiles = mutableListOf<PPath>()
    for((file, analyzedAst) in analyzed) {
      val results = trans(input.nabl2LangConfig, proj.path, file, analyzedAst, transformGoal)
      var success = false;
      for((transformedAst, writtenFile) in results) {
        if(transformedAst != null && writtenFile != null) {
          success = true
          nablStrFiles.add(writtenFile)
        }
      }
      if(!success) {
        log.error("Unable to transform NaBL2 file $file, skipping file")
      }
    }

    val strategoConfig = ImmutableStrategoConfig.builder()
      .from(input.strategoConfig)
      .addIncludeDirs(input.signatures.includeDir())
      .build()
    val strategoCtree = compileStratego(strategoConfig)
    val constraintGenerator = ConstraintGenerator(strategoCtree, input.strategoStrategyName)
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
