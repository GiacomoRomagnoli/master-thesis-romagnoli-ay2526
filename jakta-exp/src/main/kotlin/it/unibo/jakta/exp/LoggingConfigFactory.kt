package it.unibo.jakta.exp

import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.exp.ablation.exp.DefaultLoggingConfigFactory
import it.unibo.jakta.exp.options.ExpLoggingConfig

interface LoggingConfigFactory {
    fun createLoggingConfig(
        runId: String,
        expCfg: ExpLoggingConfig,
    ): LoggingConfig

    companion object {
        fun createDefault(): LoggingConfigFactory = DefaultLoggingConfigFactory()
    }
}
