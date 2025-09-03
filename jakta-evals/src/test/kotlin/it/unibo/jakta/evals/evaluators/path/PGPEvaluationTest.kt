package it.unibo.jakta.evals.evaluators.path

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.engine.FileUtils.getResourceAsFile
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin.engineJsonModule
import it.unibo.jakta.agents.bdi.engine.visitors.GuardFlattenerVisitor.Companion.flatten
import it.unibo.jakta.evals.evaluators.pgp.PGPEvaluator
import it.unibo.jakta.evals.retrievers.plandata.PlanDataRetriever
import it.unibo.jakta.exp.explorer.ModuleLoader.jsonModule
import it.unibo.tuprolog.core.Truth

class PGPEvaluationTest : FunSpec() {
    val masId = "fb90e237-4422-4c11-bdd2-6dc1058c2fca"
    val agentId = "cc336e14-00da-4ea8-9815-b09550406dd3"
    val pgpId = "37669cb0-20cf-4760-b9a6-1556f04dcd65"

    init {
        JaktaKoin.loadAdditionalModules(engineJsonModule, jsonModule)

        test("PGPEvaluator should evaluate plans correctly from test logs") {
            val masLogFile =
                getResourceAsFile("/testLogs/Mas-$masId.jsonl")
                    ?: error("Could not find mas test log file")
            val agentLogFile =
                getResourceAsFile("/testLogs/Mas-$masId-ExplorerRobot-$agentId.jsonl")
                    ?: error("Could not find agent test log file")
            val pgpLogFile =
                getResourceAsFile("/testLogs/Mas-$masId-ExplorerRobot-$agentId-trusting_nightingale-$pgpId.jsonl")
                    ?: error("Could not find pgp test log file")

            val retriever =
                PlanDataRetriever(
                    masLogFile = masLogFile,
                    agentLogFile = agentLogFile,
                    pgpLogFile = pgpLogFile,
                    masId = masId,
                    agentId = agentId,
                    pgpId = pgpId,
                )

            val planData = retriever.retrieve()

            val pgpInvocation = planData.pgpInvocation
            val invocationContext = planData.invocationContext

            val evaluator = PGPEvaluator(invocationContext, pgpInvocation)
            val result = evaluator.eval()

            result.masId shouldBe masId
            result.agentId shouldBe agentId
            result.pgpId shouldBe pgpId

            result.amountGeneratedPlans shouldBe pgpInvocation.generatedPlans.size
            result.amountInventedGoals shouldBe pgpInvocation.generatedAdmissibleGoals.size
            result.amountInventedBeliefs shouldBe pgpInvocation.generatedAdmissibleBeliefs.size
            result.amountNotParseablePlans shouldBe pgpInvocation.plansNotParsed
            result.amountInadequateUsageGoals shouldBe result.amountInadequateUsageGoals
            result.amountInadequateUsageBeliefs shouldBe result.amountInadequateUsageBeliefs
            result.amountInadequateUsageActions shouldBe result.amountInadequateUsageActions

            result.averageAmountBeliefs shouldBe
                result.parsedPlans.map { it.guard.flatten().size }.average()
            result.averageAmountOperations shouldBe
                result.parsedPlans.map { it.goals.filterNot { g -> g.value == Truth.TRUE }.size }.average()
        }
    }
}
