package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result

import it.unibo.jakta.agents.bdi.engine.beliefs.AdmissibleBelief
import it.unibo.jakta.agents.bdi.engine.events.AdmissibleGoal
import it.unibo.jakta.agents.bdi.engine.plans.Plan

sealed interface ParserSuccess : ParserResult {
    data class NewResult(
        val plans: List<Plan> = emptyList(),
        val admissibleGoals: Set<AdmissibleGoal> = emptySet(),
        val admissibleBeliefs: Set<AdmissibleBelief> = emptySet(),
        override val rawContent: String,
        val parsingErrors: List<ParserFailure> = emptyList(),
    ) : ParserSuccess
}
