package mb.spoofax.runtime.pie.legacy

import com.google.inject.Inject
import mb.log.*
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.access.DirAccess
import mb.pie.vfs.path.*
import org.eclipse.jdt.core.compiler.batch.BatchCompiler
import org.metaborg.core.action.CompileGoal
import org.metaborg.core.build.BuildInputBuilder
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector
import org.metaborg.spoofax.meta.core.build.LanguageSpecBuildInput
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths
import java.io.PrintWriter

class CoreBuild @Inject constructor(log: Logger) : TaskDef<PPath, None> {
  companion object {
    const val id = "coreBuild"
  }

  val log: Logger = log.forContext(CoreBuild::class.java)

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): None {
    val spoofax = Spx.spoofax()
    val project = loadProj(input)
    val inputBuilder = BuildInputBuilder(project)
    val buildInput = inputBuilder
      .withDefaultIncludePaths(true)
      .withSourcesFromDefaultSourceLocations(true)
      .withSelector(SpoofaxIgnoresSelector())
      .addTransformGoal(CompileGoal())
      .build(spoofax.dependencyService, spoofax.languagePathService)
    val output = spoofax.builder.build(buildInput)

    // Required files
    output.includedResources().forEach { require(it.pPath, FileStampers.hash) }
    output.changedResources().forEach { require(it.pPath, FileStampers.hash) }

    // TODO: make requirements to changes in source and include paths, to rebuild when files are added/removed

    // Generated files
    output.transformResults()
      .flatMap { it.outputs() }
      .mapNotNull { it.output()?.pPath }
      .forEach { generate(it) }

    return None.instance
  }
}

fun ExecContext.buildProject(input: PPath) = requireExec(CoreBuild::class.java, CoreBuild.id, input)


class CoreBuildLangSpec @Inject constructor(log: Logger, private val pathSrv: PathSrv) : TaskDef<PPath, None> {
  companion object {
    const val id = "coreBuildLangSpec"
  }

  val log: Logger = log.forContext(CoreBuild::class.java)

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): None {
    val spoofaxMeta = Spx.spoofaxMeta()

    val project = loadProj(input)
    val langSpec = spoofaxMeta.languageSpecService.get(project)
      ?: throw ExecException("Cannot build language specification from project $project, it is not a language specification")
    val langSpecBuildInput = LanguageSpecBuildInput(langSpec)

    // Require all SDF and Stratego files
    input.walk(PPaths.extensionsPathWalker(listOf("str", "sdf3")), object : DirAccess {
      override fun writeDir(dir: PPath?) = Unit // Directories are not written during file walking.
      override fun readDir(dir: PPath) {
        require(dir, FileStampers.modified)
      }
    }).use { pathStream -> pathStream.forEach { require(it, FileStampers.hash) } }

    // Generate sources and compile
    spoofaxMeta.metaBuilder.initialize(langSpecBuildInput)
    spoofaxMeta.metaBuilder.generateSources(langSpecBuildInput, null)
    spoofaxMeta.metaBuilder.compile(langSpecBuildInput)

    // Compile Java
    // Get paths to files and directories
    val commonPaths = SpoofaxLangSpecCommonPaths(input.fileObject)
    val strategiesDir = commonPaths.strJavaStratDir().pPath
    require(strategiesDir, FileStampers.exists)
    if(strategiesDir.exists()) {
      val targetClassesDir = commonPaths.targetClassesDir().pPath
      val spoofaxUberJar = pathSrv.resolveLocal("/Users/gohla/spoofax/master/repo/builder-releng/builder/org.metaborg.builder.core.uber/target/org.metaborg.builder.legacy.uber-2.4.0-SNAPSHOT.jar");

      // Require the java files and Spoofax uber JAR
      require(strategiesDir, FileStampers.modified)
      require(spoofaxUberJar, FileStampers.hash)

      // Execute the Java compiler
      val args = arrayOf(
        "${strategiesDir.javaPath.toFile()}", // Input directory
        "-cp ${spoofaxUberJar.javaPath.toFile()}", // Classpath
        "-d ${targetClassesDir.javaPath.toFile()}", // Output directory
        "-1.8", // Use Java 8
        "-g" // Generate debug attributes
      )
      BatchCompiler.compile(args, PrintWriter(LoggingOutputStream(log, Level.Info), true), PrintWriter(LoggingOutputStream(log, Level.Error), true), null)

      // Require the generated class files
      generate(targetClassesDir)
    }

    // Package and archive
    spoofaxMeta.metaBuilder.pkg(langSpecBuildInput)
    spoofaxMeta.metaBuilder.archive(langSpecBuildInput)

    // Require the generated archives
    val targetMetaborgDir = commonPaths.targetMetaborgDir()
    require(targetMetaborgDir.pPath, FileStampers.hash)

    return None.instance
  }
}

fun ExecContext.buildLangSpec(input: PPath) = requireExec(CoreBuildLangSpec::class.java, CoreBuildLangSpec.id, input)
