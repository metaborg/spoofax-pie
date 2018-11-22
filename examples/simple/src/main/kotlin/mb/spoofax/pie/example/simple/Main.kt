package mb.spoofax.pie.example.simple

import com.google.inject.Singleton
import mb.fs.java.JavaFSNode
import mb.log.slf4j.LogModule
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.logger.StreamLogger
import mb.pie.store.lmdb.withLMDBStore
import mb.pie.taskdefs.guice.withGuiceTaskDefs
import mb.spoofax.api.SpoofaxFacade
import mb.spoofax.api.StaticSpoofaxFacade
import mb.spoofax.legacy.LoadMetaLanguages
import mb.spoofax.legacy.StaticSpoofaxCoreFacade
import mb.spoofax.pie.*
import mb.spoofax.pie.generated.TaskDefsModule_spoofax
import mb.spoofax.runtime.SpoofaxRuntimeModule
import org.metaborg.core.editor.IEditorRegistry
import org.metaborg.core.editor.NullEditorRegistry
import org.metaborg.spoofax.core.Spoofax
import org.metaborg.spoofax.meta.core.SpoofaxExtensionModule
import org.metaborg.spoofax.meta.core.SpoofaxMeta
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  // Read input arguments.
  if(args.size < 3) {
    println("Expected 2 argument, got ${args.size}. Example: java -jar target/example.jar workspace characters example.chr")
    exitProcess(1)
  }
  val workspaceDirStr = args[0] // Workspace directory
  val containerDirRelStr = args[1] // Container (project) directory, relative to workspace.
  val documentFileRelStr = args[2] // Document file, relative to container.

  // Setup Spoofax Core (legacy) through its facade.
  val spoofaxCoreFacade = Spoofax(SpoofaxCoreModule(), SpoofaxExtensionModule())
  val spoofaxCoreMetaFacade = SpoofaxMeta(spoofaxCoreFacade)
  StaticSpoofaxCoreFacade.init(spoofaxCoreMetaFacade)

  // Setup Spoofax-PIE trough its facade.
  val spoofaxFacade = SpoofaxFacade(
    SpoofaxRuntimeModule(), // Spoofax runtime (implementation)
    LogModule(LoggerFactory.getLogger("getRoot")), // SLF4J logging support
    SpoofaxPieModule(), // Spoofax-PIE support
    SpoofaxPieTaskDefsModule(), // Spoofax-PIE task definitions
    TaskDefsModule_spoofax() // Spoofax-PIE generated task definitions
  )
  val injector = spoofaxFacade.injector
  val spoofaxPipeline = injector.getInstance(SpoofaxPipeline::class.java)
  StaticSpoofaxFacade.init(spoofaxFacade)

  // Convert workspace directory to format that PIE can work with.
  val workspaceDir = JavaFSNode(workspaceDirStr)
  if(!workspaceDir.exists() || !workspaceDir.isDirectory) {
    println("Workspace at path $workspaceDir does not exist or is not a directory")
    exitProcess(2)
  }
  val containerDir = workspaceDir.appendSegment(containerDirRelStr)
  if(!containerDir.exists() || !containerDir.isDirectory) {
    println("Container (project) at path $containerDir does not exist or is not a directory")
    exitProcess(2)
  }
  val documentFile = containerDir.appendSegment(documentFileRelStr)
  if(!documentFile.exists() || !documentFile.isFile) {
    println("Document file at path $documentFile does not exist or is not a file")
    exitProcess(2)
  }

  // Load the Spoofax Core meta-languages that Spoofax-PIE requires.
  LoadMetaLanguages.loadAll(workspaceDir)

  // Create a PIE instance.
  val pieBuilder = PieBuilderImpl()
  pieBuilder.withGuiceTaskDefs(injector) // Use task definitions from the Spoofax-PIE facade.
  pieBuilder.withLMDBStore(File("target/lmdb")) // Use LMDB persistent storage.
  pieBuilder.withLogger(StreamLogger.verbose()) // Verbose logging for testing purposes.
  val pie = pieBuilder.build()

  // Run workspace build incrementally, with top-down executor (since we do not have a list of changed files here).
  val session = pie.topDownExecutor.newSession()
  session.requireInitial(spoofaxPipeline.workspace(workspaceDir.path))
  // Get result of document file and print it.
  val result = session.requireInitial(spoofaxPipeline.document(documentFile.path, containerDir.path, workspaceDir.path))
  println(result)

  // Finally, we clean up our resources.
  pie.close()
}

/** Spoofax Core (legacy) module to disable IDE editor support, since we are running headless. */
class SpoofaxCoreModule : org.metaborg.spoofax.core.SpoofaxModule() {
  override fun bindEditor() {
    bind(IEditorRegistry::class.java).to(NullEditorRegistry::class.java).`in`(Singleton::class.java)
  }
}
