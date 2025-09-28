package it.unibo.jakta.exp.ablation.exp

import it.unibo.jakta.agents.bdi.engine.generation.GenerationStrategy
import it.unibo.jakta.agents.bdi.generationstrategies.lm.Remark
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.SystemPromptBuilder.Companion.createSystemPrompt
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.UserPromptBuilder.Companion.createUserPrompt
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.LMGenerationStrategy
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.impl.GenerationStrategies.lmConfig
import it.unibo.jakta.exp.GenerationStrategyFactory
import it.unibo.jakta.exp.MockGenerationStrategy.createLMGenStrategyWithMockedAPI
import it.unibo.jakta.exp.MockGenerationStrategy.getChatMessages
import it.unibo.jakta.exp.options.LmServerConfig
import it.unibo.jakta.exp.options.ModelConfig
import it.unibo.jakta.exp.options.PromptConfig
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileNotFoundException
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DefaultGenStrategyFactory : GenerationStrategyFactory {
    override fun createGenerationStrategy(
        lmServerCfg: LmServerConfig,
        modelCfg: ModelConfig,
        promptCfg: PromptConfig,
        logger: Logger,
        replayExp: Boolean,
        expReplayPath: String?,
    ): GenerationStrategy =
        lmConfig {
            url = lmServerCfg.lmServerUrl
            token = lmServerCfg.lmServerToken
            requestTimeout = lmServerCfg.requestTimeout.toDuration(DurationUnit.MILLISECONDS)
            connectTimeout = lmServerCfg.connectTimeout.toDuration(DurationUnit.MILLISECONDS)
            socketTimeout = lmServerCfg.socketTimeout.toDuration(DurationUnit.MILLISECONDS)

            model = modelCfg.modelId
            maxTokens = modelCfg.maxTokens
            temperature = modelCfg.temperature
            topP = modelCfg.topP
            reasoningEffort = modelCfg.reasoningEffort?.effort

            contextFilters = promptCfg.contextFilters
            useAslSyntax = promptCfg.useAslSyntax

            val remarks =
                promptCfg.remarks?.let {
                    try {
                        File(it)
                            .readLines()
                            .filter { l -> l.isNotBlank() }
                            .map(::Remark)
                    } catch (_: FileNotFoundException) {
                        emptyList()
                    }
                } ?: emptyList()

            systemPromptBuilder =
                createSystemPrompt(
                    promptCfg.aslSyntaxExplanationLevel,
                    promptCfg.withBdiAgentDefinition,
                    promptCfg.fewShot,
                    promptCfg.promptTechnique,
                    promptCfg.useAslSyntax,
                    promptCfg.promptSnippetsPath,
                )
            userPromptBuilder =
                createUserPrompt(
                    promptCfg.withoutAdmissibleBeliefsAndGoals,
                    promptCfg.withoutLogicDescription,
                    promptCfg.withoutNlDescription,
                    promptCfg.promptTechnique,
                    promptCfg.useAslSyntax,
                    promptCfg.expectedResultExplanationLevel,
                    remarks,
                )
        }.let { cfg ->
            if (replayExp && expReplayPath != null) {
                val lmResponses = getChatMessages(expReplayPath).mapNotNull { msg -> msg.content }
                if (lmResponses.isEmpty()) {
                    logger.error("No language model's responses found")
                    createLMGenStrategyWithMockedAPI(cfg, listOf(""))
                } else {
                    createLMGenStrategyWithMockedAPI(cfg, lmResponses)
                }
            } else {
                LMGenerationStrategy.of(cfg)
            }
        }
}
