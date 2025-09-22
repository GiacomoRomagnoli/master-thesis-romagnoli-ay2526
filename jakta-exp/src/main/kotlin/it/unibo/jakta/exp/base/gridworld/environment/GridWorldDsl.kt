package it.unibo.jakta.exp.base.gridworld.environment

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.actions.ActionMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.externalAction
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.exp.GridWorldEnvironment
import it.unibo.jakta.exp.base.explorer.logging.MoveActionSuccess
import it.unibo.jakta.exp.base.gridworld.environment.BaseExpGridWorld.Companion.state
import it.unibo.jakta.exp.base.gridworld.logging.ObjectReachedEvent
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Var
import kotlin.random.Random

object GridWorldDsl {
    private const val DEFAULT_SEED = 7
    private val random = Random(DEFAULT_SEED)

    fun MasScope.gridWorld(gridWorldEnvironment: GridWorldEnvironment = BaseExpGridWorld()) =
        environment {
            from(gridWorldEnvironment)
            actions {
                action(move)
                action(getDirectionToMove)
            }
        }

    val move =
        externalAction("move", "direction") {
            val env = environment as? BaseExpGridWorld
            if (env != null) {
                val updatedEnvState = env.parseAction(actionName)
                val oldPosition = env.data.state()?.agentPosition
                if (updatedEnvState != null && oldPosition != null) {
                    updateData("state" to updatedEnvState)
                    val newPosition = updatedEnvState.agentPosition
                    val feedback =
                        updatedEnvState.objectsPosition.entries
                            .find { it.value == newPosition }
                            ?.let { ObjectReachedEvent(it.key, arguments) }
                            ?: MoveActionSuccess(oldPosition, newPosition, arguments)
                    addFeedback(feedback)
                }
            }
        }.meaning {
            "move in the given ${args[0]}"
        }

    val getDirectionToMove =
        externalAction("getDirectionToMove", "direction") {
            val percepts = environment.perception.percept()
            val output = arguments[0].asVar()
            val query =
                Belief.wrap(
                    Struct.of("free", Var.of("Direction")),
                    wrappingTag = Belief.SOURCE_PERCEPT,
                )

            val dir =
                percepts
                    .solveAll(query, ignoreSource = true)
                    .toList()
                    .flatMap { it.substitution.values }
                    .random(random)

            if (output != null) {
                addResults(Substitution.unifier(output to dir))
            }
        }.meaning {
            "provides a ${args[0]} free of obstacles where the agent can then move"
        }
}
