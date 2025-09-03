package it.unibo.jakta.evals.evaluators.path

import it.unibo.jakta.exp.gridworld.model.Position

data class PathNode(
    val position: Position,
    val gScore: Int = Int.MAX_VALUE, // Cost from start to this node
    val fScore: Int = Int.MAX_VALUE, // gScore + heuristic
    val parent: PathNode? = null,
) : Comparable<PathNode> {
    override fun compareTo(other: PathNode): Int = fScore.compareTo(other.fScore)
}
