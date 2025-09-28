package it.unibo.jakta.agents.bdi.engine.generation.manager.impl

import it.unibo.jakta.agents.bdi.engine.actions.effects.EventChange
import it.unibo.jakta.agents.bdi.engine.context.AgentContext
import it.unibo.jakta.agents.bdi.engine.context.ContextUpdate.ADDITION
import it.unibo.jakta.agents.bdi.engine.events.AchievementGoalInvocation
import it.unibo.jakta.agents.bdi.engine.events.Event
import it.unibo.jakta.agents.bdi.engine.events.TestGoalInvocation
import it.unibo.jakta.agents.bdi.engine.executionstrategies.ExecutionResult
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.ExecutionFeedback
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.NegativeFeedback.InapplicablePlan
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.NegativeFeedback.PlanNotFound
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PGPSuccess
import it.unibo.jakta.agents.bdi.engine.generation.GenerationStrategy
import it.unibo.jakta.agents.bdi.engine.generation.manager.UnavailablePlanStrategy
import it.unibo.jakta.agents.bdi.engine.logging.loggers.AgentLogger
import it.unibo.jakta.agents.bdi.engine.plans.PartialPlan
import it.unibo.jakta.agents.bdi.engine.plans.Plan

internal class UnavailablePlanStrategyImpl(
    override val logger: AgentLogger?,
) : UnavailablePlanStrategy {
    override fun handleUnavailablePlans(
        selectedEvent: Event,
        relevantPlans: List<Plan>,
        isApplicablePlansEmpty: Boolean,
        context: AgentContext,
        generationStrategy: GenerationStrategy?,
    ): ExecutionResult =
        when {
            relevantPlans.isEmpty() -> handlePlanNotFound(selectedEvent, context, generationStrategy)
            isApplicablePlansEmpty ->
                handleFailedPlanPreconditions(selectedEvent, relevantPlans, context)
            // not expected since either relevantPlans and/or applicablePlans should be empty
            else -> ExecutionResult(newAgentContext = context)
        }

    override fun handlePlanNotFound(
        selectedEvent: Event,
        context: AgentContext,
        generationStrategy: GenerationStrategy?,
    ): ExecutionResult {
        val initialGoal = selectedEvent.trigger
        val newPlan =
            if (generationStrategy != null &&
                (initialGoal is AchievementGoalInvocation || initialGoal is TestGoalInvocation)
            ) {
                GenerationPlanBuilder.getGenerationPlan(initialGoal)
            } else {
                null
            }
        val feedback =
            if (newPlan != null && newPlan.parentGenerationGoal != null) {
                val goal =
                    generationStrategy?.generationConfig?.let {
                        newPlan.parentGenerationGoal!!.copy(generationConfig = it)
                    } ?: newPlan.parentGenerationGoal!!
                PGPSuccess.GenerationRequested(
                    generationStrategy,
                    goal,
                )
            } else {
                PlanNotFound(initialGoal)
            }

        return createResult(selectedEvent, context, feedback, newPlan)
    }

    private fun handleFailedPlanPreconditions(
        selectedEvent: Event,
        relevantPlans: List<Plan>,
        context: AgentContext,
    ): ExecutionResult {
        val feedback =
            InapplicablePlan(
                relevantPlans
                    .map {
                        val ignoreSource = it is PartialPlan
                        it.checkApplicability(selectedEvent, context.beliefBase, ignoreSource)
                    }.filter { it.trigger != null && it.guards != null }
                    .also { planApp ->
                        planApp.forEach { plan ->
                            logger?.warn { "Plan ${plan.trigger?.value} is not applicable" }
                            plan.guards?.forEach { guard ->
                                if (!guard.value) {
                                    logger?.warn {
                                        "Failing guard ${guard.key} due to error ${plan.error}"
                                    }
                                }
                            }
                        }
                    },
            )

        return createResult(selectedEvent, context, feedback)
    }

    private fun createResult(
        selectedEvent: Event,
        context: AgentContext,
        feedback: ExecutionFeedback,
        newPlan: PartialPlan? = null,
    ): ExecutionResult =
        if (newPlan != null) {
            val failureTrigger = selectedEvent.trigger
            val newBelief = GenerationPlanBuilder.getMissingPlanBelief(failureTrigger.value)
            val newEvent = GenerationPlanBuilder.getFailureEvent(selectedEvent)
            ExecutionResult(
                newAgentContext =
                    context.copy(
                        planLibrary = context.planLibrary.addPlan(newPlan),
                        beliefBase = context.beliefBase.add(newBelief).updatedBeliefBase,
                        events = newEvent?.let { context.events + it } ?: context.events,
                    ),
                feedback = feedback,
            ).also {
                newEvent?.let { logger?.log { EventChange(it, ADDITION) } }
            }
        } else {
            ExecutionResult(
                newAgentContext = context,
                feedback = feedback,
            )
        }
}
