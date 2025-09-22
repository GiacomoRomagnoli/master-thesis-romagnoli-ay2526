package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl

import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.events.AchievementGoalInvocation
import it.unibo.jakta.agents.bdi.engine.events.BeliefBaseAddition
import it.unibo.jakta.agents.bdi.engine.events.BeliefBaseRemoval
import it.unibo.jakta.agents.bdi.engine.events.BeliefBaseUpdate
import it.unibo.jakta.agents.bdi.engine.events.Trigger
import it.unibo.jakta.agents.bdi.engine.goals.Achieve
import it.unibo.jakta.agents.bdi.engine.goals.Act
import it.unibo.jakta.agents.bdi.engine.goals.AddBelief
import it.unibo.jakta.agents.bdi.engine.goals.Goal
import it.unibo.jakta.agents.bdi.engine.goals.RemoveBelief
import it.unibo.jakta.agents.bdi.engine.goals.UpdateBelief
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl.AgentSpeakTermExtensions.getInnerStruct
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl.AgentSpeakTermExtensions.isAchievementGoal
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl.AgentSpeakTermExtensions.isBeliefAddition
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl.AgentSpeakTermExtensions.isBeliefDeletion
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl.AgentSpeakTermExtensions.isBeliefUpdate
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl.AgentSpeakTermExtensions.isComposition
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term

class AgentSpeakConverter {
    fun createTrigger(triggerTerm: Term): Trigger? {
        val struct = triggerTerm.getInnerStruct() ?: return null
        return when {
            triggerTerm.isBeliefAddition() -> BeliefBaseAddition(Belief.wrap(struct))
            triggerTerm.isBeliefDeletion() -> BeliefBaseRemoval(Belief.wrap(struct))
            triggerTerm.isBeliefUpdate() -> BeliefBaseUpdate(Belief.wrap(struct))
            triggerTerm.isAchievementGoal() -> AchievementGoalInvocation(struct)
            else -> null
        }
    }

    fun createGoal(goalTerm: Term): Goal? {
        val struct = goalTerm.getInnerStruct()
        return when {
            goalTerm.isAchievementGoal() -> struct?.let { Achieve.of(it) }
            goalTerm.isBeliefAddition() -> struct?.let { AddBelief.of(Belief.wrap(it)) }
            goalTerm.isBeliefDeletion() -> struct?.let { RemoveBelief.of(Belief.wrap(it)) }
            goalTerm.isBeliefUpdate() -> struct?.let { UpdateBelief.of(Belief.wrap(it)) }
            goalTerm is Struct -> Act.of(goalTerm)
            else -> null
        }
    }

    fun flattenBody(term: Term): List<Goal> {
        return when {
            term.isComposition() -> {
                val struct = term as? Struct ?: return emptyList()
                val leftGoals = struct.args.getOrNull(0)?.let { flattenBody(it) } ?: emptyList()
                val rightGoals = struct.args.getOrNull(1)?.let { flattenBody(it) } ?: emptyList()
                leftGoals + rightGoals
            }
            else -> createGoal(term)?.let { listOf(it) } ?: emptyList()
        }
    }
}
