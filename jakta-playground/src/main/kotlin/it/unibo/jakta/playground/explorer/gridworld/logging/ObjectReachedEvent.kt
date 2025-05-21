package it.unibo.jakta.playground.explorer.gridworld.logging

import it.unibo.jakta.agents.bdi.engine.logging.events.JaktaLogEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ObjectReached")
data class ObjectReachedEvent(
    val objectName: String,
    override val description: String,
) : JaktaLogEvent {
    constructor(objectName: String) : this(objectName, "Reached object $objectName")
}
