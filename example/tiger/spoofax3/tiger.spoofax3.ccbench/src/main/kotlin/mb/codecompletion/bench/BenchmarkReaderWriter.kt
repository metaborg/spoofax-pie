package mb.codecompletion.bench

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mb.codecompletion.bench.utils.withExtension
import mb.codecompletion.bench.utils.withName
import org.spoofax.terms.io.TermReader
import org.spoofax.terms.io.TermWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * The [Benchmark] reader/writer.
 */
class BenchmarkReaderWriter(
    private val termReader: TermReader,
    private val termWriter: TermWriter,
) {
    /**
     * Writes the benchmark to the specified writer.
     *
     * @param benchmark the benchmark to write
     * @param writer the writer to write to
     * @param filename the filename of the test, such as {@code test.tig}
     * @param directory the directory where to write extra files for the benchmark
     */
    fun write(benchmark: Benchmark, writer: Writer, filename: String, directory: Path) {
        val inputFile = directory.resolve(filename)
        inputFile.writeText(benchmark.inputText)
        val expectedTermFile = directory.resolve(filename).withName { "$it-expected" }.withExtension { "$it.aterm" }
        termWriter.writeToPath(benchmark.expectedTerm, expectedTermFile)

        val mapper = createObjectMapper()
        mapper.writeValue(writer, BenchmarkData(
            inputFile.toString(),
            benchmark.placeholderOffset,
            expectedTermFile.toString()
        ))
    }

    /**
     * Writes the benchmark to the specified stream.
     *
     * @param benchmark the benchmark to write
     * @param stream the output stream to write to
     * @param filename the filename of the test, such as {@code test.tig}
     * @param directory the directory where to write extra files for the benchmark
     */
    fun write(benchmark: Benchmark, stream: OutputStream, filename: String, directory: Path) =
        stream.bufferedWriter().use { writer -> write(benchmark, writer, filename, directory) }

    /**
     * Writes the benchmark to the specified file.
     *
     * @param benchmark the benchmark to write
     * @param file the file to write to
     * @param filename the filename of the test, such as {@code test.tig}
     * @param directory the directory where to write extra files for the benchmark
     */
    fun write(benchmark: Benchmark, file: Path, filename: String, directory: Path) =
        file.bufferedWriter().use { writer -> write(benchmark, writer, filename, directory) }

    /**
     * Reads a benchmark from the specified reader.
     *
     * @param reader the reader to read from
     * @param filename the filename of the test, such as {@code test.tig}
     * @param directory the directory where to read extra files for the benchmark
     * @return the read benchmark
     */
    fun read(reader: Reader, filename: String, directory: Path): Benchmark {
        val mapper = createObjectMapper()
        val data = mapper.readValue(reader, BenchmarkData::class.java)

        val inputText = directory.resolve(data.inputFile).readText()
        val expectedTerm = termReader.readFromPath(directory.resolve(data.expectedTermFile))

        return Benchmark(
            filename,
            inputText,
            data.placeholderOffset,
            expectedTerm,
        )
    }

    /**
     * Reads a benchmark from the specified input stream.
     *
     * @param stream the input stream to read from
     * @param filename the filename of the test, such as {@code test.tig}
     * @param directory the directory where to read extra files for the benchmark
     * @return the read benchmark
     */
    fun read(stream: InputStream, filename: String, directory: Path): Benchmark =
        stream.bufferedReader().use { reader -> read(reader, filename, directory) }

    /**
     * Reads a benchmark from the specified file.
     *
     * @param file the file to read from
     * @param filename the filename of the test, such as {@code test.tig}
     * @param directory the directory where to read extra files for the benchmark
     * @return the read benchmark
     */
    fun read(file: Path, filename: String, directory: Path): Benchmark =
        file.bufferedReader().use { reader -> read(reader, filename, directory) }

    /**
     * Creates an object mapper for parsing YAML using Kotlin.
     *
     * @return the created object mapper
     */
    private fun createObjectMapper(): ObjectMapper {
        val mapper = ObjectMapper(YAMLFactory())
        val kotlinModule = KotlinModule.Builder().build()
        mapper.registerModule(kotlinModule)
        return mapper
    }
}
