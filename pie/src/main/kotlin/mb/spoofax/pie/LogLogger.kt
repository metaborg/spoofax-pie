package mb.spoofax.pie

import mb.log.*
import mb.pie.api.PieBuilder
import mb.pie.runtime.logger.StreamLogger
import java.io.PrintWriter

fun PieBuilder.withMbLogger(logger: Logger): PieBuilder {
  this.withLogger(LogLogger(logger))
  return this
}

class LogLogger constructor(
  logger: Logger
) : StreamLogger(
  PrintWriter(LoggingOutputStream(logger, Level.Error), true),
  PrintWriter(LoggingOutputStream(logger, Level.Warn), true),
  PrintWriter(LoggingOutputStream(logger, Level.Info), true),
  PrintWriter(LoggingOutputStream(logger, Level.Debug), true),
  PrintWriter(LoggingOutputStream(logger, Level.Trace), true)
)
