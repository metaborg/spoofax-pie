package mb.codecompletion.bench

import mb.codecompletion.bench.di.TigerBenchLanguageComponent
import mb.pie.api.ExecContext
import mb.pie.api.None
import mb.pie.api.TaskDef
import mb.pie.dagger.PieComponent
import mb.tiger.TigerResourcesComponent
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.streams.asSequence

///**
// * Benchmarks a series of code completion invocations,
// * and gathers the results in a CSV file.
// */
//class BenchmarkTask @Inject constructor(
//    val codeCompleteBenchTask: CodeCompleteBenchTask,
//  ) : TaskDef<BenchmarkTask.Input, String> {
//
//    /**
//     * The task input.
//     *
//     * @property inputDir The directory from which to execute all tests.
//     */
//    data class Input(
//        val projectDir: Path,
//        val inputDir: Path,
//    ) : Serializable
//
//    override fun getId(): String = BenchmarkTask::class.java.name
//    override fun exec(ctx: ExecContext, input: Input): String {
//        ctx.require(input.inputDir)
//        val files = Files.list(input.inputDir).asSequence().toList()
//        for (file in files) {
//            //ctx.require(file)
//        val resource = requireNotNull(resourcesComponent.definitionDirectory.appendAsRelativePath("testfile/test1.tig").asLocalResource())
//      ctx.require(resource)
//      val content = resource.readString()
//      return content
//    }
//
//  }
//}
