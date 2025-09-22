package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing

import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl.AgentSpeakParser
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result.ParserSuccess

fun main() {
    val parser = AgentSpeakParser()

    val examples =
        listOf(
            "+!go(X) : location(Y) & ~blocked(path(Y,X)) <- move(X)",
            "+temperature(T) : T > 30 <- turn_on(ac)",
            "+belief(something) <- action1; action2; +!goal(param)",
            "+!achieve(goal) : precond(A) & B > 5 <- do_action(A, B); +belief(done)",
        )

    println("=== AgentSpeak Parser Examples ===\n")

    examples.forEach { example ->
        try {
            val parserResult = parser.parse(example) as? ParserSuccess.NewResult
            println("Plan: ${parserResult?.plans}")
        } catch (e: Exception) {
            println("Failed to parse: $example")
            println("Error: ${e.message}\n")
        }
    }
}
