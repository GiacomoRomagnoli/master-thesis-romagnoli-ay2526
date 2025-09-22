package it.unibo.jakta.invoker

import com.github.ajalt.clikt.core.main
import it.unibo.jakta.exp.ablation.exp.DefaultGenStrategyFactory
import it.unibo.jakta.exp.ablation.exp.DefaultLoggingConfigFactory
import it.unibo.jakta.exp.base.BaseExpRunner.modulesToLoad
import it.unibo.jakta.exp.base.exp.BaseExpMasFactory

fun main(args: Array<String>) =
    LMInvoker(
        BaseExpMasFactory(),
        DefaultLoggingConfigFactory(),
        DefaultGenStrategyFactory(),
        modulesToLoad,
    ).main(args)
