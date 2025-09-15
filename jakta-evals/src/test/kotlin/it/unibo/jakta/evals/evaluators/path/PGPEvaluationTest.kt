package it.unibo.jakta.evals.evaluators.path

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.evals.SharedUtils.PGP_ID
import it.unibo.jakta.evals.SharedUtils.testAgentLogFile
import it.unibo.jakta.evals.SharedUtils.testMasLogFile
import it.unibo.jakta.evals.SharedUtils.testPgpLogFile
import it.unibo.jakta.evals.evaluators.pgp.PGPEvaluator
import it.unibo.jakta.evals.retrievers.plandata.AblationPGPDataRetriever
import it.unibo.jakta.exp.base.BaseExpRunner.modulesToLoad

class PGPEvaluationTest : FunSpec() {
    init {
        JaktaKoin.loadAdditionalModules(modulesToLoad)

        test("PGPEvaluator should evaluate plans correctly from test logs") {
            val retriever = AblationPGPDataRetriever(testMasLogFile, testAgentLogFile, testPgpLogFile, PGP_ID)
            val planData = retriever.retrieve()
            val pgpInvocation = planData.pgpInvocation
            val invocationContext = planData.invocationContext
            val evaluator = PGPEvaluator(invocationContext, pgpInvocation)
            val pgpEval = evaluator.eval()

            pgpEval.amountGeneratedPlans shouldBe 4
            pgpEval.averageAmountBeliefs shouldBe 1.5
            pgpEval.averageAmountOperations shouldBe 2.25
            pgpEval.amountGeneralPlan shouldBe 0
            pgpEval.amountUselessPlans shouldBe 0
            pgpEval.amountInadequateUsageGoals shouldBe 1
            pgpEval.amountInadequateUsageBeliefs shouldBe 2
            pgpEval.amountInadequateUsageActions shouldBe 0
        }
    }
}
