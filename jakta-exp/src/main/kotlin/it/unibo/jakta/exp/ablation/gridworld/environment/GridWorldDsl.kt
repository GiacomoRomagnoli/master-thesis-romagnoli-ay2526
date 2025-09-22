package it.unibo.jakta.exp.ablation.gridworld.environment

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.actions.ActionMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.externalAction
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase
import it.unibo.jakta.exp.GridWorldEnvironment
import it.unibo.jakta.exp.ablation.gridworld.environment.AblationGridWorldEnvironment.Companion.state
import it.unibo.jakta.exp.ablation.gridworld.model.Position
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var

object GridWorldDsl {
    fun MasScope.gridWorld(gridWorldEnvironment: GridWorldEnvironment = AblationGridWorldEnvironment()) =
        environment {
            from(gridWorldEnvironment)
            actions {
                action(move)
                action(manhattanDistanceFromObject)
            }
        }

    val move =
        externalAction("move", "Direction") {
            val env = environment as? AblationGridWorldEnvironment
            if (env != null) {
                val updatedEnvState = env.parseAction(actionName)
                val oldPosition = env.data.state()?.agentPosition
                if (updatedEnvState != null && oldPosition != null) {
                    updateData("state" to updatedEnvState)
                }
            }
        }.meaning {
            "move in the given ${args[0]}"
        }

    val manhattanDistanceFromObject =
        externalAction("getDistance", "Object", "Distance") {
            val percepts = environment.perception.percept()
            val objectToSearch = arguments[0].asAtom()
            val output = arguments[1].asVar()

            if (objectToSearch != null && output != null) {
                val objectCoordinates = getPositionFromPercept(percepts, "object", objectToSearch)
                val positionCoordinates = getPositionFromPercept(percepts, "current_position")

                if (objectCoordinates != null && positionCoordinates != null) {
                    val distance = objectCoordinates.manhattanDistance(positionCoordinates)
                    addResults(Substitution.unifier(output to Numeric.of(distance)))
                }
            }
        }.meaning {
            "get the manhattan distance from the current position to the position of ${args[0]}"
        }

    private fun getPositionFromPercept(
        percepts: BeliefBase,
        predicate: String,
        subject: Term? = null,
    ): Position? {
        val x = Var.of("X")
        val y = Var.of("Y")

        val queryStruct =
            if (subject != null) {
                Struct.of(predicate, subject, x, y)
            } else {
                Struct.of(predicate, x, y)
            }

        val query = Belief.wrap(queryStruct, wrappingTag = Belief.SOURCE_PERCEPT)

        return percepts.solve(query, ignoreSource = true).let {
            val x =
                it.substitution[x]
                    ?.asInteger()
                    ?.intValue
                    ?.toInt()
            val y =
                it.substitution[y]
                    ?.asInteger()
                    ?.intValue
                    ?.toInt()
            if (x != null && y != null) Position(x, y) else null
        }
    }
}
