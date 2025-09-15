package it.unibo.jakta.exp.ablation.gridworld.configuration

import it.unibo.jakta.exp.ablation.gridworld.configuration.GridWorldConfigs.channelEnv
import it.unibo.jakta.exp.ablation.gridworld.configuration.GridWorldConfigs.hShapeEnv
import it.unibo.jakta.exp.ablation.gridworld.configuration.GridWorldConfigs.standardEnv
import it.unibo.jakta.exp.ablation.gridworld.environment.AblationGridWorldEnvironment

enum class EnvironmentType(
    val config: AblationGridWorldEnvironment,
) {
    HShape(hShapeEnv),
    Channel(channelEnv),
    Standard(standardEnv),
}
