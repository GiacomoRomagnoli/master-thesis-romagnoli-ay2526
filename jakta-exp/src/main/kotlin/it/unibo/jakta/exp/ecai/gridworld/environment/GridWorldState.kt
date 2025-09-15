package it.unibo.jakta.exp.ecai.gridworld.environment

import it.unibo.jakta.playground.gridworld.model.Cell
import it.unibo.jakta.playground.gridworld.model.Direction
import it.unibo.jakta.playground.gridworld.model.Grid

data class GridWorldState(
    val gridSize: Int = DEFAULT_GRID_SIZE,
    val agentPosition: Cell = DEFAULT_START_POSITION,
    val objectsPosition: Map<String, Cell> = defaultObjects,
    val obstaclesPosition: Set<Cell> = defaultObstacles,
    val availableDirections: Set<Direction> = defaultDirections,
    val grid: Grid = Grid(gridSize, obstaclesPosition),
) {
    fun moveRobot(direction: Direction): GridWorldState? {
        val newPosition = this.agentPosition.move(direction)
        return if (grid.isInBoundaries(newPosition) && !grid.isObstacle(newPosition)) {
            val newPosition = Cell(newPosition.x, newPosition.y)
            this.copy(agentPosition = newPosition)
        } else {
            this
        }
    }

    companion object {
        val defaultHomeCell = Cell(DEFAULT_GRID_SIZE - 1, DEFAULT_GRID_SIZE - 1)

        internal const val DEFAULT_GRID_SIZE = 5

        internal val DEFAULT_START_POSITION = Cell(2, 2)

        internal val defaultObjects =
            mapOf(
                "rock" to Cell(1, 0),
                "home" to defaultHomeCell,
            )

        internal val defaultObstacles =
            setOf(
                Cell(2, 3), // south
                Cell(1, 3), // south-west
                Cell(3, 3), // south-east
            )

        internal val defaultDirections = Direction.entries.toSet()
    }
}
