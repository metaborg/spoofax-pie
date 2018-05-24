package mb.spoofax.pie.nabl2

import com.google.inject.Inject
import mb.pie.api.*
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.generated.createWorkspaceConfig
import mb.spoofax.pie.sdf3.GenerateStrategoSignatures
import mb.spoofax.pie.stratego.Compile
import mb.spoofax.runtime.nabl.CGen
import java.io.Serializable
import mb.spoofax.runtime.cfg.ImmutableStrategoConfig

class CompileStrategoCGen
@Inject constructor(
) : TaskDef<CompileStrategoCGen.Input, CGen?> {
  companion object {
    const val id = "nabl2.CompileStrategoCGen"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): CGen? {
    val (langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class.java, createWorkspaceConfig.id, root)
        ?: throw ExecException("Could not create workspace config for root $root")
    val langSpec =
      workspace.langSpecConfigForExt(input.langSpecExt)
        ?: throw ExecException("Could not get language specification config for extension $langSpecExt")

    // Generate Stratego files from NaBL2 files
    val genStrFromNablApp = Task(GenerateStrategoCGen::class.java, GenerateStrategoCGen.id, GenerateStrategoCGen.Input(langSpecExt, root))
    requireExec(genStrFromNablApp)

    // Generate Stratego signatures from SDF3.
    val genSigApp = Task(GenerateStrategoSignatures::class.java, GenerateStrategoSignatures.id, GenerateStrategoSignatures.Input(langSpecExt, root))
    val signatures = requireOutput(genSigApp)

    // Compile Stratego
    val strategoConfig = langSpec.natsStrategoConfig() ?: return null
    val strategoConfigBuilder = ImmutableStrategoConfig.builder().from(strategoConfig)
    if(signatures != null) {
      strategoConfigBuilder.addIncludeDirs(signatures.includeDir())
    }
    val finalStrategoConfig = strategoConfigBuilder.build()
    val strategoCtree = requireOutput(Compile::class.java, Compile.id, Compile.Input(
      finalStrategoConfig, arrayListOf(genStrFromNablApp, genSigApp)
    ))
    val strategoStrategyName = langSpec.natsStrategoStrategyId() ?: return null
    return CGen(strategoCtree, strategoStrategyName)
  }
}
