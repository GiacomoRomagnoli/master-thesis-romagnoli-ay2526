package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering

import it.unibo.jakta.agents.bdi.engine.actions.InternalActions
import it.unibo.jakta.agents.bdi.engine.events.AchievementGoalFailure
import it.unibo.jakta.agents.bdi.engine.events.TestGoalFailure
import it.unibo.jakta.agents.bdi.engine.generation.manager.impl.GenerationPlanBuilder
import it.unibo.jakta.agents.bdi.engine.generation.manager.impl.GenerationPlanBuilder.createNewTriggerFromGoal
import it.unibo.jakta.agents.bdi.engine.generation.manager.impl.GenerationPlanBuilder.getFailureTrigger
import it.unibo.jakta.agents.bdi.engine.plans.PlanLibrary

object DefaultFilters {
    /**
     * Filters out special generation plans from the context that are used when no plan is available,
     * preventing them from being shown to the language model. It also removes any achievement or test goal
     * failure plan.
     */
    val metaPlanFilter =
        object : ContextFilter {
            override val name = "MetaPlanFilter"

            override fun filter(extendedContext: ExtendedAgentContext): ExtendedAgentContext {
                val initialGoal = extendedContext.initialGoal
                val context = extendedContext.context
                val externalActions = extendedContext.externalActions
                val trigger = createNewTriggerFromGoal(initialGoal.goal)?.let { getFailureTrigger(it) }

                return if (trigger != null) {
                    val id = GenerationPlanBuilder.getGenerationPlanID(trigger)
                    val filteredContext =
                        context.copy(
                            planLibrary =
                                PlanLibrary.of(
                                    context.planLibrary.plans
                                        .filterNot { it.id == id }
                                        .filterNot {
                                            it.trigger is TestGoalFailure ||
                                                it.trigger is AchievementGoalFailure
                                        }.distinctBy { it.trigger },
                                ),
                        )
                    ExtendedAgentContext(initialGoal, filteredContext, externalActions)
                } else {
                    ExtendedAgentContext(initialGoal, context, externalActions)
                }
            }
        }

    val printActionFilter =
        object : ContextFilter {
            override val name = "PrintActionFilter"

            override fun filter(extendedContext: ExtendedAgentContext): ExtendedAgentContext =
                extendedContext.copy(
                    context =
                        extendedContext.context.copy(
                            internalActions =
                                extendedContext.context.internalActions
                                    .minus(InternalActions.Print.signature.name),
                        ),
                )
        }
}
