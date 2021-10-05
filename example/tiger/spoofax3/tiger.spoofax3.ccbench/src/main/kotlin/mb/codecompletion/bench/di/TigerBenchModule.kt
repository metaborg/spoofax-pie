package mb.codecompletion.bench.di

import dagger.Module
import dagger.Provides
import mb.codecompletion.bench.BenchmarkReaderWriter
import mb.resource.text.TextResourceRegistry
import org.spoofax.interpreter.terms.ITermFactory
import org.spoofax.terms.io.SimpleTextTermWriter
import org.spoofax.terms.io.binary.TermReader

@Module
class TigerBenchModule(
    private val textResourceRegistry: TextResourceRegistry
) {
    @Provides fun provideBenchmarkReaderWriter(termFactory: ITermFactory): BenchmarkReaderWriter {
        return BenchmarkReaderWriter(TermReader(termFactory), SimpleTextTermWriter())
    }

    @Provides fun provideTextResourceRegistry(): TextResourceRegistry {
        return textResourceRegistry
    }
}
