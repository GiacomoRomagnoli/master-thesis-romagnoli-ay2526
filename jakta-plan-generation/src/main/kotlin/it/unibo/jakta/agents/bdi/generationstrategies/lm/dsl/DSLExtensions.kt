package it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl

import it.unibo.jakta.agents.bdi.dsl.AgentScope
import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.plans.PlanScope
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.impl.GenerationStrategies

object DSLExtensions {
    fun MasScope.lmGeneration(config: LMGenerationConfigScope.() -> Unit): MasScope {
        generationStrategy = GenerationStrategies.lmGeneration(config)
        return this
    }

    fun AgentScope.lmGeneration(config: LMGenerationConfigScope.() -> Unit): AgentScope {
        generationStrategy = GenerationStrategies.lmGeneration(config)
        return this
    }

    infix fun PlanScope.givenLMConfig(given: LMGenerationConfigScope.() -> Unit): PlanScope {
        generationConfig = LMGenerationConfigScope().also(given).build()
        return this
    }
}
