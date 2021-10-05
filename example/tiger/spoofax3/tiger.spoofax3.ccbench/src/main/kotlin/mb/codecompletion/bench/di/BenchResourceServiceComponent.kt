package mb.codecompletion.bench.di

import dagger.Component
import mb.resource.dagger.ResourceServiceScope
import mb.resource.dagger.RootResourceServiceComponent
import mb.resource.dagger.RootResourceServiceModule

@ResourceServiceScope
@Component(
    modules = [
        RootResourceServiceModule::class,
    ],
    dependencies = [
        BenchLoggerComponent::class
    ]
)
interface BenchResourceServiceComponent : RootResourceServiceComponent {

}
