package it.unibo.jakta.evals.metrics.plandata

import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.engine.plans.PlanID
import it.unibo.jakta.agents.bdi.engine.visitors.GuardFlattenerVisitor.Companion.flatten

class UselessPlanDetector {
    /**
     * Detects plans that will never execute because they are overshadowed by more general plans
     * that appear earlier in the execution order.
     *
     * A plan P2 is considered useless if there exists a plan P1 that:
     * 1. Appears before P2 in the list (lower index)
     * 2. Has the same trigger as P2
     * 3. Has a guard that is a superset of P2's conditions (P1 is more general)
     */
    fun detectUselessPlans(plans: List<Plan>): List<PlanID> {
        val uselessPlans = mutableListOf<PlanID>()

        for (i in 1 until plans.size) {
            val currentPlan = plans[i]

            // Check if any previous plan makes this one useless
            for (j in 0 until i) {
                val previousPlan = plans[j]

                if (isOvershadowed(previousPlan, currentPlan)) {
                    uselessPlans.add(currentPlan.id)
                    break // Once we find one overshadowing plan, we don't need to check others
                }
            }
        }

        return uselessPlans
    }

    /**
     * Checks if targetPlan is overshadowed by shadowingPlan
     *
     * @param targetPlan The plan that might be useless
     * @param shadowingPlan The plan that might overshadow the target
     * @return true if shadowingPlan makes targetPlan useless
     */
    private fun isOvershadowed(
        targetPlan: Plan,
        shadowingPlan: Plan,
    ): Boolean {
        if (targetPlan.trigger != shadowingPlan.trigger) {
            return false
        }

        val targetConditions = targetPlan.guard.flatten()
        val shadowingConditions = shadowingPlan.guard.flatten()

        // The shadowing plan's conditions must be a subset of the target plan's conditions
        // This means the shadowing plan is more general (has fewer/looser conditions)
        return shadowingConditions.containsAll(targetConditions)
    }
}
