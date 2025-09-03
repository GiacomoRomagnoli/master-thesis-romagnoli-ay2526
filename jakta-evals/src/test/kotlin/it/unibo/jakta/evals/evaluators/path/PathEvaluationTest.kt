package it.unibo.jakta.evals.evaluators.path

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.unibo.jakta.exp.gridworld.model.Grid
import it.unibo.jakta.exp.gridworld.model.Position

class PathEvaluationTest :
    FunSpec({

        test("LLM path matches optimal path") {
            val grid = Grid(5, 5, emptySet())
            val start = Position(0, 0)
            val goal = Position(2, 2)
            val scenario = SearchScenario(grid, start, goal)

            val optimalResult = AStarSearch.findPath(grid, start, goal) as? SearchResult.Success
            val llmPath = optimalResult?.path

            val evaluator = PathEvaluator(scenario, llmPath)
            val result = evaluator.eval()

            result.isValidPlan shouldBe true
            result.isSuccess shouldBe true
            result.llmPathLength shouldBe optimalResult?.path?.size
            result.optimalPathLength shouldBe optimalResult?.path?.size
            result.plr shouldBe 1.0
            result.excessSteps shouldBe 0
        }

        test("LLM path is invalid") {
            val grid = Grid(5, 5, emptySet())
            val start = Position(0, 0)
            val goal = Position(2, 2)
            val scenario = SearchScenario(grid, start, goal)

            // Invalid path: jumps across grid
            val llmPath = listOf(start, Position(4, 4), goal)

            val evaluator = PathEvaluator(scenario, llmPath)
            val result = evaluator.eval()

            result.isValidPlan shouldBe false
            result.isSuccess shouldBe false
            result.llmPathLength shouldBe llmPath.size
            result.optimalPathLength shouldNotBe 0
        }

        test("No LLM path provided") {
            val grid = Grid(5, 5, emptySet())
            val start = Position(0, 0)
            val goal = Position(2, 2)
            val scenario = SearchScenario(grid, start, goal)

            val evaluator = PathEvaluator(scenario, null)
            val result = evaluator.eval()

            result.isValidPlan shouldBe false
            result.isSuccess shouldBe false
            result.llmPathLength shouldBe null
        }
    })
