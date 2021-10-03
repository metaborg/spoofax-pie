package mb.codecompletion.bench

import mb.codecompletion.bench.di.BenchLoggerModule
import mb.codecompletion.bench.di.DaggerBenchLoggerComponent
import mb.codecompletion.bench.di.DaggerBenchPlatformComponent
import mb.codecompletion.bench.di.DaggerBenchResourceServiceComponent
import mb.codecompletion.bench.di.DaggerTigerBenchLanguageComponent
import mb.codecompletion.bench.di.TigerBenchLanguageComponent
import mb.codecompletion.bench.di.TigerBenchModule
import mb.common.region.Region
import mb.pie.api.None
import mb.pie.dagger.DaggerRootPieComponent
import mb.pie.dagger.PieComponent
import mb.pie.dagger.RootPieModule
import mb.pie.runtime.PieBuilderImpl
import mb.resource.dagger.RootResourceServiceModule
import mb.spoofax.core.language.LanguageComponent
import mb.statix.codecompletion.pie.CodeCompletionTaskDef
import mb.tiger.DaggerTigerResourcesComponent
import mb.tiger.TigerModule
import mb.tiger.TigerResourcesComponent
import java.nio.file.Paths

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
    languageComponent.prepareTestFileTask
  )

  pieComponent.newSession().use { session ->
    session.requireWithoutObserving(languageComponent.prepareTestFileTask.createTask(
      PrepareTestFileTask.Input(
        Paths.get("/Users/daniel/repos/spoofax3/devenv-cc-sept-21/spoofax.pie/example/tiger/spoofax3/tiger.spoofax3.ccbench/testfiles"),
        Paths.get("/Users/daniel/repos/spoofax3/devenv-cc-sept-21/spoofax.pie/example/tiger/spoofax3/tiger.spoofax3.ccbench/testfiles/test2.tig"),
        Paths.get("/Users/daniel/repos/spoofax3/devenv-cc-sept-21/spoofax.pie/example/tiger/spoofax3/tiger.spoofax3.ccbench/testfiles/results/"),
      )
    ))
//    val result = session.requireWithoutObserving(languageComponent.testTask.createTask(None.instance))

  }
  println("Done!")
}

fun testCodeCompletion(pieComponent: PieComponent, resourcesComponent: TigerResourcesComponent, languageComponent: TigerBenchLanguageComponent) {
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
    println("Result: ${result.get()!!.proposals}")
  }
}
