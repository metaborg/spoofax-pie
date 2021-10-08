package mb.ccbench.webdsl

import mb.ccbench.di.BenchLoggerModule
import mb.ccbench.di.DaggerBenchLoggerComponent
import mb.ccbench.di.DaggerBenchPlatformComponent
import mb.ccbench.di.DaggerBenchResourceServiceComponent
import mb.ccbench.webdsl.DaggerWebDSLBenchComponent
import mb.ccbench.webdsl.DaggerWebDSLBenchLanguageComponent
import mb.ccbench.webdsl.WebDSLBenchModule
import mb.pie.dagger.DaggerRootPieComponent
import mb.pie.dagger.RootPieModule
import mb.pie.runtime.PieBuilderImpl
import mb.resource.dagger.RootResourceServiceModule
import mb.resource.text.TextResourceRegistry
import mb.webdsl.DaggerWebDSLResourcesComponent
import mb.webdsl.WebDSLModule

fun main(args: Array<String>) {

    // Platform:
    val loggerComponent = DaggerBenchLoggerComponent.builder()
        .benchLoggerModule(BenchLoggerModule())
        .build()

    // Language:
    val resourcesComponent = DaggerWebDSLResourcesComponent.create()
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
    val languageComponent = DaggerWebDSLBenchLanguageComponent.builder()
        .webDSLModule(WebDSLModule())
        .webDSLBenchModule(WebDSLBenchModule(textResourceRegistry))
        .benchLoggerComponent(loggerComponent)
        .resourceServiceComponent(resourceServiceComponent)
        .benchPlatformComponent(platformComponent)
        .webDSLResourcesComponent(resourcesComponent)
        .build()

    // PIE
    val pieModule = RootPieModule({ PieBuilderImpl() }, languageComponent)
        //.withTracerFactory(::LoggingTracer) // Only for debugging, performance overhead
    val pieComponent = DaggerRootPieComponent.builder()
        .rootPieModule(pieModule)
        .loggerComponent(loggerComponent)
        .resourceServiceComponent(resourceServiceComponent)
        .build()

    pieModule.addTaskDefs(
        languageComponent.runBenchmarkTask,
        languageComponent.prepareBenchmarkTask,
    )

    val benchComponent = DaggerWebDSLBenchComponent.builder()
        .benchLoggerComponent(loggerComponent)
        .webDSLResourcesComponent(resourcesComponent)
        .benchResourceServiceComponent(resourceServiceComponent)
        .benchPlatformComponent(platformComponent)
        .webDSLBenchLanguageComponent(languageComponent)
        .rootPieComponent(pieComponent)
        .build()

    benchComponent.mainCommand.main(args)
}

