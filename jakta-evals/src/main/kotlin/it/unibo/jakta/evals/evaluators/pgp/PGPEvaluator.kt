package it.unibo.jakta.evals.evaluators.pgp

import it.unibo.jakta.agents.bdi.engine.goals.Achieve
import it.unibo.jakta.agents.bdi.engine.goals.Act
import it.unibo.jakta.agents.bdi.engine.visitors.GuardFlattenerVisitor.Companion.flatten
import it.unibo.jakta.evals.evaluators.Evaluator
import it.unibo.jakta.evals.retrievers.plandata.InvocationContext
import it.unibo.jakta.evals.retrievers.plandata.PGPInvocation
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth

class PGPEvaluator(
    val context: InvocationContext,
    val inv: PGPInvocation,
) : Evaluator<PGPEvaluation> {
    private val detector = UselessPlanDetector()

    override fun eval(): PGPEvaluation {
        val plans = inv.generatedPlans
        val averageAmountBeliefs =
            plans
                .map { it.guard.flatten().size }
                .average()
        val averageAmountOperations =
            plans
                .map { it.goals.filterNot { g -> g.value == Truth.TRUE }.size }
                .average()
        val amountGeneralPlan =
            plans
                .count {
                    it.trigger.value.variables
                        .count() >= 1
                }

        val plansToCheck = plans + context.plans
        val uselessPlansResult = detector.detectUselessPlans(plansToCheck)

        // beliefs
        val admissibleBeliefUsages =
            plans
                .flatMap { it.guard.flatten() }
                .distinct()

        val amountInadequateBeliefs =
            compare(
                context.admissibleBeliefs.map { it.rule.head },
                inv.generatedAdmissibleBeliefs.map { it.rule.head },
                admissibleBeliefUsages,
                inv.admissibleBeliefNotParsed,
            ).let {
                it.notParsed +
                    (
                        it.usedNotAdmissible +
                            it.admissibleNotUsed +
                            it.alreadyAdmissible
                    ).size
            }

        // goals
        val admissibleGoalUsages =
            plans
                .flatMap {
                    listOf(it.trigger.value) +
                        it.goals.filterIsInstance<Achieve>().map { g ->
                            g.value
                        }
                }.distinct()

        val amountInadequateGoals =
            compare(
                context.admissibleGoals.map { it.trigger.value },
                inv.generatedAdmissibleGoals.map { it.trigger.value },
                admissibleGoalUsages,
                inv.admissibleGoalsNotParsed,
            ).let {
                it.notParsed +
                    (
                        it.usedNotAdmissible +
                            it.admissibleNotUsed +
                            it.alreadyAdmissible
                    ).size
            }

        // actions
        val actionUsages =
            plans
                .flatMap { it.goals.filterIsInstance<Act>().map { a -> a.action.functor } }

        // used but not existing
        val actionNames = context.actions.map { it.name }
        val amountInadequateActions =
            actionUsages
                .filterNot { actionNames.contains(it) }
                .size

        return PGPEvaluation(
            amountGeneratedPlans = plans.size,
            averageAmountBeliefs = averageAmountBeliefs,
            averageAmountOperations = averageAmountOperations,
            amountGeneralPlan = amountGeneralPlan,
            amountUselessPlans = uselessPlansResult.size,
            amountInadequateUsageGoals = amountInadequateGoals,
            amountInadequateUsageBeliefs = amountInadequateBeliefs,
            amountInadequateUsageActions = amountInadequateActions,
        )
    }

    companion object {
        fun compare(
            admissibleDefault: Iterable<Struct>,
            admissibleGenerated: Iterable<Struct>,
            usagesList: Iterable<Struct>,
            notParsed: Int = 0,
        ): SemanticMisalignmentResult {
            val admissibleDefault = extractElements(admissibleDefault)
            val admissibleGenerated = extractElements(admissibleGenerated)
            val admissible = admissibleDefault + admissibleGenerated
            val usages = extractElements(usagesList)

            return SemanticMisalignmentResult(
                notParsed = notParsed,
                alreadyAdmissible = admissibleDefault intersect admissibleGenerated,
                admissibleNotUsed = admissibleGenerated - usages,
                usedNotAdmissible = usages - admissible,
            )
        }

        private fun extractElements(structs: Iterable<Struct>): Set<String> =
            structs
                .map {
                    if (it.functor == "not" || it.functor == "~") {
                        val s = it.args[0].castToStruct()
                        "${s.functor}/${s.arity}"
                    } else {
                        "${it.functor}/${it.arity}"
                    }
                }.toSet()
    }
}
