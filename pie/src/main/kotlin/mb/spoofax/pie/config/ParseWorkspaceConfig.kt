package mb.spoofax.pie.config

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.output.FuncEqualsOutputStamper
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.legacy.processOne
import mb.spoofax.runtime.cfg.ConfigParser
import mb.spoofax.runtime.cfg.WorkspaceConfig

class ParseWorkspaceConfig @Inject constructor(
  logFactory: Logger,
  private val configParser: ConfigParser
) : TaskDef<PPath, WorkspaceConfig> {
  private val log: Logger = logFactory.forContext(ParseWorkspaceConfig::class.java)

  companion object {
    const val id = "config.ParseWorkspaceConfig"
  }

  override val id: String = Companion.id
  override fun ExecContext.exec(input: PPath): WorkspaceConfig {
    val root = input
    val workspaceFile = root.resolve("root/workspace.cfg")
    val workspaceAst = processOne(workspaceFile, log = log)?.ast
      ?: throw ExecException("Cannot parse workspace config; failed to parse workspace config file $workspaceFile")
    val workspaceConfigPaths = configParser.parseWorkspaceConfigPaths(workspaceAst, root)
    val langSpecConfigs = workspaceConfigPaths.langSpecConfigFiles().mapNotNull { langSpecFile ->
      val ast = processOne(langSpecFile, log = log)?.ast
        ?: throw ExecException("Cannot parse workspace config; failed to parse language specification config file $langSpecFile")
      val dir = langSpecFile.parent()
        ?: throw ExecException("Cannot parse workspace config; $langSpecFile has no parent directory")
      configParser.parseLangSpecConfig(ast, dir)
    }
    return WorkspaceConfig.fromConfigs(langSpecConfigs)
  }

  fun <T : Out> ExecContext.requireConfigValue(root: PPath, valueFunc: (WorkspaceConfig) -> T): T {
    val workspaceConfig = require(this@ParseWorkspaceConfig, root, FuncEqualsOutputStamper { out ->
      valueFunc(out as WorkspaceConfig)
    })
    return valueFunc(workspaceConfig)
  }
}
