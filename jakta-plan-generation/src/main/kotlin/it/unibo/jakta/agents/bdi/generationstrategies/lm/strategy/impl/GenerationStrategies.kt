package it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.impl

import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl.LMGenerationConfigScope
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.LMGenerationStrategy

object GenerationStrategies {
    fun lmGeneration(generationCfg: LMGenerationConfigScope.() -> Unit) =
        LMGenerationStrategy.of(lmConfig(generationCfg))

    fun lmConfig(generationCfg: LMGenerationConfigScope.() -> Unit): LMGenerationConfig.LMGenerationConfigContainer {
        val configPatch = LMGenerationConfigScope().also(generationCfg).build()
        return configPatch.patch(LMGenerationConfig.LMGenerationConfigContainer())
    }
}
