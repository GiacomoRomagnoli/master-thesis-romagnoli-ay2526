package it.unibo.jakta.exp.ecai

import com.github.ajalt.clikt.core.main
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializersModuleProvider
import it.unibo.jakta.agents.bdi.generationstrategies.lm.serialization.LMPlanGenJsonModule
import it.unibo.jakta.exp.Experiment
import it.unibo.jakta.exp.ablation.exp.DefaultGenStrategyFactory
import it.unibo.jakta.exp.ablation.exp.DefaultLoggingConfigFactory
import it.unibo.jakta.exp.ecai.exp.EcaiMasFactory
import it.unibo.jakta.exp.ecai.explorer.serialization.ExplorerJsonModule
import it.unibo.jakta.exp.ecai.gridworld.serialization.GridWorldJsonModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

object EcaiExpRunner {
    val jsonModule =
        module {
            single<SerializersModuleProvider>(named("LMPlanGenJsonModule")) { LMPlanGenJsonModule() }
            single<SerializersModuleProvider>(named("ExplorerJsonModule")) { ExplorerJsonModule() }
            single<SerializersModuleProvider>(named("GridWorldJsonModule")) { GridWorldJsonModule() }
        }
    val modulesToLoad = listOf(JaktaKoin.engineJsonModule, jsonModule)

    @JvmStatic
    fun main(args: Array<String>) =
        Experiment(
            EcaiMasFactory(),
            DefaultLoggingConfigFactory(),
            DefaultGenStrategyFactory(),
            modulesToLoad,
        ).main(args)
}
