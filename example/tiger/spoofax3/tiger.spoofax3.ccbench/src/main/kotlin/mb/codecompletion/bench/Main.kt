package mb.codecompletion.bench

import mb.codecompletion.bench.di.BenchLoggerModule
import mb.codecompletion.bench.di.DaggerBenchLoggerComponent
import mb.codecompletion.bench.di.DaggerBenchPlatformComponent
import mb.codecompletion.bench.di.DaggerBenchResourceServiceComponent
import mb.codecompletion.bench.di.DaggerTigerBenchLanguageComponent
import mb.codecompletion.bench.di.TigerBenchModule
import mb.common.region.Region
import mb.pie.api.None
import mb.pie.dagger.DaggerRootPieComponent
import mb.pie.dagger.RootPieModule
import mb.pie.runtime.PieBuilderImpl
import mb.resource.dagger.RootResourceServiceModule
import mb.statix.codecompletion.pie.CodeCompletionTaskDef
import mb.tiger.DaggerTigerResourcesComponent
import mb.tiger.TigerModule

fun main(args: Array<String>) {

  // Platform:
  val loggerComponent = DaggerBenchLoggerComponent.builder()
    .benchLoggerModule(BenchLoggerModule())
    .build()

  // Language:
  val resourcesComponent = DaggerTigerResourcesComponent.create()
  // Platform:
  val resourceServiceModule = RootResourceServiceModule()
    .addRegistriesFrom(resourcesComponent)
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
    .tigerBenchModule(TigerBenchModule())
    .build()

  // PIE
  val pieModule = RootPieModule({ PieBuilderImpl() }, languageComponent)
  val pieComponent = DaggerRootPieComponent.builder()
    .rootPieModule(pieModule)
    .loggerComponent(loggerComponent)
    .resourceServiceComponent(resourceServiceComponent)
    .build()

  pieModule.addTaskDef(
    languageComponent.testTask
  )

  pieComponent.newSession().use { session ->
    val resource = requireNotNull(resourcesComponent.definitionDirectory.appendAsRelativePath("testfile/test1.tig").asLocalResource())

    val result = session.requireWithoutObserving(languageComponent.tigerCodeCompletionTaskDef.createTask(
      CodeCompletionTaskDef.Args(
        Region.atOffset(1),
        resource.key,
        null
      )
    ))
//    val result = session.requireWithoutObserving(languageComponent.testTask.createTask(None.instance))
    println("Result: $result")
  }
  println("Done!")
}
