package mb.codecompletion.bench.di

import dagger.Module
import dagger.Provides
import mb.resource.text.TextResourceRegistry
import org.spoofax.terms.io.PrettyTextTermWriter
import org.spoofax.terms.io.SimpleTextTermWriter

@Module
class TigerBenchModule(
    private val textResourceRegistry: TextResourceRegistry
) {

    @Provides fun provideTextResourceRegistry(): TextResourceRegistry = this.textResourceRegistry

    @Provides fun providePrettyTextTermWriter(): PrettyTextTermWriter =
        PrettyTextTermWriter()

    @Provides fun provideSimpleTextTermWriter(): SimpleTextTermWriter =
        SimpleTextTermWriter()

}
