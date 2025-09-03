package it.unibo.jakta.agents.bdi.engine.logging

import it.unibo.jakta.agents.bdi.engine.logging.events.LogEventContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogEntry(
    @SerialName("@timestamp")
    val timestamp: String,
    @SerialName("ecs.version")
    val ecsVersion: String,
    @SerialName("log.level")
    val logLevel: String,
    val message: LogEventContext,
    @SerialName("process.thread.name")
    val processThreadName: String,
    @SerialName("log.logger")
    val logLogger: String,
)
