package it.unibo.jakta.exp.ablation.explorer

import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.beliefs.BeliefMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.goals.TriggerMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.plans.PlanMetadata.meaning
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.LMGenerationStrategy

// TODO remove code duplication
// CPD-OFF
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
                "object"(O, X, Y).fromPercept
            } then {
                add("reached"(O))
                execute("print"("reached object: ", O))
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
                +fact { "obstacle"("Direction") }.meaning {
                    "there is an $functor to the ${args[0]}"
                }
                +fact { "free"("Direction") }.meaning {
                    "there isn't an obstacle to the ${args[0]}"
                }
                +fact { "there_is"("Object", "Direction") }.meaning {
                    "there is an ${args[0]} in the given ${args[1]}"
                }
                +fact { "direction"("Direction") }.meaning {
                    "${args[0]} is a direction"
                }
            }
        }
        plans?.let { plans(it) }
    }
}
// CPD-ON
