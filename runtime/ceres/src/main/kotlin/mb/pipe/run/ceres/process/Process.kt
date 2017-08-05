package mb.pipe.run.ceres.process

import mb.ceres.BuildException
import mb.pipe.run.ceres.util.Tuple2
import mb.pipe.run.ceres.util.tuple
import java.io.IOException

fun execute(arguments: ArrayList<String>): Tuple2<String, String> {
  try {
    val proc = ProcessBuilder(arguments)
            .directory(null)
            .inheritIO()
            //.redirectOutput(ProcessBuilder.Redirect.PIPE)
            //.redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
    proc.waitFor()
    val stdout = proc.inputStream.bufferedReader().readText()
    System.out.print(stdout)
    val stderr = proc.errorStream.bufferedReader().readText()
    System.err.print(stderr)
    return tuple(stdout, stderr)
  } catch(e: IOException) {
    throw BuildException("Failed to execute '${arguments.joinToString(" ")}'", e)
  }
}
