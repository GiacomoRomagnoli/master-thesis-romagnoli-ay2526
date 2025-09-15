package it.unibo.jakta.exp.ablation.gridworld.environment

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
import it.unibo.jakta.exp.gridworld.environment.GridWorldPercepts
import it.unibo.jakta.exp.gridworld.environment.GridWorldState
import it.unibo.tuprolog.core.Integer
import kotlin.collections.forEach

class AblationGridWorldEnvironment(
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
        return if (currentState != null) {
            val grid = currentState.grid

            val gridSize = listOf(perceptsFactory.createGridSizeBelief(grid))
            val currentPos = listOf(perceptsFactory.createCurrentPositionBelief(currentState))
            val objects = perceptsFactory.createObjectBeliefs(currentState)
            val obstacles = perceptsFactory.createObstacleBeliefs(grid)
            val validMoves = perceptsFactory.createValidMoveBeliefs(grid, currentState)

            gridSize + currentPos + objects + obstacles + validMoves
        } else {
            emptyList()
        }
    }

    override fun percept(): BeliefBase {
        perception = Perception.of(getPercepts())
        return super<EnvironmentImpl>.percept()
    }

    override fun updateData(newData: Map<String, Any>): AblationGridWorldEnvironment =
        copy(data = newData).also {
            getPercepts().forEach { b -> logger?.info { b.purpose?.capitalize() } }
        }

    private fun Integer?.toIntOrNull(): Int? = this?.toString()?.toIntOrNull()

    fun parseAction(actionName: String): GridWorldState? {
        val state = data.state() ?: return null
        val action = tangleStruct(actionName)
        return when {
            action?.functor == "move" -> {
                if (action.args.isNotEmpty()) {
                    val xStart = action.args[0].asInteger().toIntOrNull()
                    val yStart = action.args[1].asInteger().toIntOrNull()
                    val xEnd = action.args[2].asInteger().toIntOrNull()
                    val yEnd = action.args[3].asInteger().toIntOrNull()

                    if (xStart != null && yStart != null && xEnd != null && yEnd != null) {
                        state.move(xStart, yStart, xEnd, yEnd).also {
                            logger?.info {
                                "The robot moved from ($xStart, $yStart) to ($xEnd, $yEnd)"
                            }
                        }
                    } else {
                        null.also {
                            logger?.info {
                                "The robot could not move with the provided arguments ${action.args}"
                            }
                        }
                    }
                } else {
                    null // invalid action
                }
            }

            else -> state.also { logger?.warn { "Unknown action: $actionName" } }
        }
    }

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>,
        logger: MasLogger?,
    ): AblationGridWorldEnvironment =
        AblationGridWorldEnvironment(
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
