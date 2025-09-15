package it.unibo.jakta.exp.base.gridworld.model

import it.unibo.jakta.exp.sharedModel.Direction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
@SerialName("Cell")
data class Cell(
    val x: Int,
    val y: Int,
) {
    fun move(direction: Direction): Cell = Cell(x + direction.dx, y + direction.dy)

    fun directionTo(other: Cell): Direction? {
        val dx = (other.x - x).coerceIn(-1, 1)
        val dy = (other.y - y).coerceIn(-1, 1)
        return Direction.fromDeltas(dx, dy)
    }

    fun isAdjacentTo(other: Cell): Boolean {
        val dx = other.x - x
        val dy = other.y - y
        return abs(dx) <= 1 && abs(dy) <= 1 && !(dx == 0 && dy == 0)
    }

    fun isOn(other: Cell): Boolean = x == other.x && y == other.y
}
