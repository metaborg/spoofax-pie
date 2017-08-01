package mb.pipe.run.ceres.util

import com.google.inject.Inject
import mb.ceres.impl.StreamBuildReporter
import mb.log.Level
import mb.log.Logger
import mb.log.LoggingOutputStream

class LoggerBuildReporter @Inject constructor(logger: Logger)
  : StreamBuildReporter(LoggingOutputStream(logger.forContext("Build log"), Level.Info), LoggingOutputStream(logger.forContext("Build log"), Level.Trace))