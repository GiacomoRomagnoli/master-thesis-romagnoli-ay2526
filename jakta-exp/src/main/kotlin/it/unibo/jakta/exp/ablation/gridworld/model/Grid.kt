package it.unibo.jakta.exp.gridworld.model

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

    fun addObstacle(
        x: Int,
        y: Int,
    ): Grid = copy(obstacles = obstacles + Position(x, y))

    fun addObstacles(positions: List<Position>) = copy(obstacles = obstacles + positions)

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

    fun printGrid(
        path: List<Position> = emptyList(),
        start: Position? = null,
        goal: Position? = null,
    ) {
        for (y in height - 1 downTo 0) {
            for (x in 0 until width) {
                val pos = Position(x, y)
                when {
                    pos == start -> print("S ")
                    pos == goal -> print("G ")
                    obstacles.contains(pos) -> print("# ")
                    path.contains(pos) -> print("* ")
                    else -> print(". ")
                }
            }
            println()
        }
    }
}
