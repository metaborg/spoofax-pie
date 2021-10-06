package mb.codecompletion.bench

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mb.codecompletion.bench.format.PathDeserializer
import mb.codecompletion.bench.format.PathSerializer
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Serializable
import java.io.Writer
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter

/**
 * A benchmark test suite.
 *
 * @property name the name of the benchmark
 * @property testCaseDirectory the directory where the test case files are found, relative to the benchmark data file
 * @property testCases the test cases
 */
data class Benchmark(
    val name: String,
    val testCaseDirectory: Path,
    val testCases: List<TestCase>,
): Serializable {

    companion object {
        /**
         * Writes the benchmark to the specified writer.
         *
         * @param benchmark the benchmark to write
         * @param writer the writer to write to
         */
        fun write(benchmark: Benchmark, writer: Writer) {
            val mapper = createObjectMapper()
            mapper.writeValue(writer, benchmark)
        }

        /**
         * Writes the benchmark to the specified stream.
         *
         * @param benchmark the benchmark to write
         * @param stream the output stream to write to
         */
        fun write(benchmark: Benchmark, stream: OutputStream) =
            stream.bufferedWriter().use { writer -> write(benchmark, writer) }

        /**
         * Writes the benchmark to the specified file.
         *
         * @param benchmark the benchmark to write
         * @param file the file to write to
         */
        fun write(benchmark: Benchmark, file: Path) =
            file.bufferedWriter().use { writer -> write(benchmark, writer) }

        /**
         * Reads a benchmark from the specified reader.
         *
         * @param reader the reader to read from
         * @return the read benchmark
         */
        fun read(reader: Reader): Benchmark {
            val mapper = createObjectMapper()
            return mapper.readValue(reader, Benchmark::class.java)
        }

        /**
         * Reads a benchmark from the specified input stream.
         *
         * @param stream the input stream to read from
         * @return the read benchmark
         */
        fun read(stream: InputStream): Benchmark =
            stream.bufferedReader().use { reader -> read(reader) }

        /**
         * Reads a benchmark from the specified file.
         *
         * @param file the file to read from
         * @return the read benchmark
         */
        fun read(file: Path): Benchmark =
            file.bufferedReader().use { reader -> read(reader) }

        /**
         * Creates an object mapper for parsing YAML using Kotlin.
         *
         * @return the created object mapper
         */
        private fun createObjectMapper(): ObjectMapper {
            val mapper = ObjectMapper(YAMLFactory())
            val kotlinModule = KotlinModule.Builder().build()
            mapper.registerModule(kotlinModule)
            val customModule = SimpleModule()
                .addSerializer(Path::class.java, PathSerializer())
            mapper.registerModule(customModule)
            return mapper
        }
    }
}

/**
 * A test case in a benchmark.
 *
 * @property name the name of the test case
 * @property file the file being replaced in this test case, relative to the project directory
 * @property inputFile the input file for this test case, relative to the test cases directory
 * @property placeholderOffset the zero-based offset of the placeholder in the [inputFile]
 * @property expectedFile the file with the extected term for this test case, relative to the test cases directory
 */
data class TestCase(
    val name: String,
    val file: Path,
    val inputFile: Path,
    val placeholderOffset: Int,
    val expectedFile: Path,
): Serializable


/**
 * A single benchmark case.
 *
 * @property filename the input filename
 * @property inputText the case input, code in the target language with a single placeholder
 * @property placeholderOffset the zero-based placeholder offset in [inputText], in characters
 * @property expectedTerm the term expected for the placeholder
 */
@Deprecated("")
data class BenchmarkCase(
    val filename: String,
    val inputText: String,
    val placeholderOffset: Int,
    val expectedTerm: IStrategoTerm,
): Serializable {
    /**
     * The data object that is stored in a YAML file.
     */
    @Deprecated("")
    data class Data(
        val inputFile: String,
        val placeholderOffset: Int,
        val expectedTermFile: String,
    )

}

