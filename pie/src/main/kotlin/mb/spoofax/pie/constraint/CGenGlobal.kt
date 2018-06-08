package mb.spoofax.pie.constraint

import com.google.inject.Inject
import mb.log.Logger
import mb.nabl2.spoofax.analysis.ImmutableInitialResult
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.api.SpoofaxEx
import mb.spoofax.pie.nabl2.CompileCGen
import mb.spoofax.runtime.stratego.StrategoRuntimeBuilder
import mb.spoofax.runtime.stratego.primitive.ScopeGraphPrimitiveLibrary
import java.io.Serializable

class CGenGlobal @Inject constructor(
  log: Logger,
  private val compileCGen: CompileCGen,
  private val primitiveLibrary: ScopeGraphPrimitiveLibrary
) : TaskDef<CGenGlobal.Input, ImmutableInitialResult?> {
  private val log: Logger = log.forContext(CGenGlobal::class.java)

  companion object {
    const val id = "constraint.CGenGlobal"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ImmutableInitialResult? {
    val (langSpecExt, root) = input
    val cgen = require(compileCGen, CompileCGen.Input(langSpecExt, root)) ?: return null
    val strategoRuntime = cgen.createSuitableRuntime(StrategoRuntimeBuilder(), primitiveLibrary)
    require(cgen.strategoCtree(), FileStampers.hash)
    return try {
      cgen.cgenGlobal(strategoRuntime)
    } catch(e: SpoofaxEx) {
      log.error("Generating global constraints failed", e)
      null
    }
  }
}
