package it.unibo.jakta.exp.ablation

import com.github.ajalt.clikt.core.main
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializersModuleProvider
import it.unibo.jakta.agents.bdi.generationstrategies.lm.serialization.LMPlanGenJsonModule
import it.unibo.jakta.exp.Experiment
import it.unibo.jakta.exp.ablation.exp.DefaultGenStrategyFactory
import it.unibo.jakta.exp.ablation.exp.DefaultLoggingConfigFactory
import it.unibo.jakta.exp.ablation.exp.DefaultMasFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module

object AblationExpRunner {
    val jsonModule =
        module {
            single<SerializersModuleProvider>(named("LMPlanGenJsonModule")) { LMPlanGenJsonModule() }
        }
    val modulesToLoad = listOf(JaktaKoin.engineJsonModule, jsonModule)

    @JvmStatic
    fun main(args: Array<String>) =
        Experiment(
            DefaultMasFactory(),
            DefaultLoggingConfigFactory(),
            DefaultGenStrategyFactory(),
            modulesToLoad,
        ).main(args)
}
