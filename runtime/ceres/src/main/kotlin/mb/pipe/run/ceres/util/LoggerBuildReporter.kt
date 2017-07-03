package mb.pipe.run.ceres.util

import com.google.inject.Inject
import mb.ceres.impl.StreamBuildReporter
import mb.pipe.run.core.log.Level
import mb.pipe.run.core.log.Logger
import mb.pipe.run.core.log.LoggingOutputStream

class LoggerBuildReporter @Inject constructor(logger: Logger)
  : StreamBuildReporter(LoggingOutputStream(logger.forContext("Build log"), Level.Info)) {

}