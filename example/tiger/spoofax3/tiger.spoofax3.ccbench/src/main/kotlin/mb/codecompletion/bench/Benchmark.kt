package mb.codecompletion.bench

import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

/**
 * A single benchmark.
 *
 * @property filename the input filename
 * @property inputText the benchmark input, code in the target language with a single placeholder
 * @property placeholderOffset the zero-based placeholder offset in [inputText], in characters
 * @property expectedTerm the term expected for the placeholder
 */
data class Benchmark(
    val filename: String,
    val inputText: String,
    val placeholderOffset: Int,
    val expectedTerm: IStrategoTerm,
): Serializable

/**
 * The data object that is stored in a YAML file.
 */
data class BenchmarkData(
    val inputFile: String,
    val placeholderOffset: Int,
    val expectedTermFile: String,
)
