package it.unibo.jakta.evals.evaluators.path

import it.unibo.jakta.exp.ablation.gridworld.model.Position

sealed class SearchResult {
    data class Success(
        val path: List<Position>,
        val cost: Int,
    ) : SearchResult()

    data class NoPath(
        val reason: String,
    ) : SearchResult()
}
