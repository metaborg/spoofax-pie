package mb.pipe.run.ceres.spoofax.core

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.BuildException
import mb.ceres.OutEffectBuilder
import mb.ceres.PathStampers
import mb.pipe.run.ceres.path.cPath
import mb.pipe.run.core.log.Logger
import mb.pipe.run.core.path.DirAccess
import mb.pipe.run.core.path.PPath
import mb.pipe.run.core.path.PPaths
import mb.pipe.run.core.path.PathSrv
import org.eclipse.jdt.core.compiler.batch.BatchCompiler
import org.metaborg.spoofax.meta.core.build.LanguageSpecBuildInput
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths
import java.io.PrintWriter
import mb.pipe.run.core.log.LoggingOutputStream
import mb.pipe.run.core.log.Level


class CoreBuildLangSpec @Inject constructor(log: Logger, val pathSrv: PathSrv) : OutEffectBuilder<PPath> {
  companion object {
    val id = "coreBuildLangSpec"
  }

  val log: Logger = log.forContext(CoreBuild::class.java)

  override val id = Companion.id
  override fun BuildContext.effect(input: PPath) {
    val spoofax = Spx.spoofax()
    val spoofaxMeta = Spx.spoofaxMeta()

    val project = spoofax.projectService.get(input.fileObject) ?: throw BuildException("Cannot build language specification at $input, it has not been loaded as a project")
    val langSpec = spoofaxMeta.languageSpecService.get(project) ?: throw BuildException("Cannot build language specification from project $project, it is not a language specification")
    val langSpecBuildInput = LanguageSpecBuildInput(langSpec)

    // Require all SDF and Stratego files
    input.walk(PPaths.extensionsPathWalker(listOf("str", "sdf3")), object : DirAccess {
      override fun writeDir(dir: PPath?) = Unit // Directories are not written during path walking.
      override fun readDir(dir: PPath) = require(dir.cPath, PathStampers.nonRecursiveModified)
    }).forEach { require(it.cPath, PathStampers.hash) }

    // Generate sources and compile
    spoofaxMeta.metaBuilder.initialize(langSpecBuildInput)
    spoofaxMeta.metaBuilder.generateSources(langSpecBuildInput, null)
    spoofaxMeta.metaBuilder.compile(langSpecBuildInput)

    // Compile Java
    // Get paths to files and directories
    val commonPaths = SpoofaxLangSpecCommonPaths(input.fileObject)
    val strategiesDir = commonPaths.strJavaStratDir().pPath
    require(strategiesDir.cPath, PathStampers.exists)
    if (strategiesDir.exists()) {
      val targetClassesDir = commonPaths.targetClassesDir().pPath
      val spoofaxUberJar = pathSrv.resolveLocal("/Users/gohla/spoofax/master/repo/spoofax-releng/spoofax/org.metaborg.spoofax.core.uber/target/org.metaborg.spoofax.core.uber-2.3.0-SNAPSHOT.jar");

      // Require the java files and Spoofax uber JAR
      require(strategiesDir.cPath, PathStampers.modified)
      require(spoofaxUberJar.cPath, PathStampers.hash)

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
      generate(targetClassesDir.cPath)
    }

    // Package and archive
    spoofaxMeta.metaBuilder.pkg(langSpecBuildInput)
    spoofaxMeta.metaBuilder.archive(langSpecBuildInput)

    // Require the generated archives
    val targetMetaborgDir = commonPaths.targetMetaborgDir()
    require(targetMetaborgDir.cPath, PathStampers.hash)
  }
}

fun BuildContext.buildLangSpec(input: PPath) = requireOutput(CoreBuildLangSpec::class.java, input)