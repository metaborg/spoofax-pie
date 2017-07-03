package mb.pipe.run.ceres.spoofax.core

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.BuildException
import mb.ceres.OutEffectBuilder
import mb.ceres.PathStampers
import mb.pipe.run.core.log.Logger
import mb.pipe.run.core.path.PPath
import org.metaborg.core.action.CompileGoal
import org.metaborg.core.build.BuildInputBuilder
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector

class CoreBuild @Inject constructor(log: Logger) : OutEffectBuilder<PPath> {
  companion object {
    val id = "coreBuild"
  }

  val log: Logger = log.forContext(CoreBuild::class.java)

  override val id = Companion.id
  override fun BuildContext.effect(input: PPath) {
    val spoofax = Spx.spoofax()
    val project = spoofax.projectService.get(input.fileObject) ?: throw BuildException("Cannot build project at $input, it has not been loaded as a project")
    val inputBuilder = BuildInputBuilder(project)
    // @formatter:off
    val buildInput = inputBuilder
      .withDefaultIncludePaths(true)
      .withSourcesFromDefaultSourceLocations(true)
      .withSelector(SpoofaxIgnoresSelector())
      .addTransformGoal(CompileGoal())
      .build(spoofax.dependencyService, spoofax.languagePathService)
    // @formatter:on
    val output = spoofax.builder.build(buildInput)

    // Required files
    output.includedResources().forEach { require(it.cPath, PathStampers.hash) }
    output.changedResources().forEach { require(it.cPath) }

    // TODO: make requirements to changes in source and include paths, to rebuild when files are added/removed

    // Generated files
    output.transformResults()
      .flatMap { it.outputs() }
      .mapNotNull { it.output()?.cPath }
      .forEach { generate(it) }
  }
}

fun BuildContext.build(input: PPath) = requireOutput(CoreBuild::class.java, input)