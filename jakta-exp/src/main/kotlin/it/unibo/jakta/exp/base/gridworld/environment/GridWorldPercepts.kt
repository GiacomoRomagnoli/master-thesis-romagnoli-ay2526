package it.unibo.jakta.exp.base.gridworld.environment

import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.exp.sharedModel.Direction
import it.unibo.jakta.playground.gridworld.model.Grid
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import kotlin.collections.plus

// TODO remove code duplication
// CPD-OFF
class GridWorldPercepts {
    fun createDirectionBeliefs(state: GridWorldState) =
        state.availableDirections.map { dir ->
            val functor = "direction"
            val struct = Struct.of(functor, Atom.of(dir.id))
            Belief.wrap(
                struct,
                wrappingTag = Belief.SOURCE_PERCEPT,
                purpose = "${dir.id} is a $functor",
            )
        } +
            Belief.wrap(
                Struct.of("direction", Atom.of("here")),
                wrappingTag = Belief.SOURCE_PERCEPT,
                purpose = "here denotes the null direction w.r.t. the agent's current location",
            )

    fun createObjectBeliefs(state: GridWorldState) =
        state.objectsPosition.map {
            val functor = "object"
            val struct = Struct.of(functor, Atom.of(it.key))
            Belief.wrap(
                struct,
                wrappingTag = Belief.SOURCE_PERCEPT,
                purpose = "${it.key} is an $functor",
            )
        }

    fun createObstacleBeliefs(
        grid: Grid,
        state: GridWorldState,
    ) = state.availableDirections.filter { it != Direction.HERE }.map { direction ->
        val isObstacle = grid.isObstacleInDirection(state.agentPosition, direction)
        if (isObstacle) {
            val struct = Struct.of("obstacle", Atom.of(direction.id))
            Belief.wrap(
                struct,
                wrappingTag = Belief.SOURCE_PERCEPT,
                purpose = "there is an obstacle to the ${direction.id}",
            )
        } else {
            val struct = Struct.of("free", Atom.of(direction.id))
            Belief.wrap(
                struct,
                wrappingTag = Belief.SOURCE_PERCEPT,
                purpose = "there is no obstacle to the ${direction.id}",
            )
        }
    }

    fun createThereIsBeliefs(state: GridWorldState) =
        state.objectsPosition.mapNotNull { (objectName, position) ->
            val direction = state.agentPosition.directionTo(position)
            if (direction != null &&
                (
                    state.agentPosition.isAdjacentTo(position) ||
                        state.agentPosition.isOn(position)
                )
            ) {
                val struct =
                    Struct.of(
                        "there_is",
                        Atom.of(objectName),
                        Atom.of(direction.id),
                    )
                Belief.wrap(
                    struct,
                    wrappingTag = Belief.SOURCE_PERCEPT,
                    purpose = "there is a $objectName to the ${direction.id}",
                )
            } else {
                null
            }
        }
}
// CPD-ON
