package it.unibo.jakta.agents.bdi.engine.plans.impl

import it.unibo.jakta.agents.bdi.engine.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.engine.events.Event
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PlanApplicabilityResult
import it.unibo.jakta.agents.bdi.engine.goals.Goal
import it.unibo.jakta.agents.bdi.engine.plans.ActivationRecord
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.engine.visitors.GuardFlattenerVisitor.Companion.flatten
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("BasePlan")
internal abstract class AbstractPlan : Plan {
    override fun checkApplicability(
        event: Event,
        beliefBase: BeliefBase,
        ignoreSource: Boolean,
    ): PlanApplicabilityResult =
        if (isRelevant(event)) {
            val mgu = event.trigger.value mguWith this.trigger.value
            val actualGuard = guard.apply(mgu).castToStruct()
            // TODO what if the conditions are not only in and?
            // -> The result of the check becomes misleading even if the check itself isn't
            val guards = actualGuard.flatten().associateWith { beliefBase.solve(it, ignoreSource).isYes }
            PlanApplicabilityResult(event.trigger, guards)
        } else {
            PlanApplicabilityResult(
                error = "could not check applicability since the provided plan is not relevant",
            )
        }

    override fun isApplicable(
        event: Event,
        beliefBase: BeliefBase,
    ): Boolean {
        val mgu = event.trigger.value mguWith this.trigger.value
        val actualGuard = guard.apply(mgu).castToStruct()
        return isRelevant(event) && beliefBase.solve(actualGuard).isYes
    }

    override fun isRelevant(event: Event): Boolean =
        event.trigger::class == this.trigger::class && (trigger.value mguWith event.trigger.value).isSuccess

    override fun toActivationRecord(): ActivationRecord = ActivationRecord.of(goals, id)

    protected fun createApplicablePlan(
        event: Event,
        beliefBase: BeliefBase,
        ignoreSource: Boolean = false,
    ): Pair<Struct, List<Goal>>? =
        if (isApplicable(event, beliefBase)) {
            val mgu = event.trigger.value mguWith this.trigger.value
            val actualGuard = guard.apply(mgu).castToStruct()
            val solvedGuard = beliefBase.solve(actualGuard, ignoreSource)
            val actualGoals =
                goals.map {
                    it.copy(
                        it.value
                            .apply(mgu)
                            .apply(solvedGuard.substitution)
                            .castToStruct(),
                    )
                }
            Pair(actualGuard, actualGoals)
        } else {
            null
        }
}
