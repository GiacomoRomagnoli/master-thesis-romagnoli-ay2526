package it.unibo.jakta.evals.evaluators.path

import it.unibo.jakta.exp.gridworld.model.Position

data class PathEvaluationResult(
    val scenario: SearchScenario,
    val isValidPlan: Boolean,
    val isSuccess: Boolean,
    val llmPathLength: Int?,
    val optimalPathLength: Int,
    val llmPath: List<Position>? = null,
) {
    val plr: Double? =
        if (isSuccess && llmPathLength != null) {
            llmPathLength / optimalPathLength.toDouble()
        } else {
            null
        }

    val excessSteps: Int? =
        if (isSuccess && llmPathLength != null) {
            llmPathLength - optimalPathLength
        } else {
            null
        }
}
