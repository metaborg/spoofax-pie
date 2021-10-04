package mb.codecompletion.bench

import mb.codecompletion.bench.di.TigerBenchLanguageComponent
import mb.pie.api.ExecContext
import mb.pie.api.None
import mb.pie.api.TaskDef
import mb.pie.dagger.PieComponent
import mb.tiger.TigerResourcesComponent
import javax.inject.Inject

class CompletenessTest(
//  val languageComponent: TigerBenchLanguageComponent,
//  val pieComponent: PieComponent,
//  val resourcesComponent: TigerResourcesComponent,
) {
//  fun run() {
//    val session = pieComponent.newSession()
//
//    val result =
//      session.requireWithoutObserving(languageComponent.testTask.createTask(None.instance))
//    println("Read: $result")
//    // For a given project, parse files to AST
//    // Perform whole-project analysis
//
//    // Take one of the files
//    // Remove an AST node
//    // For each ast with a placeholder and the expected ast
//    // Perform code completion
//    // Assert that the expected completion is in the result set
//    // Measure and log the time to get completions
//  }

  class TestTask @Inject constructor(
    val resourcesComponent: TigerResourcesComponent
  ) : TaskDef<None, String> {
    override fun getId(): String = TestTask::class.java.name
    override fun exec(ctx: ExecContext, input: None): String {
      val resource = requireNotNull(resourcesComponent.definitionDirectory.appendAsRelativePath("testfile/test1.tig").asLocalResource())
      ctx.require(resource)
      val content = resource.readString()
      return content
    }

  }
}
