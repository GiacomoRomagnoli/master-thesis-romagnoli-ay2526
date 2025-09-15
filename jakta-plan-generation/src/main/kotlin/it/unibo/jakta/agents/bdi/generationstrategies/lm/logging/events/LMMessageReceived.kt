package it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events

import com.aallam.openai.api.chat.ChatMessage
import it.unibo.jakta.agents.bdi.engine.logging.events.PlanGenProcedureEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("LMMessageReceived")
data class LMMessageReceived(
    val chatCompletionId: String,
    val chatMessage: ChatMessage,
    override val description: String?,
) : PlanGenProcedureEvent {
    constructor(chatCompletionId: String, chatMessage: ChatMessage) : this(
        chatCompletionId,
        chatMessage,
        "New message received",
    )
}
