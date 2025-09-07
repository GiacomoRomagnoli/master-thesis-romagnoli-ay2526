package it.unibo.jakta.exp.explorer

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.beliefs.BeliefMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.goals.TriggerMetadata.meaning
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.LMGenerationStrategy

object ExplorerRobot {
    fun MasScope.explorerRobot(
        plans: Iterable<Plan>? = null,
        strategy: LMGenerationStrategy? = null,
    ) = agent("ExplorerRobot") {
        generationStrategy = strategy
        goals {
            admissible {
                +achieve("reach"("Object")).meaning {
                    "reach a situation where ${args[0]} is in the position of the agent"
                }
            }
            +achieve("reach"("home"))
        }
        plans {
            +"current_position"(X, Y).fromPercept onlyIf {
                "object"("home", X, Y)
            } then {
                add("reached"("home"))
                execute("stop")
            }
        }
        beliefs {
            admissible {
                +fact { "grid_size"("X", "Y") }.meaning {
                    "the grid has width ${args[0]} and height ${args[1]}"
                }
                +fact { "current_position"("X", "Y") }.meaning {
                    "the agent is at coordinates (${args[0]}, ${args[1]})"
                }
                +fact { "object"("Object", "X", "Y") }.meaning {
                    "${args[0]} is at coordinates (${args[1]}, ${args[2]})"
                }
                +fact { "obstacle"("X", "Y") }.meaning {
                    "there is an obstacle at coordinates (${args[0]}, ${args[1]})"
                }
                +fact { "valid_move"("XStart", "YStart", "XEnd", "YEnd") }.meaning {
                    "the agent can move from (${args[0]}, ${args[1]}) to (${args[2]}, ${args[3]})"
                }
            }
        }
        plans?.let { plans(it) }
    }
}
