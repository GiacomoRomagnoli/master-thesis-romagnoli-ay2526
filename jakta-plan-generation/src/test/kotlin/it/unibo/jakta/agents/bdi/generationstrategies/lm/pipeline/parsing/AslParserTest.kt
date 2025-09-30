package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing

import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl.AgentSpeakParser

fun main() {
    val parser = AgentSpeakParser()

    val examples =
        listOf(
            "+!choose_move(O) : object(O, Xg, Yg) & current_position(X, Y) & ~(X < Xg & free(east)) & ~(X > Xg & free(west)) & ~(Y < Yg & free(north)) & ~(Y > Yg & free(south)) <- fail",
            "+!go(X) : location(Y) & ~blocked(path(Y,X)) <- move(X)",
            "+temperature(T) : T > 30 <- turn_on(ac)",
            "+belief(something) <- action1; action2; +!goal(param)",
            "+!achieve(goal) : precond(A) & B > 5 <- do_action(A, B); +belief(done)",
        )

    println("=== AgentSpeak Parser Examples ===\n")

    examples.forEach { example ->
        try {
            val parserResult = parser.toPlan(parser.parseTerm(example))
            println("Plan: $parserResult")
        } catch (e: Exception) {
            println("Failed to parse: $example")
            println("Error: ${e.message}\n")
        }
    }
}
