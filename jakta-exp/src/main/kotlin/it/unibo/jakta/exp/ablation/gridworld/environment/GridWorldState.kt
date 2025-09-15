package it.unibo.jakta.exp.ablation.gridworld.environment

import it.unibo.jakta.exp.ablation.gridworld.model.Grid
import it.unibo.jakta.exp.ablation.gridworld.model.Position
import it.unibo.jakta.exp.sharedModel.Direction

data class GridWorldState(
    val agentPosition: Position = DEFAULT_START_POSITION,
    val objectsPosition: Map<String, Position> = defaultObjects,
    val availableDirections: Set<Direction> = defaultDirections,
    val grid: Grid = Grid(DEFAULT_GRID_SIZE, DEFAULT_GRID_SIZE, defaultObstacles),
) {
    fun move(
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int,
    ): GridWorldState {
        val current = this.agentPosition
        if (current.x != fromX || current.y != fromY) {
            return this // invalid move
        }

        val newPosition = Position(toX, toY)
        return if (grid.isInBoundaries(newPosition) && !grid.isObstacle(newPosition)) {
            this.copy(agentPosition = newPosition)
        } else {
            this // invalid move
        }
    }

    companion object {
        fun of(
            gridWidth: Int,
            gridHeight: Int,
            obstaclesPosition: Set<Position>,
            agentPosition: Position = DEFAULT_START_POSITION,
            objectsPosition: Map<String, Position> = defaultObjects,
            availableDirections: Set<Direction> = defaultDirections,
        ): GridWorldState =
            GridWorldState(
                agentPosition,
                objectsPosition,
                availableDirections,
                Grid(gridWidth, gridHeight, obstaclesPosition),
            )

        val defaultHomeCell = Position(DEFAULT_GRID_SIZE - 1, DEFAULT_GRID_SIZE - 1)

        internal const val DEFAULT_GRID_SIZE = 5

        internal val DEFAULT_START_POSITION = Position(2, 2)

        internal val defaultObjects =
            mapOf(
                "rock" to Position(1, 0),
                "home" to defaultHomeCell,
            )

        internal val defaultObstacles =
            setOf(
                Position(2, 3), // south
                Position(1, 3), // south-west
                Position(3, 3), // south-east
            )

        internal val defaultDirections = Direction.entries.toSet()
    }
}
