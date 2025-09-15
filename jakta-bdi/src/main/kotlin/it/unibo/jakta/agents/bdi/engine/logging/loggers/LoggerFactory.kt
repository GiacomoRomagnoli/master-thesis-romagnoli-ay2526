package it.unibo.jakta.agents.bdi.engine.logging.loggers

import it.unibo.jakta.agents.bdi.engine.Jakta.separator
import it.unibo.jakta.agents.bdi.engine.logging.LoggerConfigurator
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.loggers.appenders.Appenders.buildAppenders
import org.apache.logging.log4j.Logger

class LoggerFactory private constructor(
    private val loggerName: String,
    private val logFileName: String,
    private val loggingConfig: LoggingConfig,
) {
    val logger: Logger by lazy {
        val appenders = buildAppenders(loggerName, logFileName, loggingConfig)
        LoggerConfigurator.addLogger(loggerName, loggingConfig.logLevel, appenders)
        JaktaLogger.logger(loggerName)
    }

    companion object {
        fun create(
            loggerName: String,
            loggingConfig: LoggingConfig,
            logFileName: String = loggerName,
        ): LoggerFactory {
            val logFileName =
                if (loggingConfig.logToSingleFile) {
                    "${loggingConfig.logDir}${separator}trace-$logFileName"
                } else {
                    "${loggingConfig.logDir}${separator}$logFileName"
                }
            return LoggerFactory(loggerName, logFileName, loggingConfig)
        }
    }
}
