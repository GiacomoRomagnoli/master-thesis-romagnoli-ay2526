package it.unibo.jakta.evals.evaluators.path

import it.unibo.jakta.exp.gridworld.model.Grid
import it.unibo.jakta.exp.gridworld.model.Position
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SearchScenario")
data class SearchScenario(
    val grid: Grid,
    val start: Position,
    val goal: Position,
)
