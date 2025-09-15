package it.unibo.jakta.evals.retrievers.plandata

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.evals.SharedUtils.PGP_ID
import it.unibo.jakta.evals.SharedUtils.testAgentLogFile
import it.unibo.jakta.evals.SharedUtils.testMasLogFile
import it.unibo.jakta.evals.SharedUtils.testPgpLogFile
import it.unibo.jakta.exp.ecai.EcaiExpRunner.modulesToLoad

class PlanDataRetrievalTest : FunSpec() {
    init {
        JaktaKoin.loadAdditionalModules(modulesToLoad)

        test("PlanDataRetriever should retrieve PlanData from streams") {
            val retriever = EcaiPGPDataRetriever(testMasLogFile, testAgentLogFile, testPgpLogFile, PGP_ID)
            val planData = retriever.retrieve()
            val pgpInvocation = planData.pgpInvocation
            val invocationContext = planData.invocationContext

//            pgpInvocation.history.forEach { println(it.content) }

            invocationContext.plans.size shouldBe 0
            invocationContext.admissibleGoals.size shouldBe 1
            invocationContext.admissibleBeliefs.size shouldBe 5
            invocationContext.actions.size shouldBe 7

            pgpInvocation.history.size shouldBe 3
            pgpInvocation.generatedPlans.size shouldBe 4
            pgpInvocation.generatedAdmissibleGoals.size shouldBe 1
            pgpInvocation.generatedAdmissibleBeliefs.size shouldBe 2
            pgpInvocation.plansNotParsed shouldBe 0
            pgpInvocation.admissibleGoalsNotParsed shouldBe 0
            pgpInvocation.admissibleBeliefNotParsed shouldBe 0
            pgpInvocation.completionTime shouldBe 37L
            pgpInvocation.executable shouldBe true
            pgpInvocation.achievesGoal shouldBe true
        }
    }
}
