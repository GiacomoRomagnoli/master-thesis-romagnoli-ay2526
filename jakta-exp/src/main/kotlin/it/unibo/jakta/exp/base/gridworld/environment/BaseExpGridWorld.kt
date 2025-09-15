package it.unibo.jakta.exp.base.gridworld.environment

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.Jakta.capitalize
import it.unibo.jakta.agents.bdi.engine.JaktaParser.tangleStruct
import it.unibo.jakta.agents.bdi.engine.actions.ExternalAction
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.engine.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.engine.logging.loggers.MasLogger
import it.unibo.jakta.agents.bdi.engine.messages.MessageQueue
import it.unibo.jakta.agents.bdi.engine.perception.Perception
import it.unibo.jakta.exp.GridWorldEnvironment
import it.unibo.jakta.playground.gridworld.model.Direction
import it.unibo.tuprolog.core.Atom
import kotlin.collections.forEach

class BaseExpGridWorld(
    agentIDs: Map<String, AgentID> = emptyMap(),
    externalActions: Map<String, ExternalAction> = emptyMap(),
    messageBoxes: Map<AgentID, MessageQueue> = emptyMap(),
    override var perception: Perception = Perception.empty(),
    data: Map<String, Any> = defaultData,
    override val logger: MasLogger? = null,
) : EnvironmentImpl(externalActions, agentIDs, messageBoxes, perception, data, logger),
    GridWorldEnvironment {
    private val perceptsFactory = GridWorldPercepts()

    init {
        perception = Perception.of(getPercepts())
    }

    private fun getPercepts(): List<Belief> {
        val currentState = data.state()
        if (currentState != null) {
            val directionBeliefs = perceptsFactory.createDirectionBeliefs(currentState)
            val objectBeliefs = perceptsFactory.createObjectBeliefs(currentState)
            val obstacleBeliefs = perceptsFactory.createObstacleBeliefs(currentState.grid, currentState)
            val thereIsBeliefs = perceptsFactory.createThereIsBeliefs(currentState)
            return directionBeliefs + objectBeliefs + obstacleBeliefs + thereIsBeliefs
        } else {
            return emptyList()
        }
    }

    override fun updateData(newData: Map<String, Any>): BaseExpGridWorld =
        copy(data = newData).also {
            getPercepts().forEach { b -> logger?.info { b.purpose?.capitalize() } }
        }

    fun parseAction(actionName: String): GridWorldState? {
        val state = data.state() ?: return null
        val action = tangleStruct(actionName)
        return when {
            action?.functor == "move" -> {
                if (action.args.isNotEmpty()) {
                    val direction = action.args[0] as? Atom
                    direction?.value?.let { dir ->
                        val parsedDirection = Direction.fromId(dir)
                        parsedDirection?.let {
                            state.moveRobot(it).also {
                                logger?.info { "The robot moved" }
                            }
                        }
                    }
                } else {
                    null
                }
            }

            else -> state.also { logger?.warn { "Unknown action: $actionName" } }
        }
    }

    override fun percept(): BeliefBase {
        perception = Perception.of(getPercepts())
        return super<EnvironmentImpl>.percept()
    }

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>,
        logger: MasLogger?,
    ): BaseExpGridWorld =
        BaseExpGridWorld(
            agentIDs,
            externalActions,
            messageBoxes,
            perception,
            data,
            logger,
        )

    companion object {
        internal fun Map<String, Any>.state() = this["state"] as? GridWorldState

        internal val defaultData = mapOf("state" to GridWorldState())
    }
}
