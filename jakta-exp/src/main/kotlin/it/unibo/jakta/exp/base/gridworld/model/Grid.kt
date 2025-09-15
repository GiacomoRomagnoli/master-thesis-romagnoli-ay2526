package it.unibo.jakta.playground.gridworld.model

import it.unibo.jakta.exp.base.gridworld.model.Cell
import it.unibo.jakta.exp.sharedModel.Direction

class Grid(
    val size: Int,
    val obstacles: Set<Cell> = emptySet(),
) {
    fun isInBoundaries(position: Cell): Boolean = position.x in 0 until size && position.y in 0 until size

    fun isObstacle(position: Cell): Boolean = obstacles.contains(position)

    fun isObstacleInDirection(
        from: Cell,
        direction: Direction,
    ): Boolean {
        val targetPosition = from.move(direction)
        return !isInBoundaries(targetPosition) || isObstacle(targetPosition)
    }
}
