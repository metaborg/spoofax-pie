package mb.spoofax.pie.nabl2

import com.google.inject.Inject
import mb.pie.api.*
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.generated.createWorkspaceConfig
import mb.spoofax.pie.sdf3.SDF3ToStrategoSignatures
import mb.spoofax.pie.stratego.CompileStratego
import mb.spoofax.runtime.cfg.ImmutableStrategoConfig
import mb.spoofax.runtime.constraint.CGen
import java.io.Serializable

class CompileCGen
@Inject constructor(
  private val createWorkspaceConfig: createWorkspaceConfig,
  private val nabl2ToStrategoCGen: NaBL2ToStrategoCGen,
  private val sdf3ToStrategoSignatures: SDF3ToStrategoSignatures,
  private val compileStratego: CompileStratego
) : TaskDef<CompileCGen.Input, CGen?> {
  companion object {
    const val id = "nabl2.CompileCGen"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): CGen? {
    val (langSpecExt, root) = input

    // OPTO: only depend on language specification config for langSpecExt.
    val workspaceConfig = require(createWorkspaceConfig, root)
      ?: throw ExecException("Could not get workspace config at root $root")

    // OPTO: only depend on natsStrategoConfig and natsStrategoStrategyId.
    val langSpec = workspaceConfig.langSpecConfigForExt(langSpecExt)
      ?: throw ExecException("Could not get language specification config for extension $langSpecExt")

    // Generate Stratego files from NaBL2 files
    val nabl2ToStrategoCgenTask = Task(nabl2ToStrategoCGen, NaBL2ToStrategoCGen.Input(langSpecExt, root))
    require(nabl2ToStrategoCgenTask)

    // Generate Stratego signatures from SDF3.
    val sdf3ToStrategoSignaturesTask = Task(sdf3ToStrategoSignatures, SDF3ToStrategoSignatures.Input(langSpecExt, root))
    val signatures = require(sdf3ToStrategoSignaturesTask)

    // Compile Stratego
    val strategoConfig = langSpec.natsStrategoConfig() ?: return null
    val strategoConfigBuilder = ImmutableStrategoConfig.builder().from(strategoConfig)
    if(signatures != null) {
      strategoConfigBuilder.addIncludeDirs(signatures.includeDir())
    }
    val finalStrategoConfig = strategoConfigBuilder.build()
    val taskDeps = arrayListOf(nabl2ToStrategoCgenTask.toSTask(), sdf3ToStrategoSignaturesTask.toSTask())
    val strategoCtree = require(compileStratego, CompileStratego.Input(finalStrategoConfig, taskDeps))
    val strategoStrategyName = langSpec.natsStrategoStrategyId() ?: return null
    return CGen(strategoCtree, strategoStrategyName)
  }
}
