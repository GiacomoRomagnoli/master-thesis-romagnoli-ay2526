package it.unibo.jakta.evals.retrievers.gendata

import it.unibo.jakta.evals.retrievers.Retriever
import kotlinx.coroutines.runBlocking

class GenerationDataRetriever(
    val client: GenerationClient,
    val chatCompletionId: String,
) : Retriever<GenerationData?> {
    override fun retrieve(): GenerationData? =
        runBlocking {
            client.use { client ->
                client.getGenerationData(chatCompletionId)
            }
        }
}
