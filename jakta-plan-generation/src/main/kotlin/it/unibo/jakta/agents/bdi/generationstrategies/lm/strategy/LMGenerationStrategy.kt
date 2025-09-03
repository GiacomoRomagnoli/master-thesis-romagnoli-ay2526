package it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import it.unibo.jakta.agents.bdi.engine.generation.GenerationState
import it.unibo.jakta.agents.bdi.engine.generation.GenerationStrategy
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig.LMGenerationConfigContainer
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.generation.LMPlanGenerator
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.Parser
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.RequestHandler
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.RequestProcessor
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.impl.LMGenerationStrategyImpl
import kotlin.time.DurationUnit
import kotlin.time.toDuration

interface LMGenerationStrategy : GenerationStrategy {
    override val generator: LMPlanGenerator
    override val generationConfig: LMGenerationConfig

    companion object {
        val configErrorMsg: (GenerationState) -> String = {
            "Expected a LMGenerationState but got ${it.javaClass.simpleName}"
        }

        fun of(
            planGenerator: LMPlanGenerator,
            lmGenCfg: LMGenerationConfigContainer,
        ): LMGenerationStrategy = LMGenerationStrategyImpl(planGenerator, lmGenCfg)

        fun of(lmGenCfg: LMGenerationConfigContainer): LMGenerationStrategy {
            val api = createOpenAIApi(lmGenCfg)
            val requestProcessor = RequestProcessor.of()
            val requestHandler = RequestHandler.of(lmGenCfg, api, requestProcessor)
            val responseParser = Parser.create()
            val planGenerator = LMPlanGenerator.of(requestHandler, responseParser)

            return LMGenerationStrategyImpl(planGenerator, lmGenCfg)
        }

        private fun createOpenAIApi(lmGenCfg: LMGenerationConfigContainer): OpenAI {
            val host = OpenAIHost(baseUrl = lmGenCfg.lmServerUrl)
            val config =
                OpenAIConfig(
                    host = host,
                    token = lmGenCfg.lmServerToken,
                    logging = LoggingConfig(logger = Logger.Simple),
                    timeout =
                        Timeout(
                            request = lmGenCfg.requestTimeout.toDuration(DurationUnit.MILLISECONDS),
                            connect = lmGenCfg.connectTimeout.toDuration(DurationUnit.MILLISECONDS),
                            socket = lmGenCfg.socketTimeout.toDuration(DurationUnit.MILLISECONDS),
                        ),
                )
            return OpenAI(config)
        }
    }
}
