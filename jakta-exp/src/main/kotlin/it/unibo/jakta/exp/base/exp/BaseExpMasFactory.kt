package it.unibo.jakta.exp.base.exp

import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.engine.Mas
import it.unibo.jakta.agents.bdi.engine.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.engine.generation.GenerationStrategy
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.exp.GridWorldEnvironment
import it.unibo.jakta.exp.MasFactory
import it.unibo.jakta.exp.base.BaseExpRunner.jsonModule
import it.unibo.jakta.exp.base.explorer.ExplorerRobot.explorerRobot
import it.unibo.jakta.exp.base.gridworld.environment.GridWorldDsl.gridWorld

internal class BaseExpMasFactory : MasFactory {
    override fun createMas(
        logConfig: LoggingConfig,
        genStrat: GenerationStrategy?,
        gridWorldEnv: GridWorldEnvironment,
    ): Mas =
        mas {
            executionStrategy = ExecutionStrategy.oneThreadPerAgent()
            generationStrategy = genStrat
            loggingConfig = logConfig
            modules = listOf(jsonModule)

            explorerRobot()
            gridWorld() // The env type is not configurable here so `gridWorldEnv` is ignored
        }
}
