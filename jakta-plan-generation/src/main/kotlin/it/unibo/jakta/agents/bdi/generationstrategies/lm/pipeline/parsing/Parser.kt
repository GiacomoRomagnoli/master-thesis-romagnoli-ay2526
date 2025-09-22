package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing

import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result.ParserResult

fun interface Parser {
    fun parse(input: String): ParserResult
}
