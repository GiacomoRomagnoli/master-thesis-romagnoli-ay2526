package it.unibo.jakta.exp.explorer

import it.unibo.jakta.agents.bdi.dsl.loggingConfig
import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.engine.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.engine.generation.GenerationStrategy
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.DefaultFilters.metaPlanFilter
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.DefaultFilters.printActionFilter
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.LMGenerationStrategy
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.impl.GenerationStrategies.lmConfig
import it.unibo.jakta.exp.AbstractExperiment
import it.unibo.jakta.exp.explorer.ExplorerRobot.explorerRobot
import it.unibo.jakta.exp.explorer.MockGenerationStrategy.createLMGenStrategyWithMockedAPI
import it.unibo.jakta.exp.explorer.MockGenerationStrategy.getChatMessages
import it.unibo.jakta.exp.explorer.ModuleLoader.jsonModule
import it.unibo.jakta.exp.gridworld.environment.GridWorldDsl.gridWorld
import it.unibo.jakta.exp.gridworld.environment.GridWorldEnvironment
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ExplorerExperiment : AbstractExperiment() {
    override fun createMas(
        logConfig: LoggingConfig,
        genStrat: GenerationStrategy?,
        gridWorldEnv: GridWorldEnvironment,
    ) = mas {
        executionStrategy = ExecutionStrategy.oneThreadPerAgent()
        generationStrategy = genStrat
        loggingConfig = logConfig
        modules = listOf(jsonModule)

        explorerRobot()
        gridWorld(gridWorldEnv)
    }

    override fun createLoggingConfig(expName: String) =
        loggingConfig {
            logToFile = this@ExplorerExperiment.logToFile
            logToConsole = this@ExplorerExperiment.logToConsole
            logToServer = this@ExplorerExperiment.logToServer
            logLevel = this@ExplorerExperiment.logLevel.level
            logDir = "${this@ExplorerExperiment.logDir}/$expName"
            logServerUrl = this@ExplorerExperiment.logServerUrl
        }

    override fun createGenerationStrategy() =
        lmConfig {
            url = lmServerUrl
            token = lmServerToken
            model = modelId
            maxTokens = this@ExplorerExperiment.maxTokens
            temperature = this@ExplorerExperiment.temperature
            topP = this@ExplorerExperiment.topP
            requestTimeout = 240.seconds
            systemPromptBuilder = this@ExplorerExperiment.systemPromptType.builder
            userPromptBuilder = this@ExplorerExperiment.userPromptType.builder
            contextFilters = listOf(metaPlanFilter, printActionFilter)
            requestTimeout = this@ExplorerExperiment.requestTimeout.toDuration(DurationUnit.MILLISECONDS)
            connectTimeout = this@ExplorerExperiment.connectTimeout.toDuration(DurationUnit.MILLISECONDS)
            socketTimeout = this@ExplorerExperiment.socketTimeout.toDuration(DurationUnit.MILLISECONDS)
        }.let { cfg ->
            if (this.replayExp) {
                val lmResponses = getChatMessages(this.expReplayPath).mapNotNull { msg -> msg.content }
                createLMGenStrategyWithMockedAPI(cfg, lmResponses)
            } else {
                LMGenerationStrategy.of(cfg)
            }
        }
}
