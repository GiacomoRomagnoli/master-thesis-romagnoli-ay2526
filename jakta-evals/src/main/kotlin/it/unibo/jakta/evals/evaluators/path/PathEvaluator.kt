package it.unibo.jakta.evals.evaluators.path

import it.unibo.jakta.evals.evaluators.Evaluator
import it.unibo.jakta.exp.gridworld.model.Grid
import it.unibo.jakta.exp.gridworld.model.Position

class PathEvaluator(
    private val scenario: SearchScenario,
    private val llmPath: List<Position>?,
) : Evaluator<PathEvaluationResult> {
    override fun eval(): PathEvaluationResult {
        val optimalResult =
            AStarSearch.findPath(
                scenario.grid,
                scenario.start,
                scenario.goal,
            )

        val optimalPath =
            when (optimalResult) {
                is SearchResult.Success -> optimalResult.path
                is SearchResult.NoPath -> emptyList()
            }

        val optimalPathLength = optimalPath.size
        val isValidPlan = llmPath?.let { validatePath(scenario.grid, it, scenario.start, scenario.goal) } ?: false
        val isSuccess = isValidPlan && llmPath.lastOrNull() == scenario.goal

        return PathEvaluationResult(
            scenario = scenario,
            isValidPlan = isValidPlan,
            isSuccess = isSuccess,
            llmPathLength = llmPath?.size,
            optimalPathLength = optimalPathLength,
            llmPath = llmPath,
        )
    }

    private fun validatePath(
        grid: Grid,
        path: List<Position>,
        start: Position,
        goal: Position,
    ): Boolean {
        if (path.isEmpty() || path.first() != start || path.last() != goal) return false
        for (i in 1 until path.size) {
            val prev = path[i - 1]
            val curr = path[i]
            if (!grid.isInBoundaries(curr)) return false
            if (curr !in grid.getNeighbors(prev)) return false
        }
        return true
    }
}
