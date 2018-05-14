package mb.spoofax.runtime.pie.nabl2

import com.google.inject.Inject
import mb.log.Logger
import mb.nabl2.solver.ImmutableSolution
import mb.pie.runtime.core.ExecContext
import mb.pie.runtime.core.TaskDef
import mb.spoofax.runtime.impl.nabl.ConstraintSolver
import mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution
import mb.spoofax.runtime.model.SpoofaxEx
import mb.vfs.path.PPath
import java.io.Serializable
import java.util.*

class SolveFinal
@Inject constructor(
  log: Logger,
  private val solver: ConstraintSolver
) : TaskDef<SolveFinal.Input, ConstraintSolverSolution?> {
  private val log: Logger = log.forContext(SolveFinal::class.java)

  companion object {
    const val id = "nabl2.SolveFinal"
  }

  data class Input(
    val documentSolutions: ArrayList<ImmutableSolution>,
    val globalSolution: ImmutableSolution,
    val projectPath: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ConstraintSolverSolution? {
    val (documentSolutions, globalSolution, projectPath) = input
    return try {
      solver.solve(documentSolutions, globalSolution, projectPath)
    } catch(e: SpoofaxEx) {
      log.error("Finally solving constraints failed", e)
      null
    }
  }
}
