package it.unibo.jakta.playground.explorer.gridworld

import it.unibo.jakta.exp.gridworld.configuration.GridWorldConfigs.channelEnv

fun main() {
    val gridWorld = channelEnv
    println(gridWorld.data.values.joinToString("\n"))
    println(gridWorld.perception.percept().joinToString("\n"))

    val updatedGridWorld =
        gridWorld.parseAction("move(0, 3, 0, 2)")?.let {
            gridWorld.copy(data = mapOf("state" to it))
        }

    println()
    println(updatedGridWorld?.perception?.percept()?.joinToString("\n"))
}
