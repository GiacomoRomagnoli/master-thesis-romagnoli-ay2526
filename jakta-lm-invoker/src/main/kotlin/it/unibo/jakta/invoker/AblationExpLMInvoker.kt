package it.unibo.jakta.invoker

import com.github.ajalt.clikt.core.main
import it.unibo.jakta.exp.ablation.AblationExpRunner.modulesToLoad
import it.unibo.jakta.exp.ablation.exp.DefaultGenStrategyFactory
import it.unibo.jakta.exp.ablation.exp.DefaultLoggingConfigFactory
import it.unibo.jakta.exp.ablation.exp.DefaultMasFactory

fun main(args: Array<String>) =
    LMInvoker(
        DefaultMasFactory(),
        DefaultLoggingConfigFactory(),
        DefaultGenStrategyFactory(),
        modulesToLoad,
    ).main(args)
