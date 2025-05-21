package it.unibo.jakta.agents.bdi.engine.actions

import it.unibo.jakta.agents.bdi.engine.Agent
import it.unibo.jakta.agents.bdi.engine.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.engine.messages.Message

interface ExternalAction : Action<EnvironmentChange, ExternalResponse, ExternalRequest> {
    fun addAgent(agent: Agent)

    fun removeAgent(agentName: String)

    fun sendMessage(
        agentName: String,
        message: Message,
    )

    fun broadcastMessage(message: Message)

    fun addData(
        key: String,
        value: Any,
    )

    fun removeData(key: String)

    fun updateData(newData: Map<String, Any>)

    fun updateData(
        keyValue: Pair<String, Any>,
        vararg others: Pair<String, Any>,
    ) = updateData(mapOf(keyValue, *others))
}
