package it.unibo.jakta.evals.evaluators.path

import it.unibo.jakta.exp.gridworld.model.Grid
import it.unibo.jakta.exp.gridworld.model.Position
import java.util.PriorityQueue

object AStarSearch {
    fun findPath(
        grid: Grid,
        start: Position,
        goal: Position,
    ): SearchResult {
        if (!grid.isInBoundaries(start) || !grid.isInBoundaries(goal)) {
            return SearchResult.NoPath("Invalid start or goal position")
        }

        if (start == goal) {
            return SearchResult.Success(listOf(start), 0)
        }

        val openSet = PriorityQueue<PathNode>()
        val closedSet = mutableSetOf<Position>()
        val nodeMap = mutableMapOf<Position, PathNode>()

        // Initialize start node
        val startNode =
            PathNode(
                position = start,
                gScore = 0,
                fScore = start.manhattanDistance(goal),
            )

        openSet.add(startNode)
        nodeMap[start] = startNode

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()

            // Goal reached!
            if (current.position == goal) {
                val path = generatePath(current)
                return SearchResult.Success(path, current.gScore)
            }

            closedSet.add(current.position)

            // Check all neighbors
            for (neighborPos in grid.getNeighbors(current.position)) {
                if (closedSet.contains(neighborPos)) continue

                val tentativeGScore = current.gScore + 1 // Assuming uniform cost
                val existingNode = nodeMap[neighborPos]

                if (existingNode == null || tentativeGScore < existingNode.gScore) {
                    val neighborNode =
                        PathNode(
                            position = neighborPos,
                            gScore = tentativeGScore,
                            fScore = tentativeGScore + neighborPos.manhattanDistance(goal),
                            parent = current,
                        )

                    nodeMap[neighborPos] = neighborNode

                    // Remove old node if it exists and add new one
                    existingNode?.let { openSet.remove(it) }
                    openSet.add(neighborNode)
                }
            }
        }

        return SearchResult.NoPath("No path found to goal")
    }

    private fun generatePath(goalNode: PathNode): List<Position> {
        val path = mutableListOf<Position>()
        var current: PathNode? = goalNode

        while (current != null) {
            path.add(current.position)
            current = current.parent
        }

        return path.reversed()
    }
}
