package it.unibo.jakta.evals.retrievers.plandata

import it.unibo.jakta.agents.bdi.engine.actions.ActionSignature
import it.unibo.jakta.agents.bdi.engine.beliefs.AdmissibleBelief
import it.unibo.jakta.agents.bdi.engine.events.AdmissibleGoal
import it.unibo.jakta.agents.bdi.engine.plans.Plan

data class InvocationContext(
    val masId: String?,
    val agentId: String?,
    val plans: List<Plan> = emptyList(),
    val admissibleGoals: List<AdmissibleGoal> = emptyList(),
    val admissibleBeliefs: List<AdmissibleBelief> = emptyList(),
    val actions: List<ActionSignature> = emptyList(),
) {
    override fun toString(): String =
        """
        |InvocationContext:
        |  Plans: ${plans.size}
        |  Admissible Goals: ${admissibleGoals.size}
        |  Admissible Beliefs: ${admissibleBeliefs.size}
        |  Actions: ${actions.size}
        """.trimMargin()
}
