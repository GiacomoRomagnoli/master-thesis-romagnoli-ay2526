package it.unibo.jakta.exp.ablation.exp

import it.unibo.jakta.agents.bdi.engine.FileUtils.getResourceAsFile
import it.unibo.jakta.agents.bdi.engine.generation.GenerationStrategy
import it.unibo.jakta.agents.bdi.generationstrategies.lm.Remark
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.LMGenerationStrategy
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.impl.GenerationStrategies.lmConfig
import it.unibo.jakta.exp.GenerationStrategyFactory
import it.unibo.jakta.exp.MockGenerationStrategy.createLMGenStrategyWithMockedAPI
import it.unibo.jakta.exp.MockGenerationStrategy.getChatMessages
import it.unibo.jakta.exp.options.LmServerConfig
import it.unibo.jakta.exp.options.ModelConfig
import it.unibo.jakta.exp.options.PromptConfig
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class DefaultGenStrategyFactory : GenerationStrategyFactory {
    override fun createGenerationStrategy(
        lmServerCfg: LmServerConfig,
        modelCfg: ModelConfig,
        promptCfg: PromptConfig,
        replayExp: Boolean,
        expReplayPath: String?,
    ): GenerationStrategy =
        lmConfig {
            url = lmServerCfg.lmServerUrl
            token = lmServerCfg.lmServerToken
            model = modelCfg.modelId
            maxTokens = modelCfg.maxTokens
            temperature = modelCfg.temperature
            topP = modelCfg.topP
            reasoningEffort = modelCfg.reasoningEffort?.effort
            contextFilters = promptCfg.contextFilters
            requestTimeout = lmServerCfg.requestTimeout.toDuration(DurationUnit.MILLISECONDS)
            connectTimeout = lmServerCfg.connectTimeout.toDuration(DurationUnit.MILLISECONDS)
            socketTimeout = lmServerCfg.socketTimeout.toDuration(DurationUnit.MILLISECONDS)
            remarks =
                promptCfg.remarks?.let {
                    getResourceAsFile(it)
                        ?.readLines()
                        ?.filter { l -> l.isNotBlank() }
                        ?.map(::Remark) ?: emptyList()
                }
        }.let { cfg ->
            if (replayExp && expReplayPath != null) {
                val lmResponses = getChatMessages(expReplayPath).mapNotNull { msg -> msg.content }
                createLMGenStrategyWithMockedAPI(cfg, lmResponses)
            } else {
                LMGenerationStrategy.of(cfg)
            }
        }
}
