package mb.spoofax.runtime.pie.nabl2

import com.google.inject.Inject
import mb.log.Logger
import mb.nabl2.spoofax.analysis.ImmutableInitialResult
import mb.pie.runtime.ExecContext
import mb.pie.runtime.TaskDef
import mb.pie.runtime.stamp.FileStampers
import mb.spoofax.runtime.impl.stratego.StrategoRuntimeBuilder
import mb.spoofax.runtime.impl.stratego.primitive.ScopeGraphPrimitiveLibrary
import mb.spoofax.runtime.model.SpoofaxEx
import mb.vfs.path.PPath
import java.io.Serializable

class CGenGlobal
@Inject constructor(
  log: Logger,
  private val primitiveLibrary: ScopeGraphPrimitiveLibrary
) : TaskDef<CGenGlobal.Input, ImmutableInitialResult?> {
  private val log: Logger = log.forContext(CGenGlobal::class.java)

  companion object {
    const val id = "nabl2.CGenGlobal"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ImmutableInitialResult? {
    val (langSpecExt, root) = input
    val constraintGenerator = requireOutput(CompileStrategoCGen::class.java, CompileStrategoCGen.id, CompileStrategoCGen.Input(
      langSpecExt, root
    )) ?: return null
    val strategoRuntime = constraintGenerator.createSuitableRuntime(StrategoRuntimeBuilder(), primitiveLibrary)
    require(constraintGenerator.strategoCtree(), FileStampers.hash)
    return try {
      constraintGenerator.cgenGlobal(strategoRuntime)
    } catch(e: SpoofaxEx) {
      log.error("Generating global constraints failed", e)
      null
    }
  }
}
