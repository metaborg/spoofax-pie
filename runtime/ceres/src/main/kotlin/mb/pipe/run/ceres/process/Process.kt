package mb.pipe.run.ceres.process

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.BuildException
import mb.ceres.Builder
import mb.ceres.Out
import mb.log.Logger
import java.io.IOException

class Execute @Inject constructor(log: Logger) : Builder<ArrayList<String>, Execute.Output> {
  companion object {
    val id = "execute"
  }

  val log: Logger = log.forContext(Execute::class.java)

  data class Output(val stdout: String, val stderr: String) : Out

  override val id = Companion.id
  override fun BuildContext.build(input: ArrayList<String>): Output {
    try {
      val proc = ProcessBuilder(input)
        .directory(null)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

      proc.waitFor()
      val stdout = proc.inputStream.bufferedReader().readText()
      System.out.print(stdout)
      val stderr = proc.errorStream.bufferedReader().readText()
      System.err.print(stderr)
      return Output(stdout, stderr)
    } catch(e: IOException) {
      throw BuildException("Failed to execute '${input.joinToString(" ")}'", e)
    }
  }
}

fun BuildContext.execute(input: ArrayList<String>) = requireOutput(Execute::class.java, input)