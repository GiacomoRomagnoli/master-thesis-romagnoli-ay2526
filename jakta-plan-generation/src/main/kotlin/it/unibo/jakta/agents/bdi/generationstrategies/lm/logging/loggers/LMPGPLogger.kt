package it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.loggers

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.MasID
import it.unibo.jakta.agents.bdi.engine.generation.PgpID
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.logging.loggers.JaktaLogger.Companion.implementation
import it.unibo.jakta.agents.bdi.engine.logging.loggers.LoggerFactory
import it.unibo.jakta.agents.bdi.engine.logging.loggers.PGPLogger
import org.apache.logging.log4j.Logger

class LMPGPLogger(
    override val masID: MasID,
    override val agentID: AgentID,
    override val pgpID: PgpID,
    private val delegate: LoggerFactory,
) : PGPLogger {
    override val logger: Logger get() = delegate.logger

    override fun log(event: () -> LogEvent) = logger.implementation(masID, event, agentID, pgpID)

    companion object {
        fun create(
            masID: MasID,
            agentID: AgentID,
            pgpID: PgpID,
            loggingConfig: LoggingConfig,
        ): LMPGPLogger {
            val loggerName = "Mas-${masID.id}-${agentID.name}-${agentID.id}-${pgpID.name}-${pgpID.id}"
            val logFileName = if (loggingConfig.logToSingleFile) masID.id else "Pgp_${pgpID.name}"
            val delegate = LoggerFactory.create(loggerName, loggingConfig, logFileName)
            return LMPGPLogger(masID, agentID, pgpID, delegate)
        }
    }
}
