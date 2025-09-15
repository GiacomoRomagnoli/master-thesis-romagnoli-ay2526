package it.unibo.jakta.exp.explorer

import it.unibo.jakta.agents.bdi.engine.events.BeliefBaseAddition
import it.unibo.jakta.agents.bdi.engine.plans.PlanLibrary
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.ContextFilter
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.ExtendedAgentContext

object CustomFilter {
    /**
     * Removes all belief base addition plans.
     */
    val beliefBaseAdditionPlanFilter =
        object : ContextFilter {
            override val name = "BeliefBaseAdditionPlanFilter"

            override fun filter(extendedContext: ExtendedAgentContext): ExtendedAgentContext {
                val initialGoal = extendedContext.initialGoal

                val context = extendedContext.context
                val filteredContext =
                    context.copy(
                        planLibrary =
                            PlanLibrary.of(
                                context.planLibrary.plans
                                    .filterNot { it.trigger is BeliefBaseAddition }
                                    .distinctBy { it.trigger },
                            ),
                    )

                val externalActions = extendedContext.externalActions

                return ExtendedAgentContext(initialGoal, filteredContext, externalActions)
            }
        }
}
