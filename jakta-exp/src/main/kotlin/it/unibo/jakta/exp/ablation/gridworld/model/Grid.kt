package it.unibo.jakta.exp.ablation.gridworld.model

import it.unibo.jakta.exp.sharedModel.Direction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Grid")
data class Grid(
    val width: Int,
    val height: Int,
    val obstacles: Set<Position> = emptySet(),
) {
    fun isInBoundaries(pos: Position): Boolean =
        pos.x in 0 until width &&
            pos.y in 0 until height &&
            !obstacles.contains(pos)

    fun isObstacle(position: Position): Boolean = obstacles.contains(position)

    fun isObstacleInDirection(
        from: Position,
        direction: Direction,
    ): Boolean {
        val targetPosition = from.translate(direction)
        return !isInBoundaries(targetPosition) || isObstacle(targetPosition)
    }

    fun getNeighbors(pos: Position): List<Position> {
        val directions =
            listOf(
                Position(0, 1), // Up
                Position(0, -1), // Down
                Position(1, 0), // Right
                Position(-1, 0), // Left
            )

        return directions
            .map { dir ->
                Position(pos.x + dir.x, pos.y + dir.y)
            }.filter { isInBoundaries(it) }
    }
}
