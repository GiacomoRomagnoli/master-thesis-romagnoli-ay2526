package it.unibo.jakta.exp.gridworld.environment

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.actions.ActionMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.externalAction
import it.unibo.jakta.exp.gridworld.environment.GridWorldEnvironment.Companion.state

object GridWorldDsl {
    fun MasScope.gridWorld(gridWorldEnvironment: GridWorldEnvironment = GridWorldEnvironment()) =
        environment {
            from(gridWorldEnvironment)
            actions {
                action(move).meaning {
                    "move in the given ${args[0]}"
                }
            }
        }

    val move =
        externalAction("move", "direction") {
            val env = environment as? GridWorldEnvironment
            if (env != null) {
                val updatedEnvState = env.parseAction(actionName)
                val oldPosition = env.data.state()?.agentPosition
                if (updatedEnvState != null && oldPosition != null) {
                    updateData("state" to updatedEnvState)
                }
            }
        }
}
