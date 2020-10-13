package mb.spoofax.compiler.gradle

import org.gradle.api.logging.Logger

class PieLogger(private val logger: Logger) : mb.pie.api.Logger {
  override fun error(message: String, throwable: Throwable?) {
    logger.error(message, throwable)
  }

  override fun warn(message: String, throwable: Throwable?) {
    logger.warn(message, throwable)
  }

  override fun info(message: String) {
    logger.info(message)
  }

  override fun debug(message: String) {
    logger.debug(message)
  }

  override fun trace(message: String) {
    logger.trace(message)
  }
}
