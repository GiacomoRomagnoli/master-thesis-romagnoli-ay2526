package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl

import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.engine.plans.PlanID
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Truth

class PlanParser(
    private val converter: AgentSpeakConverter,
) {
    data class PlanComponents(
        val trigger: Term,
        val context: Term,
        val body: Term,
    )

    fun isPlan(term: Term): Boolean = term is Struct && term.functor == "<-"

    fun extractPlanComponents(plan: Term): PlanComponents? {
        if (plan !is Struct) return null

        return when (plan.functor) {
            ":-" -> PlanComponents(plan.args[0], Truth.TRUE, plan.args[1])
            "<-" -> {
                if (plan.arity != 2) return null

                val trigger = plan.args[0]
                val body = plan.args[1]

                if (trigger is Struct && trigger.functor == ":") {
                    PlanComponents(trigger.args[0], trigger.args[1], body)
                } else {
                    PlanComponents(trigger, Truth.TRUE, body)
                }
            }
            else -> null
        }
    }

    fun toPlan(planTerm: Term): Plan? {
        val components = extractPlanComponents(planTerm) ?: return null

        val trigger = converter.createTrigger(components.trigger) ?: return null
        val guard = components.context as? Struct ?: Truth.TRUE
        val goals = converter.flattenBody(components.body)

        return Plan.of(PlanID(trigger, guard), goals)
    }
}
