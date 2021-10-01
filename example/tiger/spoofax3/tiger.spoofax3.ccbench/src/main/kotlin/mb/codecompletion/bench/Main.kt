package mb.codecompletion.bench

import mb.log.dagger.DaggerLoggerComponent
import mb.log.dagger.LoggerModule
import mb.log.slf4j.SLF4JLoggerFactory
import mb.pie.api.TaskDef
import mb.pie.dagger.DaggerRootPieComponent
import mb.pie.dagger.RootPieModule
import mb.pie.runtime.PieBuilderImpl
import mb.resource.dagger.DaggerRootResourceServiceComponent
import mb.resource.dagger.RootResourceServiceModule
import mb.tiger.DaggerTigerComponent
import mb.tiger.DaggerTigerResourcesComponent

fun main(args: Array<String>) {

  // Language specific:
  val resourcesComponent = DaggerTigerResourcesComponent.create()

  val loggerComponent = DaggerLoggerComponent.builder()
    .loggerModule(LoggerModule(SLF4JLoggerFactory()))
    .build()
  val resourceServiceModule = RootResourceServiceModule()
    .addRegistriesFrom(resourcesComponent)
  val resourceServiceComponent = DaggerRootResourceServiceComponent.builder()
    .loggerComponent(loggerComponent)
    .rootResourceServiceModule(resourceServiceModule)
    .build()
  val pieModule = RootPieModule { PieBuilderImpl() }
  val pieComponent = DaggerRootPieComponent.builder()
    .rootPieModule(pieModule)
    .loggerComponent(loggerComponent)
    .resourceServiceComponent(resourceServiceComponent)
    .build()

  // Test specific:
  val testComponent = DaggerTestComponent.builder()
    .loggerComponent(loggerComponent)
    .resourceServiceComponent(resourceServiceComponent)
    .pieComponent(pieComponent)
    .build()

  // Language specific:
  val languageComponent = DaggerTigerComponent.builder()
    .loggerComponent(loggerComponent)
    .tigerResourcesComponent(resourcesComponent)
    .resourceServiceComponent(resourceServiceComponent)
    .platformComponent(testComponent)
    .build()

  pieModule.addTaskDef(
    CompletenessTest.TestTask(resourcesComponent)
  )
  val completenessTest = CompletenessTest(testComponent, pieComponent, languageComponent, resourcesComponent)
  completenessTest.run()
  println("Done!")
}
