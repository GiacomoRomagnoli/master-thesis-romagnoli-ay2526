package it.unibo.jakta.exp.ablation.gridworld.environment

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.actions.ActionMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.externalAction
import it.unibo.jakta.exp.GridWorldEnvironment
import it.unibo.jakta.exp.ablation.gridworld.environment.AblationGridWorldEnvironment.Companion.state

object GridWorldDsl {
    fun MasScope.gridWorld(gridWorldEnvironment: GridWorldEnvironment = AblationGridWorldEnvironment()) =
        environment {
            from(gridWorldEnvironment)
            actions {
                action(move).meaning {
                    "move from (${args[0]}, ${args[1]}) to (${args[2]}, ${args[3]})"
                }
            }
        }

    val move =
        externalAction("move", "XStart", "YStart", "XEnd", "YEnd") {
            val env = environment as? AblationGridWorldEnvironment
            if (env != null) {
                val updatedEnvState = env.parseAction(actionName)
                val oldPosition = env.data.state()?.agentPosition
                if (updatedEnvState != null && oldPosition != null) {
                    updateData("state" to updatedEnvState)
                }
            }
        }
}
