package it.unibo.jakta.evals.retrievers.plandata

import it.unibo.jakta.evals.retrievers.Retriever
import java.io.File

interface PGPDataRetriever : Retriever<PlanData> {
    fun buildInvocationContext(
        masLogFile: File,
        agentLogFile: File,
    ): InvocationContext

    fun buildPGPInvocation(
        agentLogStream: File,
        pgpLogStream: File,
    ): PGPInvocation
}
