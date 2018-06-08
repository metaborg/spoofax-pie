package mb.spoofax.pie.constraint

import com.google.inject.Inject
import mb.log.Logger
import mb.nabl2.solver.ImmutableSolution
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.vfs.path.PPath
import mb.spoofax.api.SpoofaxEx
import mb.spoofax.runtime.nabl.ConstraintSolver
import mb.spoofax.runtime.nabl.ConstraintSolverSolution
import java.io.Serializable
import java.util.*

class CSolveFinal @Inject constructor(
  log: Logger,
  private val solver: ConstraintSolver
) : TaskDef<CSolveFinal.Input, ConstraintSolverSolution?> {
  private val log: Logger = log.forContext(CSolveFinal::class.java)

  companion object {
    const val id = "constraint.CSolveFinal"
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
