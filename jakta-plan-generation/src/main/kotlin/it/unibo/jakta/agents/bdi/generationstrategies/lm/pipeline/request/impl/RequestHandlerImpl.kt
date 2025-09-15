package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.impl

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig.LMGenerationConfigContainer
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationState
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMGenerationRequested
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.Parser
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.RequestHandler
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.RequestProcessor
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.result.RequestFailure
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.result.RequestResult

internal class RequestHandlerImpl(
    override val api: OpenAI?,
    override val requestProcessor: RequestProcessor,
    override val generationConfig: LMGenerationConfigContainer,
) : RequestHandler {
    override suspend fun requestTextCompletion(
        generationState: LMGenerationState,
        parser: Parser,
    ): RequestResult =
        if (api != null) {
            val request = makeTextCompletionRequest(generationConfig, generationState)
            requestProcessor.requestGeneration(api, request, generationState.logger, parser).also {
                generationState.logger?.log { LMGenerationRequested(generationConfig) }
            }
        } else {
            RequestFailure.NetworkRequestFailure("No API specified")
        }

    private fun makeTextCompletionRequest(
        cfg: LMGenerationConfigContainer,
        state: LMGenerationState,
    ) = ChatCompletionRequest(
        model = ModelId(cfg.modelId),
        temperature = cfg.temperature,
        topP = cfg.topP,
        reasoningEffort = cfg.reasoningEffort,
        messages = state.chatHistory,
        maxCompletionTokens = cfg.maxTokens,
    )
}
