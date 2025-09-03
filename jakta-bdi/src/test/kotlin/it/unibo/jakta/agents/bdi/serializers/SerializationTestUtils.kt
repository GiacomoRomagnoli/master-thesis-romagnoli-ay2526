package it.unibo.jakta.agents.bdi.serializers

import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.engine.Jakta.dropWordsWithTrailingNumbers
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin.defaultJsonModule
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin.engineJsonModule
import it.unibo.jakta.agents.bdi.engine.events.Trigger
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.termFormatter
import it.unibo.jakta.agents.bdi.engine.goals.Goal

internal object SerializationTestUtils {
    internal val modules = listOf(engineJsonModule, defaultJsonModule)

    private fun String.normalizeJsonWhitespace(): String =
        this.trim().replace("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex(), "")

    internal infix fun String.shouldBeWithTrimming(expected: String) {
        this.normalizeJsonWhitespace() shouldBe expected.trimIndent().normalizeJsonWhitespace()
    }

    internal fun compareBeliefs(
        actual: Belief,
        expected: Belief,
    ): Boolean =
        termFormatter.format(actual.rule).dropWordsWithTrailingNumbers() ==
            termFormatter.format(expected.rule).dropWordsWithTrailingNumbers() &&
            actual.purpose == expected.purpose

    internal fun compareTriggers(
        actual: Trigger,
        expected: Trigger,
    ): Boolean =
        termFormatter.format(actual.value).dropWordsWithTrailingNumbers() ==
            termFormatter.format(expected.value).dropWordsWithTrailingNumbers() &&
            actual.purpose == expected.purpose

    internal fun compareGoals(
        actual: Goal,
        expected: Goal,
    ): Boolean =
        termFormatter.format(actual.value).dropWordsWithTrailingNumbers() ==
            termFormatter.format(expected.value).dropWordsWithTrailingNumbers()
}
