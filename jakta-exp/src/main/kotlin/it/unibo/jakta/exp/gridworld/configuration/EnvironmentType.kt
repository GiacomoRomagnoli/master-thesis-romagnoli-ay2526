package it.unibo.jakta.exp.gridworld.configuration

import it.unibo.jakta.exp.gridworld.configuration.GridWorldConfigs.channelEnv
import it.unibo.jakta.exp.gridworld.configuration.GridWorldConfigs.hShapeEnv
import it.unibo.jakta.exp.gridworld.configuration.GridWorldConfigs.standardEnv
import it.unibo.jakta.exp.gridworld.environment.GridWorldEnvironment

enum class EnvironmentType(
    val config: GridWorldEnvironment,
) {
    HShape(hShapeEnv),
    Channel(channelEnv),
    Standard(standardEnv),
}
