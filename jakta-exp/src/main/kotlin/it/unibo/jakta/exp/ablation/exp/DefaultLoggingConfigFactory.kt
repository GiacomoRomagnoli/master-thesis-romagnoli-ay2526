package it.unibo.jakta.exp.ablation.exp

import it.unibo.jakta.agents.bdi.dsl.loggingConfig
import it.unibo.jakta.exp.LoggingConfigFactory
import it.unibo.jakta.exp.options.ExpLoggingConfig

internal class DefaultLoggingConfigFactory : LoggingConfigFactory {
    override fun createLoggingConfig(
        runId: String,
        expCfg: ExpLoggingConfig,
    ) = loggingConfig {
        logToFile = expCfg.logToFile
        logToConsole = expCfg.logToConsole
        logToServer = expCfg.logToServer
        logLevel = expCfg.logLevel.level
        logDir = "${expCfg.logDir}/$runId"
        logServerUrl = expCfg.logServerUrl
    }
}
