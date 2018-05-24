package mb.spoofax.pie.nabl2

import com.google.inject.Inject
import mb.log.Logger
import mb.nabl2.solver.ImmutableSolution
import mb.nabl2.spoofax.analysis.ImmutableInitialResult
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.spoofax.api.SpoofaxEx
import mb.spoofax.runtime.nabl.ConstraintSolver

class SolveGlobal
@Inject constructor(
  log: Logger,
  private val solver: ConstraintSolver
) : TaskDef<ImmutableInitialResult, ImmutableSolution?> {
  private val log: Logger = log.forContext(SolveGlobal::class.java)

  companion object {
    const val id = "nabl2.SolveGlobal"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: ImmutableInitialResult): ImmutableSolution? {
    return try {
      solver.solveGlobal(input)
    } catch(e: SpoofaxEx) {
      log.error("Solving global constraints failed", e)
      null
    }
  }
}
