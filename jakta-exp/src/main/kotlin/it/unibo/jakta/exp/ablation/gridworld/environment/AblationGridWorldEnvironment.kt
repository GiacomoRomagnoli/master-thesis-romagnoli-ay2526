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
import it.unibo.jakta.exp.sharedModel.Direction
import it.unibo.tuprolog.core.Atom
import kotlin.collections.forEach

// TODO remove code duplication
// CPD-OFF
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

            val availableDirections = perceptsFactory.createDirectionBeliefs(currentState)
            val gridSize = listOf(perceptsFactory.createGridSizeBelief(grid))
            val currentPos = listOf(perceptsFactory.createCurrentPositionBelief(currentState))
            val objects = perceptsFactory.createObjectBeliefs(currentState)
            val obstacles = perceptsFactory.createObstacleBeliefs(grid, currentState)
            val validMoves = perceptsFactory.createThereIsBeliefs(currentState)

            availableDirections + gridSize + currentPos + objects + obstacles + validMoves
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
                            val curPos = state.agentPosition
                            val newPos = curPos.translate(parsedDirection)
                            state.move(curPos.x, curPos.y, newPos.x, newPos.y).also {
                                logger?.info {
                                    "The robot moved from (${curPos.x}, ${curPos.y}) to (${newPos.x}, ${newPos.y})"
                                }
                            }
                        }
                    }
                } else {
                    null.also {
                        logger?.info {
                            "The robot could not move with the provided arguments ${action.args}"
                        }
                    }
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
// CPD-ON
