package it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback

import it.unibo.jakta.agents.bdi.engine.logging.events.JaktaLogEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ExecutionFeedback")
sealed interface ExecutionFeedback : JaktaLogEvent
