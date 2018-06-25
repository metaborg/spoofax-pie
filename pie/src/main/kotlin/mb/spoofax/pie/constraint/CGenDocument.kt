package mb.spoofax.pie.constraint

import com.google.inject.Inject
import mb.log.Logger
import mb.nabl2.spoofax.analysis.ImmutableInitialResult
import mb.nabl2.spoofax.analysis.ImmutableUnitResult
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.api.SpoofaxEx
import mb.spoofax.pie.nabl2.CompileCGen
import mb.spoofax.runtime.cfg.LangId
import mb.spoofax.runtime.stratego.StrategoRuntimeBuilder
import mb.spoofax.runtime.stratego.primitive.ScopeGraphPrimitiveLibrary
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

class CGenDocument @Inject constructor(
  log: Logger,
  private val compileCGen: CompileCGen,
  private val primitiveLibrary: ScopeGraphPrimitiveLibrary
) : TaskDef<CGenDocument.Input, ImmutableUnitResult?> {
  private val log: Logger = log.forContext(CGenDocument::class.java)

  companion object {
    const val id = "constraint.CGenDocument"
  }

  data class Input(
    val document: PPath,
    val langId: LangId,
    val root: PPath,
    val ast: IStrategoTerm,
    val globalConstraints: ImmutableInitialResult
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ImmutableUnitResult? {
    val (document, langId, root, ast, globalConstraints) = input
    val cgen = require(compileCGen, CompileCGen.Input(langId, root)) ?: return null
    val strategoRuntime = cgen.createSuitableRuntime(StrategoRuntimeBuilder(), primitiveLibrary)
    require(cgen.strategoCtree(), FileStampers.hash)
    return try {
      cgen.cgenDocument(globalConstraints, ast, document.toString(), strategoRuntime)
    } catch(e: SpoofaxEx) {
      log.error("Generating document constraints failed", e)
      null
    }
  }
}
