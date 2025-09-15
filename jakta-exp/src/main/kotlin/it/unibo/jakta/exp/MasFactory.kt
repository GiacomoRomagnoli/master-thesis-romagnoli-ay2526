package it.unibo.jakta.exp

import it.unibo.jakta.agents.bdi.engine.Mas
import it.unibo.jakta.agents.bdi.engine.generation.GenerationStrategy
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.exp.ablation.exp.DefaultMasFactory

interface MasFactory {
    fun createMas(
        logConfig: LoggingConfig,
        genStrat: GenerationStrategy?,
        gridWorldEnv: GridWorldEnvironment,
    ): Mas

    companion object {
        fun createDefault(): MasFactory = DefaultMasFactory()
    }
}
