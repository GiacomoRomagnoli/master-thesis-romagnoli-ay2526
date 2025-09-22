package it.unibo.jakta.exp.ablation.gridworld.configuration

import it.unibo.jakta.exp.ablation.gridworld.environment.AblationGridWorldEnvironment
import it.unibo.jakta.exp.ablation.gridworld.environment.GridWorldState
import it.unibo.jakta.exp.ablation.gridworld.model.Position
import it.unibo.jakta.exp.sharedModel.Direction

object GridWorldConfigs {
    private const val GRID_HEIGHT = 7
    private const val GRID_WIDTH = 5
    private val agentPosition = Position(0, 3)
    private val goalPosition = mapOf("home" to Position(6, 3))
    private val manhattanDirections = setOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)

    private fun positions(
        x: Int,
        yRange: IntRange,
    ) = yRange.map { Position(x, it) }.toSet()

    private fun positions(
        xRange: IntRange,
        y: Int,
    ) = xRange.map { Position(it, y) }.toSet()

    val channelEnv =
        AblationGridWorldEnvironment(
            data =
                mapOf(
                    "state" to
                        GridWorldState.of(
                            gridWidth = GRID_WIDTH,
                            gridHeight = GRID_HEIGHT,
                            agentPosition = agentPosition,
                            objectsPosition = goalPosition,
                            obstaclesPosition =
                                buildSet {
                                    addAll(positions(2, 0..5))
                                },
                            availableDirections = manhattanDirections,
                        ),
                ),
        )

    val hShapeEnv =
        AblationGridWorldEnvironment(
            data =
                mapOf(
                    "state" to
                        GridWorldState.of(
                            gridWidth = GRID_WIDTH,
                            gridHeight = GRID_HEIGHT,
                            agentPosition = agentPosition,
                            objectsPosition = goalPosition,
                            obstaclesPosition =
                                buildSet {
                                    add(Position(1, 1))
                                    add(Position(1, 5))
                                    addAll(positions(2, 1..5))
                                    add(Position(3, 1))
                                    add(Position(3, 5))
                                },
                            availableDirections = manhattanDirections,
                        ),
                ),
        )

    val standardEnv = AblationGridWorldEnvironment(data = mapOf("state" to GridWorldState()))
}
