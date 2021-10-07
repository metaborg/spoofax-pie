package mb.codecompletion.bench.di

import dagger.Component
import mb.codecompletion.bench.PrepareBenchmarkTask
import mb.codecompletion.bench.RunBenchmarkTask
import mb.codecompletion.bench.TigerPrepareBenchmarkTask
import mb.codecompletion.bench.TigerRunBenchmarkTask
import mb.resource.dagger.ResourceServiceComponent
import mb.tiger.TigerComponent
import mb.tiger.TigerModule
import mb.tiger.TigerResourcesComponent
import mb.tiger.TigerScope
import org.spoofax.interpreter.terms.ITermFactory

@TigerScope
@Component(
  modules = [
    TigerModule::class,
    TigerBenchModule::class,
  ],
  dependencies = [
    BenchLoggerComponent::class,
    TigerResourcesComponent::class,
    ResourceServiceComponent::class,
    BenchPlatformComponent::class,
  ]
)
interface TigerBenchLanguageComponent: TigerComponent {
    val termFactory: ITermFactory
    val runBenchmarkTask: TigerRunBenchmarkTask
    val prepareBenchmarkTask: TigerPrepareBenchmarkTask
}
