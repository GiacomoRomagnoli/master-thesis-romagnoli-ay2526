package it.unibo.jakta.exp

import it.unibo.jakta.exp.ablation.gridworld.configuration.GridWorldConfigs.channelEnv
import it.unibo.jakta.exp.ablation.gridworld.environment.AblationGridWorldEnvironment

fun main() {
    val gridWorld = channelEnv

    println("Initial state:")
    println(gridWorld.data.values.joinToString("\n"))
    println("\nInitial perception:")
    println(gridWorld.percept().joinToString("\n"))

    // Recursive movement test
    fun performMovements(
        world: AblationGridWorldEnvironment,
        moves: Int,
        count: Int = 0,
    ) {
        if (count >= moves) return

        println("\n--- Move ${count + 1} ---")
        val updatedGridWorld =
            world.parseAction("move(north)")?.let { newState ->
                world.copy(data = mapOf("state" to newState))
            }

        println("New perception:")
        println(updatedGridWorld?.percept()?.joinToString("\n"))

        performMovements(updatedGridWorld ?: world, moves, count + 1)
    }

    println("\n=== Performing 5 moves north ===")
    performMovements(gridWorld, 5)

    // Test with different directions
    println("\n=== Testing multiple directions ===")
    var testWorld = gridWorld
    val testMoves = listOf("north", "east", "south", "west", "north")

    testMoves.forEachIndexed { index, direction ->
        println("\nMove ${index + 1}: $direction")
        testWorld = testWorld.parseAction("move($direction)")?.let { newState ->
            testWorld.copy(data = mapOf("state" to newState))
        } ?: testWorld

        println("Perception: ${testWorld.percept().joinToString()}")
    }
}
