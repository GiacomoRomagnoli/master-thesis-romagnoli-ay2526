package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl

import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.Parser
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result.ParserResult
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result.ParserSuccess
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.core.operators.Specifier
import it.unibo.tuprolog.core.parsing.TermParser

// TODO remove code duplication
// CPD-OFF
class AgentSpeakParser : Parser {
    private val normalizer = AgentSpeakNormalizer()
    private val converter = AgentSpeakConverter()
    private val planParser = PlanParser(converter)

    override fun parse(input: String): ParserResult {
        val blocks = extractCodeBlocks(input)
        val parsedPlans = blocks.mapNotNull { content -> toPlan(parseTerm(content)) }
        return ParserSuccess.NewResult(parsedPlans, rawContent = input)
    }

    private fun extractCodeBlocks(input: String): List<String> {
        val regex = """```(?:\w*\n)?([\s\S]*?)```""".toRegex()
        val matches = regex.findAll(input)

        return matches
            .flatMap { matchResult ->
                val blockContent = matchResult.groupValues[1].trim()
                if (blockContent.contains("\n---\n")) {
                    blockContent.split("\n---\n").map { it.trim() }
                } else {
                    listOf(blockContent)
                }
            }.toList()
    }

    private fun parseTerm(input: String): Term {
        val normalized = normalizer.normalize(input)
        return parser.parseTerm(normalized)
    }

    private fun toPlan(planTerm: Term): Plan? = planParser.toPlan(planTerm)

    companion object {
        private val AGENTSPEAK_OPERATORS =
            OperatorSet(
                Operator("&", Specifier.XFY, 1000), // Conjunction
                Operator("|", Specifier.XFY, 1100), // Disjunction
                Operator("~", Specifier.FX, 900), // Negation
                Operator(":", Specifier.XFX, 1150), // Context separator
                Operator(";", Specifier.XFY, 1050), // Sequential composition
                Operator("<-", Specifier.XFX, 1200), // Plan body separator
            )

        private val parser = TermParser.withOperators(OperatorSet.DEFAULT + AGENTSPEAK_OPERATORS)
    }
}
// CPD-ON
