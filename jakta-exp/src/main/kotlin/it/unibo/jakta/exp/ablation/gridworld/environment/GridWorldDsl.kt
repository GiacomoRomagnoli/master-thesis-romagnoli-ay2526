package it.unibo.jakta.exp.ablation.gridworld.environment

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.actions.ActionMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.externalAction
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.ActionFailure
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
                when (updatedEnvState) {
                    null -> addFeedback(ActionFailure.GenericActionFailure(actionSignature, arguments))
                    else -> if (oldPosition != null) updateData("state" to updatedEnvState)
                }
            }
        }.meaning {
            "move in the given ${args[0]}"
        }

    val manhattanDistanceFromObject =
        externalAction("get_distance", "Object", "Distance") {
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
        val xVar = Var.of("X")
        val yVar = Var.of("Y")

        val queryStruct =
            if (subject != null) {
                Struct.of(predicate, subject, xVar, yVar)
            } else {
                Struct.of(predicate, xVar, yVar)
            }

        val query = Belief.wrap(queryStruct, wrappingTag = Belief.SOURCE_PERCEPT)

        return percepts.solve(query, ignoreSource = true).let {
            val x =
                it.substitution[xVar]
                    ?.asInteger()
                    ?.intValue
                    ?.toInt()
            val y =
                it.substitution[yVar]
                    ?.asInteger()
                    ?.intValue
                    ?.toInt()
            if (x != null && y != null) Position(x, y) else null
        }
    }
}
