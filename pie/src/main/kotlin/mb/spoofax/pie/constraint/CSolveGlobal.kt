package mb.spoofax.pie.constraint

import com.google.inject.Inject
import mb.log.Logger
import mb.nabl2.solver.ImmutableSolution
import mb.nabl2.spoofax.analysis.ImmutableInitialResult
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.spoofax.api.SpoofaxEx
import mb.spoofax.runtime.constraint.CSolver

class CSolveGlobal @Inject constructor(
  log: Logger,
  private val solver: CSolver
) : TaskDef<ImmutableInitialResult, ImmutableSolution?> {
  private val log: Logger = log.forContext(CSolveGlobal::class.java)

  companion object {
    const val id = "constraint.CSolveGlobal"
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
