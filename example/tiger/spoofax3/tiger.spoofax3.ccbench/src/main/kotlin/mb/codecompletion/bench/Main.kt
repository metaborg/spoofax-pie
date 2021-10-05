package mb.codecompletion.bench

import mb.codecompletion.bench.di.BenchLoggerModule
import mb.codecompletion.bench.di.BenchResourceServiceModule
import mb.codecompletion.bench.di.DaggerBenchLoggerComponent
import mb.codecompletion.bench.di.DaggerBenchPlatformComponent
import mb.codecompletion.bench.di.DaggerBenchResourceServiceComponent
import mb.codecompletion.bench.di.DaggerTigerBenchLanguageComponent
import mb.codecompletion.bench.di.TigerBenchLanguageComponent
import mb.codecompletion.bench.di.TigerBenchModule
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

    val baseDir = Path.of("/Users/daniel/repos/spoofax3/devenv-cc-sept-21-2/spoofax.pie/example/tiger/spoofax3/")
    val projectDir = Path.of("tiger/")
    val outputDir = Path.of("")
    val testCaseDir = Path.of("tiger-tests/")

//    // Preparation
//    val benchmark = BenchmarkBuilder(pieComponent.pie, languageComponent.prepareBenchmarkTask).build(
//        "Tiger",
//        baseDir,
//        projectDir,
//        ".tig",
//        outputDir,
//        testCaseDir,
//    )

    // Running
    val benchmark = Benchmark.read(baseDir.resolve(outputDir).resolve("Tiger.yml"))
    val results = BenchmarkRunner(pieComponent.pie, languageComponent.runBenchmarkTask, languageComponent.termFactory).run(
        benchmark, baseDir
    )

    val resultsFile = baseDir.resolve(outputDir).resolve("Tiger.csv")
    results.writeAsCsv(resultsFile)

    println("Done!")
}
