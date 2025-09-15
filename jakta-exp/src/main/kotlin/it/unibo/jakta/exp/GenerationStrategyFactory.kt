package it.unibo.jakta.exp

import it.unibo.jakta.agents.bdi.engine.generation.GenerationStrategy
import it.unibo.jakta.exp.options.LmServerConfig
import it.unibo.jakta.exp.options.ModelConfig
import it.unibo.jakta.exp.options.PromptConfig

interface GenerationStrategyFactory {
    fun createGenerationStrategy(
        lmServerCfg: LmServerConfig,
        modelCfg: ModelConfig,
        promptCfg: PromptConfig,
        replayExp: Boolean,
        expReplayPath: String?,
    ): GenerationStrategy?
}
