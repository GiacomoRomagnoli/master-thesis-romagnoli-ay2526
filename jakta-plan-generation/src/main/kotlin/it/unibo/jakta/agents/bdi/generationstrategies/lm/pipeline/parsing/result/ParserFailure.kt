package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result

import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.yaml.PlanData

sealed interface ParserFailure : ParserResult {
    data class GenericParseFailure(
        override val rawContent: String,
    ) : ParserFailure

    data class AdmissibleGoalParseFailure(
        override val rawContent: String,
    ) : ParserFailure

    data class AdmissibleBeliefParseFailure(
        override val rawContent: String,
    ) : ParserFailure

    data class PlanParseFailure(
        val planData: PlanData,
    ) : ParserFailure {
        override val rawContent = ""
    }

    data class EmptyResponse(
        override val rawContent: String,
        val parsingErrors: List<ParserFailure> = emptyList(),
    ) : ParserFailure
}
