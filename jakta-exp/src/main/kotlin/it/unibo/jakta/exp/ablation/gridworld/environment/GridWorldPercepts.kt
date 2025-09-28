package it.unibo.jakta.exp.ablation.gridworld.environment

import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.exp.ablation.gridworld.model.Grid
import it.unibo.jakta.exp.sharedModel.Direction
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct

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

    fun createGridSizeBelief(grid: Grid) =
        Belief.wrap(
            Struct.of("grid_size", Numeric.of(grid.width), Numeric.of(grid.height)),
            wrappingTag = Belief.SOURCE_PERCEPT,
            purpose = "the grid has width ${grid.width} and height ${grid.height}",
        )

    fun createCurrentPositionBelief(state: GridWorldState) =
        Belief.wrap(
            Struct.of(
                "current_position",
                Numeric.of(state.agentPosition.x),
                Numeric.of(state.agentPosition.y),
            ),
            wrappingTag = Belief.SOURCE_PERCEPT,
            purpose = "the agent is at coordinates (${state.agentPosition.x}, ${state.agentPosition.y})",
        )

    fun createObjectBeliefs(state: GridWorldState) =
        state.objectsPosition.map { (objectName, pos) ->
            Belief.wrap(
                Struct.of(
                    "object",
                    Atom.of(objectName),
                    Numeric.of(pos.x),
                    Numeric.of(pos.y),
                ),
                wrappingTag = Belief.SOURCE_PERCEPT,
                purpose = "$objectName is at coordinates (${pos.x}, ${pos.y})",
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
            val direction = state.agentPosition.directionTo(state.availableDirections, position)
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
