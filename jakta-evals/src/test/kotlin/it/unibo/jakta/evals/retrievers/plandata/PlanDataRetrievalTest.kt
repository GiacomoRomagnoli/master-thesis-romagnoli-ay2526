package it.unibo.jakta.evals.retrievers.plandata

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.engine.FileUtils.getResourceAsFile
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin.engineJsonModule
import it.unibo.jakta.exp.explorer.ModuleLoader.jsonModule

class PlanDataRetrievalTest : FunSpec() {
    init {
        JaktaKoin.loadAdditionalModules(engineJsonModule, jsonModule)

        test("PlanDataRetriever should retrieve PlanData from streams") {
            val masLogFile =
                getResourceAsFile("/testLogs/Mas-fb90e237-4422-4c11-bdd2-6dc1058c2fca.jsonl")
                    ?: error("Could not find mas test log file")
            val agentLogFile =
                getResourceAsFile(
                    "/testLogs/Mas-fb90e237-4422-4c11-bdd2-6dc1058c2fca-ExplorerRobot-cc336e14-00da-4ea8-9815-b09550406dd3.jsonl",
                )
                    ?: error("Could not find agent test log file")
            val pgpLogFile =
                getResourceAsFile(
                    "/testLogs/Mas-fb90e237-4422-4c11-bdd2-6dc1058c2fca-ExplorerRobot-cc336e14-00da-4ea8-9815-b09550406dd3-trusting_nightingale-37669cb0-20cf-4760-b9a6-1556f04dcd65.jsonl",
                )
                    ?: error("Could not find pgp test log file")

            val masId = "fb90e237-4422-4c11-bdd2-6dc1058c2fca"
            val agentId = "cc336e14-00da-4ea8-9815-b09550406dd3"
            val pgpId = "37669cb0-20cf-4760-b9a6-1556f04dcd65"

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

            planData.pgpInvocation.history.forEach { println(it.content) }

            planData.invocationContext.plans.size shouldBe 0
            planData.invocationContext.actions.size shouldBe 5

            planData.pgpInvocation.generatedPlans.size shouldBe 4
            planData.pgpInvocation.executable shouldBe true
        }
    }
}
