package mb.spoofax.pie.config

import com.google.inject.Inject
import mb.fs.java.JavaFSPath
import mb.log.api.Logger
import mb.pie.api.*
import mb.pie.api.fs.stamp.FileSystemStampers
import mb.pie.api.stamp.output.FuncEqualsOutputStamper
import mb.spoofax.pie.legacy.LegacyLoadProject
import mb.spoofax.pie.legacy.processOne
import mb.spoofax.runtime.cfg.ConfigParser
import mb.spoofax.runtime.cfg.WorkspaceConfig
import org.metaborg.core.action.CompileGoal
import java.io.Serializable

class ParseWorkspaceConfig @Inject constructor(
  logFactory: Logger,
  private val configParser: ConfigParser,
  private val legacyLoadProject: LegacyLoadProject
) : TaskDef<JavaFSPath, WorkspaceConfig> {
  private val log: Logger = logFactory.forContext(ParseWorkspaceConfig::class.java)

  companion object {
    const val id = "config.ParseWorkspaceConfig"
  }

  override val id: String = Companion.id
  override fun ExecContext.exec(input: JavaFSPath): WorkspaceConfig {
    val workspaceDir = input
    val paths = run {
      val rootConfigDir = workspaceDir.appendSegment("root")
      val project = require(legacyLoadProject, rootConfigDir).v
      val filePath = rootConfigDir.appendSegment("workspace.cfg")
      val file = toNode(filePath)
      if(!file.exists()) {
        throw ExecException("Cannot parse workspace config; workspace config file $file does not exist")
      }
      val ast = processOne(file, project, transformGoal = CompileGoal(), log = log, requireResourceStamper = FileSystemStampers.hash)?.ast
        ?: throw ExecException("Cannot parse workspace config; failed to parse workspace config file $file")
      configParser.parseWorkspaceConfigPaths(ast, workspaceDir)
    }
    val langSpecConfigs = paths.langSpecConfigFiles().mapNotNull { langSpecFile ->
      val dir = langSpecFile.parent
        ?: throw ExecException("Cannot parse workspace config; $langSpecFile has no parent directory")
      val project = require(legacyLoadProject, dir).v
      val ast = processOne(langSpecFile.toNode(), project, transformGoal = CompileGoal(), log = log, requireResourceStamper = FileSystemStampers.hash)?.ast
        ?: throw ExecException("Cannot parse workspace config; failed to parse language specification config file $langSpecFile")
      val langSpecConfig = configParser.parseLangSpecConfig(ast, dir)
      if(!langSpecConfig.isPresent) {
        throw ExecException("Cannot parse workspace config; $langSpecFile is missing a language identifier")
      }
      langSpecConfig.get()
    }
    return WorkspaceConfig.fromConfigs(langSpecConfigs)
  }
}

fun <T : Out> requireConfigValue(ctx: ExecContext, taskDef: ParseWorkspaceConfig, root: JavaFSPath, valueFunc: (WorkspaceConfig) -> T): T {
  val workspaceConfig = ctx.require(taskDef, root, FuncEqualsOutputStamper(ApplyValueFunc(valueFunc)))
  return valueFunc(workspaceConfig)
}

class ApplyValueFunc<T : Out>(private val valueFunc: (WorkspaceConfig) -> T) : (Out) -> Out, Serializable {
  override fun invoke(out: Out): Out {
    val workspaceConfig = out as WorkspaceConfig
    return valueFunc(workspaceConfig)
  }
}
