package it.unibo.jakta.agents.bdi.engine.logging.loggers

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.MasID
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.NegativeFeedback
import it.unibo.jakta.agents.bdi.engine.generation.PgpID
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEventContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.ObjectMessage

interface JaktaLogger {
    val logger: Logger

    fun log(event: () -> LogEvent)

    fun trace(message: () -> Any?) = logger.trace(message)

    fun debug(message: () -> Any?) = logger.debug(message)

    fun info(message: () -> Any?) = logger.info(message)

    fun warn(message: () -> Any?) = logger.warn(message)

    fun error(message: () -> Any?) = logger.error(message)

    companion object {
        fun logger(name: String): Logger = LogManager.getLogger(name)

        fun Logger.implementation(
            masID: MasID,
            event: () -> LogEvent,
            agentID: AgentID? = null,
            pgpID: PgpID? = null,
            cycleCount: Long? = null,
        ) {
            val eventInstance by lazy(event)
            when (val e = eventInstance) {
                // TODO allow to configure messages to log or to ignore
                is NegativeFeedback ->
                    this.warn {
                        ObjectMessage(LogEventContext(e, masID, agentID, pgpID, cycleCount))
                    }
                else ->
                    this.info {
                        ObjectMessage(LogEventContext(e, masID, agentID, pgpID, cycleCount))
                    }
            }
        }
    }
}
