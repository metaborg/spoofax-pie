package mb.spoofax.pie.constraint

import com.google.inject.Inject
import mb.log.Logger
import mb.nabl2.solver.ImmutableSolution
import mb.nabl2.spoofax.analysis.ImmutableInitialResult
import mb.nabl2.spoofax.analysis.ImmutableUnitResult
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.spoofax.api.SpoofaxEx
import mb.spoofax.runtime.nabl.ConstraintSolver
import java.io.Serializable

class CSolveDocument @Inject constructor(
  log: Logger,
  private val solver: ConstraintSolver
) : TaskDef<CSolveDocument.Input, ImmutableSolution?> {
  private val log: Logger = log.forContext(CSolveDocument::class.java)

  companion object {
    const val id = "constraint.CSolveDocument"
  }

  data class Input(
    val documentConstraints: ImmutableUnitResult,
    val globalConstraints: ImmutableInitialResult,
    val globalSolution: ImmutableSolution
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ImmutableSolution? {
    val (documentConstraints, globalConstraints, globalSolution) = input
    return try {
      solver.solveDocument(documentConstraints, globalConstraints, globalSolution)
    } catch(e: SpoofaxEx) {
      log.error("Solving document constraints failed", e)
      null
    }
  }
}
