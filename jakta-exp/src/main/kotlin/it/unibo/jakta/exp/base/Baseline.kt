package it.unibo.jakta.exp.base

import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.dsl.plans
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.exp.base.BaseExpRunner.modulesToLoad
import it.unibo.jakta.exp.base.explorer.ExplorerRobot.explorerRobot
import it.unibo.jakta.exp.base.gridworld.environment.GridWorldDsl.gridWorld
import it.unibo.tuprolog.core.Var

private val Direction = Var.of("Direction")
private val Object = Var.of("Object")

val baselinePlans =
    plans {
        +achieve("reach"(Object)) onlyIf {
            "there_is"(Object, "here").fromPercept
        }
        +achieve("reach"(Object)) onlyIf {
            "there_is"(Object, Direction).fromPercept
        } then {
            execute("move"(Direction))
        }
        +achieve("reach"(Object)) onlyIf {
            not("there_is"(Object, `_`).fromPercept)
        } then {
            execute("getDirectionToMove"(Direction))
            execute("move"(Direction))
            achieve("reach"(Object))
        }
    }

fun main() {
    val mas =
        mas {
            modules = modulesToLoad
            loggingConfig = LoggingConfig(logToFile = true)
            gridWorld()
            explorerRobot(baselinePlans)
        }

    mas.start()
    Thread.sleep(2000)
    mas.stop()
}
