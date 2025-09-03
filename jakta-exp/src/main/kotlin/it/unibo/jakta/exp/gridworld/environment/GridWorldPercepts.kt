package it.unibo.jakta.exp.gridworld.environment

import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.exp.gridworld.model.Direction
import it.unibo.jakta.exp.gridworld.model.Grid
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

class GridWorldPercepts {
    fun createGridSizeBelief(grid: Grid) =
        Belief.wrap(
            Struct.of("grid_size", Atom.of("${grid.width}"), Atom.of("${grid.height}")),
            wrappingTag = Belief.SOURCE_PERCEPT,
            purpose = "the grid has size ${grid.width}x${grid.height}",
        )

    fun createCurrentPositionBelief(state: GridWorldState) =
        Belief.wrap(
            Struct.of(
                "current_position",
                Atom.of("${state.agentPosition.x}"),
                Atom.of("${state.agentPosition.y}"),
            ),
            wrappingTag = Belief.SOURCE_PERCEPT,
            purpose = "the agent is at (${state.agentPosition.x}, ${state.agentPosition.y})",
        )

    fun createObjectBeliefs(state: GridWorldState) =
        state.objectsPosition.map { (objectName, pos) ->
            Belief.wrap(
                Struct.of(
                    "object",
                    Atom.of(objectName),
                    Atom.of("${pos.x}"),
                    Atom.of("${pos.y}"),
                ),
                wrappingTag = Belief.SOURCE_PERCEPT,
                purpose = "$objectName is at (${pos.x}, ${pos.y})",
            )
        }

    fun createObstacleBeliefs(grid: Grid) =
        grid.obstacles.map { pos ->
            Belief.wrap(
                Struct.of("obstacle", Atom.of("${pos.x}"), Atom.of("${pos.y}")),
                wrappingTag = Belief.SOURCE_PERCEPT,
                purpose = "there is an obstacle at (${pos.x}, ${pos.y})",
            )
        }

    fun createValidMoveBeliefs(
        grid: Grid,
        state: GridWorldState,
    ) = state.availableDirections
        .filter { it != Direction.HERE }
        .mapNotNull { dir ->
            val from = state.agentPosition
            val to = from.translate(dir)

            if (grid.isInBoundaries(to) && !grid.isObstacle(to)) {
                Belief.wrap(
                    Struct.of(
                        "valid_move",
                        Atom.of("${from.x}"),
                        Atom.of("${from.y}"),
                        Atom.of("${to.x}"),
                        Atom.of("${to.y}"),
                    ),
                    wrappingTag = Belief.SOURCE_PERCEPT,
                    purpose = "the agent can move from (${from.x}, ${from.y}) to (${to.x}, ${to.y})",
                )
            } else {
                null
            }
        }
}
