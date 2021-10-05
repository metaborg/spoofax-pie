package mb.codecompletion.bench.di

import dagger.Component
import mb.codecompletion.bench.Benchmarker
import mb.codecompletion.bench.PrepareBenchmarkTask
import mb.codecompletion.bench.PrepareTestFileTask
import mb.codecompletion.bench.RunBenchmarkTask
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
    TigerBenchModule::class
  ],
  dependencies = [
    BenchLoggerComponent::class,
    TigerResourcesComponent::class,
    ResourceServiceComponent::class,
    BenchPlatformComponent::class
  ]
)
interface TigerBenchLanguageComponent: BenchLanguageComponent, TigerComponent {
//    /** Runs the benchmarks. */
//    val benchmarker: Benchmarker

    val termFactory: ITermFactory
//    val prepareTestFileTask: PrepareTestFileTask
    val runBenchmarkTask: RunBenchmarkTask
    val prepareBenchmarkTask: PrepareBenchmarkTask
}
