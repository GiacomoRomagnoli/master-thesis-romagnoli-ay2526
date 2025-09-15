package it.unibo.jakta.agents.bdi.engine

import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.events.AchievementGoalFailure
import it.unibo.jakta.agents.bdi.engine.events.AchievementGoalInvocation
import it.unibo.jakta.agents.bdi.engine.events.BeliefBaseAddition
import it.unibo.jakta.agents.bdi.engine.events.BeliefBaseRemoval
import it.unibo.jakta.agents.bdi.engine.events.BeliefBaseUpdate
import it.unibo.jakta.agents.bdi.engine.events.TestGoalFailure
import it.unibo.jakta.agents.bdi.engine.events.TestGoalInvocation
import it.unibo.jakta.agents.bdi.engine.events.Trigger
import it.unibo.jakta.agents.bdi.engine.goals.Achieve
import it.unibo.jakta.agents.bdi.engine.goals.Act
import it.unibo.jakta.agents.bdi.engine.goals.AddBelief
import it.unibo.jakta.agents.bdi.engine.goals.GeneratePlan
import it.unibo.jakta.agents.bdi.engine.goals.Goal
import it.unibo.jakta.agents.bdi.engine.goals.RemoveBelief
import it.unibo.jakta.agents.bdi.engine.goals.Spawn
import it.unibo.jakta.agents.bdi.engine.goals.Test
import it.unibo.jakta.agents.bdi.engine.goals.UpdateBelief
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.parsing.ParseException

object JaktaParser {
    private fun parseRule(input: String): Rule? =
        try {
            val processedInput = convertVariables(input)
            Jakta.parseRule(processedInput)
        } catch (_: ParseException) {
            null
        }

    private fun parseStruct(input: String): Struct? =
        try {
            val processedInput =
                convertVariables(input)
                    .removeSuffix("()")
            Jakta.parseStruct(processedInput)
        } catch (_: ParseException) {
            null
        }

    private fun convertVariables(content: String): String {
        val regex = Regex("\\[(\\w+)]")
        return regex.replace(content) { match ->
            match.groupValues[1].uppercase()
        }
    }

    fun tangleTrigger(input: String): Trigger? {
        val keyword = ParsingUtils.TriggerKeyword.extractTriggerType(input)
        return if (keyword != null) {
            val processedInput =
                input
                    .removePrefix(keyword.toString().lowercase())
                    .removeSuffix("()")
            val parsedInput = tangleStruct(processedInput)
            when (keyword) {
                ParsingUtils.TriggerKeyword.Achieve -> parsedInput?.let { AchievementGoalInvocation(it) }
                ParsingUtils.TriggerKeyword.AchieveFailure -> parsedInput?.let { AchievementGoalFailure(it) }
                ParsingUtils.TriggerKeyword.Test ->
                    parsedInput?.let { TestGoalInvocation(Belief.wrap(it, wrappingTag = Belief.SOURCE_SELF)) }
                ParsingUtils.TriggerKeyword.TestFailure -> parsedInput?.let { TestGoalFailure(it) }
                ParsingUtils.TriggerKeyword.AddBelief ->
                    parsedInput?.let { Belief.from(it) }?.let {
                        BeliefBaseAddition(it)
                    }
                ParsingUtils.TriggerKeyword.RemoveBelief ->
                    parsedInput?.let { Belief.from(it) }?.let {
                        BeliefBaseRemoval(it)
                    }
                ParsingUtils.TriggerKeyword.UpdateBelief ->
                    parsedInput?.let { Belief.from(it) }?.let {
                        BeliefBaseUpdate(it)
                    }
            }
        } else {
            null
        }
    }

    fun tangleGoal(input: String): Goal? {
        val keyword = ParsingUtils.GoalKeyword.extractGoalType(input)
        return if (keyword != null) {
            val processedInput =
                input
                    .removePrefix(keyword.toString().lowercase())
                    .removeSuffix("()")
            when (keyword) {
                ParsingUtils.GoalKeyword.Spawn -> tangleGoal(processedInput)?.let { g -> Spawn.of(g) }
                ParsingUtils.GoalKeyword.Generate -> tangleGoal(processedInput)?.let { g -> GeneratePlan.of(g) }
                else -> {
                    val parsedInput = tangleStruct(processedInput)
                    when (keyword) {
                        ParsingUtils.GoalKeyword.Achieve -> parsedInput?.let { g -> Achieve.of(g) }
                        ParsingUtils.GoalKeyword.Test ->
                            parsedInput
                                ?.let {
                                    Belief.wrap(it, wrappingTag = Belief.SOURCE_SELF)
                                }?.let { g -> Test.of(g) }
                        ParsingUtils.GoalKeyword.Add ->
                            parsedInput
                                ?.let {
                                    Belief.wrap(it, wrappingTag = Belief.SOURCE_SELF)
                                }?.let { g -> AddBelief.of(g) }
                        ParsingUtils.GoalKeyword.Remove ->
                            parsedInput
                                ?.let {
                                    Belief.wrap(it, wrappingTag = Belief.SOURCE_SELF)
                                }?.let { g -> RemoveBelief.of(g) }
                        ParsingUtils.GoalKeyword.Update ->
                            parsedInput
                                ?.let {
                                    Belief.wrap(it, wrappingTag = Belief.SOURCE_SELF)
                                }?.let { g -> UpdateBelief.of(g) }
                        ParsingUtils.GoalKeyword.Execute -> parsedInput?.let { g -> Act.of(g) }
                        else -> null
                    }
                }
            }
        } else {
            null
        }
    }

    fun tangleStruct(input: String): Struct? = parseStruct(input)

    fun tangleRule(input: String): Rule? = parseRule(input)
}
