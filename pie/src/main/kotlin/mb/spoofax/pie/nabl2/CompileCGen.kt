package mb.spoofax.pie.nabl2

import com.google.inject.Inject
import mb.pie.api.*
import mb.pie.vfs.path.PPath
import mb.pie.vfs.path.PathSrv
import mb.spoofax.pie.config.ParseWorkspaceConfig
import mb.spoofax.pie.config.requireConfigValue
import mb.spoofax.pie.legacy.LegacyLoadProject
import mb.spoofax.pie.legacy.Spx
import mb.spoofax.pie.sdf3.SDF3ToStrategoSignatures
import mb.spoofax.pie.stratego.CompileStratego
import mb.spoofax.runtime.cfg.ImmutableStrategoCompilerConfig
import mb.spoofax.runtime.cfg.LangId
import mb.spoofax.runtime.constraint.CGen
import org.apache.commons.vfs2.AllFileSelector
import org.metaborg.spoofax.core.SpoofaxConstants
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths
import java.io.Serializable

class CompileCGen
@Inject constructor(
  private val pathSrv: PathSrv,
  private val parseWorkspaceConfig: ParseWorkspaceConfig,
  private val nabl2ToStrategoCGen: NaBL2ToStrategoCGen,
  private val sdf3ToStrategoSignatures: SDF3ToStrategoSignatures,
  private val legacyLoadProject: LegacyLoadProject,
  private val compileStratego: CompileStratego
) : TaskDef<CompileCGen.Input, CGen?> {
  companion object {
    const val id = "nabl2.CompileCGen"
  }

  data class Input(val langId: LangId, val root: PPath) : Serializable
  data class LangSpecConfigInfo(val dir: PPath, val strategoCompilerConfig: ImmutableStrategoCompilerConfig?, val strategyId: String?) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): CGen? {
    val (langId, root) = input

    val (langSpecDir, strategoCompilerConfig, strategyId) = requireConfigValue(this, parseWorkspaceConfig, root) { workspaceConfig ->
      val langSpecConfig = workspaceConfig.langSpecConfigForId(langId)
      if(langSpecConfig != null) {
        LangSpecConfigInfo(langSpecConfig.dir(), langSpecConfig.natsStrategoConfig(), langSpecConfig.natsStrategoStrategyId())
      } else {
        null
      }
    } ?: throw ExecException("Could not get language specification configuration for language with identifier $langId")
    if(strategoCompilerConfig == null || strategyId == null) {
      return null
    }

    // Generate Stratego files from NaBL2 files
    val nabl2ToStrategoCgenTask = Task(nabl2ToStrategoCGen, NaBL2ToStrategoCGen.Input(langId, root))
    require(nabl2ToStrategoCgenTask)

    // Generate Stratego signatures from SDF3.
    val sdf3ToStrategoSignaturesTask = Task(sdf3ToStrategoSignatures, SDF3ToStrategoSignatures.Input(langId, root))
    val signatures = require(sdf3ToStrategoSignaturesTask)

    // Prepare Stratego compiler config.
    val strategoConfigBuilder = ImmutableStrategoCompilerConfig.builder().from(strategoCompilerConfig)
    // Use sets to collect includes without duplicates.
    val includeDirs = strategoCompilerConfig.includeDirs().toHashSet()
    val includeFiles = strategoCompilerConfig.includeFiles().toHashSet()
    val includeLibs = strategoCompilerConfig.includeLibs().toHashSet()
    // Add Stratego include files and directories, based on Spoofax Core paths.
    val spoofax = Spx.spoofax()
    val project = require(legacyLoadProject, langSpecDir).v
    val paths = SpoofaxLangSpecCommonPaths(project.location())
    val includePaths = spoofax.languagePathService.sourceAndIncludePaths(project, SpoofaxConstants.LANG_STRATEGO_NAME)
    val replicateDir = paths.replicateDir().resolveFile("strj-includes")
    replicateDir.delete(AllFileSelector())
    for(path in includePaths) {
      if(!path.exists()) {
        continue
      }
      if(path.isFolder) {
        val localDir = spoofax.resourceService.localFile(path, replicateDir)
        includeDirs.add(pathSrv.resolveLocal(localDir))
      }
      if(path.isFile) {
        val localFile = spoofax.resourceService.localFile(path, replicateDir)
        includeFiles.add(pathSrv.resolveLocal(localFile))
      }
    }
    // Add signatures to include directories.
    if(signatures != null) {
      includeDirs.add(signatures.includeDir())
    }
    // Add default libraries.
    // TODO: is stratego-sglr required?
    includeLibs.add("stratego-lib")
    // Set other defaults
    if(strategoCompilerConfig.baseDir() == null) {
      strategoConfigBuilder.baseDir(langSpecDir)
    }
    if(strategoCompilerConfig.cacheDir() == null) {
      strategoConfigBuilder.cacheDir(langSpecDir.resolve("target/nabl2-cgen-str-cache"))
    }
    if(strategoCompilerConfig.outputFile() == null) {
      strategoConfigBuilder.outputFile(langSpecDir.resolve("target/nabl2-cgen.ctree"))
    }
    // Finalize includes.
    strategoConfigBuilder.includeDirs(includeDirs)
    strategoConfigBuilder.includeFiles(includeFiles)
    strategoConfigBuilder.includeLibs(includeLibs)
    // Finalize config and compile.
    val finalStrategoCompilerConfig = strategoConfigBuilder.build()
    val taskDeps = arrayListOf(nabl2ToStrategoCgenTask.toSTask(), sdf3ToStrategoSignaturesTask.toSTask())
    val strategoCtree = require(compileStratego, CompileStratego.Input(finalStrategoCompilerConfig, taskDeps))
    return CGen(strategoCtree, strategyId)
  }
}
