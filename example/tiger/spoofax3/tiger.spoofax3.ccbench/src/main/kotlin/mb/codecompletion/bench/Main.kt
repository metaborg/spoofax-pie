package mb.codecompletion.bench

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import mb.codecompletion.bench.di.BenchLoggerModule
import mb.codecompletion.bench.di.BenchResourceServiceModule
import mb.codecompletion.bench.di.DaggerBenchComponent
import mb.codecompletion.bench.di.DaggerBenchLoggerComponent
import mb.codecompletion.bench.di.DaggerBenchPlatformComponent
import mb.codecompletion.bench.di.DaggerBenchResourceServiceComponent
import mb.codecompletion.bench.di.DaggerTigerBenchLanguageComponent
import mb.codecompletion.bench.di.TigerBenchLanguageComponent
import mb.codecompletion.bench.di.TigerBenchModule
import mb.codecompletion.bench.utils.withExtension
import mb.common.region.Region
import mb.pie.api.Pie
import mb.pie.dagger.DaggerRootPieComponent
import mb.pie.dagger.PieComponent
import mb.pie.dagger.RootPieModule
import mb.pie.runtime.PieBuilderImpl
import mb.pie.runtime.tracer.LoggingTracer
import mb.resource.dagger.RootResourceServiceModule
import mb.resource.text.TextResourceRegistry
import mb.statix.codecompletion.pie.CodeCompletionTaskDef
import mb.tiger.DaggerTigerResourcesComponent
import mb.tiger.TigerModule
import mb.tiger.TigerResourcesComponent
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

fun main(args: Array<String>) {

    // Platform:
    val loggerComponent = DaggerBenchLoggerComponent.builder()
        .benchLoggerModule(BenchLoggerModule())
        .build()

    // Language:
    val resourcesComponent = DaggerTigerResourcesComponent.create()
    // Platform:
    val textResourceRegistry = TextResourceRegistry()
    val resourceServiceModule = RootResourceServiceModule()
        .addRegistriesFrom(resourcesComponent)
        .addRegistry(textResourceRegistry)
    val resourceServiceComponent = DaggerBenchResourceServiceComponent.builder()
        .benchLoggerComponent(loggerComponent)
        .rootResourceServiceModule(resourceServiceModule)
        .build()
    val platformComponent = DaggerBenchPlatformComponent.builder()
        .benchLoggerComponent(loggerComponent)
        .benchResourceServiceComponent(resourceServiceComponent)
        .build()

    // Language:
    val languageComponent = DaggerTigerBenchLanguageComponent.builder()
        .benchLoggerComponent(loggerComponent)
        .tigerResourcesComponent(resourcesComponent)
        .resourceServiceComponent(resourceServiceComponent)
        .benchPlatformComponent(platformComponent)
        .tigerModule(TigerModule())
        .tigerBenchModule(TigerBenchModule(textResourceRegistry))
        .build()

    // PIE
    val pieModule = RootPieModule({ PieBuilderImpl() }, languageComponent)
        .withTracerFactory(::LoggingTracer) // Only for debugging, performance overhead
    val pieComponent = DaggerRootPieComponent.builder()
        .rootPieModule(pieModule)
        .loggerComponent(loggerComponent)
        .resourceServiceComponent(resourceServiceComponent)
        .build()

    pieModule.addTaskDefs(
        languageComponent.runBenchmarkTask,
        languageComponent.prepareBenchmarkTask,
    )

    val benchComponent = DaggerBenchComponent.builder()
        .benchLoggerComponent(loggerComponent)
        .tigerResourcesComponent(resourcesComponent)
        .benchResourceServiceComponent(resourceServiceComponent)
        .benchPlatformComponent(platformComponent)
        .tigerBenchLanguageComponent(languageComponent)
        .rootPieComponent(pieComponent)
        .build()

    benchComponent.mainCommand.main(args)

//    val baseDir = Path.of("/Users/daniel/repos/spoofax3/devenv-cc-sept-21-2/spoofax.pie/example/tiger/spoofax3/")
//    val projectDir = Path.of("tiger/")
//    val outputDir = Path.of("")
//    val testCaseDir = Path.of("tiger-tests/")

//    // Preparation
//    val benchmark = BenchmarkBuilder(pieComponent.pie, languageComponent.prepareBenchmarkTask).build(
//        "Tiger",
//        baseDir,
//        projectDir,
//        ".tig",
//        outputDir,
//        testCaseDir,
//    )

//    // Running
//    val benchmark = Benchmark.read(baseDir.resolve(outputDir).resolve("Tiger.yml"))
//    val results = BenchmarkRunner(pieComponent.pie, languageComponent.runBenchmarkTask, languageComponent.termFactory).run(
//        benchmark, baseDir
//    )
//    val resultsFile = baseDir.resolve(outputDir).resolve("Tiger.csv")
//    results.writeAsCsv(resultsFile)

//    println("Done!")
}

class MainCommand @Inject constructor(
    private val prepareBenchmarkCommand: PrepareBenchmarkCommand,
    private val runBenchmarkCommand: RunBenchmarkCommand,
): CliktCommand() {
    init {
        subcommands(
            prepareBenchmarkCommand,
            runBenchmarkCommand,
        )
    }

    override fun run() = Unit
}

/**
 * Command that prepares tests.
 */
class PrepareBenchmarkCommand @Inject constructor(
    private val benchmarkBuilder: BenchmarkBuilder,
) : CliktCommand(name = "prepare") {
    val name: String by argument(help = "Name of the benchmark")
    val ext: String by option("-e", "--ext", help = "File extension").required()

    val baseDir: Path? by option("-d", "--baseDir", help = "Base directory").path(mustExist = true, canBeFile = false, canBeDir = true)
    val projectDir: Path by option("-p", "--project", help = "Project directory").path(mustExist = true, canBeFile = false, canBeDir = true).required()
    val outputDir: Path? by option("-o", "--output", help = "Output directory").path(mustExist = true, canBeFile = false, canBeDir = true)
    val testCaseDir: Path? by option("-t", "--testcaseDir", help = "Test case directory").path(mustExist = true, canBeFile = false, canBeDir = true)

    override fun run() {
        val actualBaseDir = baseDir ?: Path.of("")
        val actualProjectDir = projectDir
        val actualOutputDir = outputDir ?: Path.of("output/")
        val actualTestCaseDir = testCaseDir ?: actualOutputDir.resolve("testcases/")
        benchmarkBuilder.build(
            name,
            actualBaseDir,
            actualProjectDir,
            ".$ext",
            actualOutputDir,
            actualTestCaseDir,
        )
        println("Done!")
    }
}

/**
 * Command that runs tests.
 */
class RunBenchmarkCommand @Inject constructor(
    private val benchmarkRunner: BenchmarkRunner,
) : CliktCommand(name = "run") {
    val name: String by argument(help = "Name of the benchmark")

    val baseDir: Path? by option("-d", "--baseDir", help = "Base directory").path(mustExist = true, canBeFile = false, canBeDir = true)
    val inputFile: Path? by option("-i", "--input", help = "Benchmark YAML file").path(mustExist = true, canBeFile = true, canBeDir = false)
    val outputDir: Path? by option("-o", "--output", help = "Output directory").path(mustExist = true, canBeFile = false, canBeDir = true)

    override fun run() {
        val actualBaseDir = baseDir ?: Path.of("")
        val actualInputFile = inputFile ?: Path.of("$name.yaml")
        val actualOutputDir = outputDir ?: Path.of("output/")
        val actualOutputFile = actualBaseDir.resolve(actualOutputDir).resolve(actualInputFile.withExtension(".csv").fileName)

        val benchmark = Benchmark.read(actualInputFile)
        val results = benchmarkRunner.run(
            benchmark, actualBaseDir
        )
        results.writeAsCsv(actualOutputFile)
        println("Done!")
    }
}
