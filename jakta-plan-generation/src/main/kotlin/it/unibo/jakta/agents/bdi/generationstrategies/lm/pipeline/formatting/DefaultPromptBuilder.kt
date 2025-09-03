package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting

import it.unibo.jakta.agents.bdi.engine.actions.Action
import it.unibo.jakta.agents.bdi.engine.beliefs.AdmissibleBelief
import it.unibo.jakta.agents.bdi.engine.beliefs.Belief
import it.unibo.jakta.agents.bdi.engine.events.AdmissibleGoal
import it.unibo.jakta.agents.bdi.engine.events.Trigger
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.actionsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.actionsFormatterWithoutHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleBeliefsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleBeliefsFormatterWithoutHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleGoalsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleGoalsFormatterWithoutHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.beliefsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.beliefsFormatterWithoutHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.goalFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.triggerFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.Formatter
import it.unibo.jakta.agents.bdi.engine.goals.Goal
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.PromptBuilder.Companion.formatAsBulletList
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.impl.SystemPromptBuilderImpl.Companion.system
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.impl.UserPromptBuilderImpl.Companion.user

object DefaultPromptBuilder {
    val defaultSystemPrompt =
        system("SystemPrompt") {
            section("System Message") {
                fromFile("system.md")
            }
        }

    fun createUserPrompt(
        name: String,
        withRemarks: Boolean,
        admissibleBeliefsFormatter: Formatter<AdmissibleBelief>,
        beliefsFormatter: Formatter<Belief>,
        admissibleGoalsFormatter: Formatter<AdmissibleGoal>,
        actionsFormatter: Formatter<Action<*, *, *>>,
        triggerFormatter: Formatter<Trigger>,
        goalFormatter: Formatter<Goal>,
    ) = user(name) { ctx ->
        section("User Message") {
            fromString("Below is your internal state and the specific goal I need you to plan for.")

            section("Agent's internal state") {
                section("Beliefs") {
                    section("Admissible beliefs") {
                        fromFormatter(ctx.admissibleBeliefs) {
                            formatAsBulletList(it, admissibleBeliefsFormatter::format)
                        }
                    }

                    section("Actual beliefs") {
                        fromFormatter(ctx.beliefs.asIterable().toList()) {
                            formatAsBulletList(it, beliefsFormatter::format)
                        }
                    }
                }

                section("Goals") {
                    section("Admissible goals") {
                        fromFormatter(ctx.admissibleGoals) {
                            formatAsBulletList(it, admissibleGoalsFormatter::format)
                        }
                    }

                    section("Actual goals") {
                        fromFormatter(ctx.goals) { plans ->
                            val triggers = plans.map { it.trigger }
                            formatAsBulletList(triggers, triggerFormatter::format)
                        }
                    }
                }

                section("Admissible actions") {
                    fromFormatter(ctx.internalActions + ctx.externalActions) {
                        formatAsBulletList(it, actionsFormatter::format)
                    }
                }

                if (withRemarks) {
                    section("Remarks") {
                        fromFormatter(ctx.remarks) { r ->
                            formatAsBulletList(r) { it.value }
                        }
                    }
                }
            }

            section("Expected outcome") {
                val formattedGoal = goalFormatter.format(ctx.initialGoal.goal)
                fromString("Create plans to pursue the goal: $formattedGoal.")
                fromString(
                    """
                    Output only the final set of plans with no alternatives or intermediate attempts. 
                    End with an additional YAML block that contains a list of any new admissible goals and beliefs you invented, including their natural language interpretation.
                    """.trimIndent(),
                )
            }
        }
    }

    val userPromptWithHintsAndRemarks =
        createUserPrompt(
            name = "UserMessageWithHintsAndRemarks",
            withRemarks = true,
            admissibleBeliefsFormatter = admissibleBeliefsFormatter,
            beliefsFormatter = beliefsFormatter,
            admissibleGoalsFormatter = admissibleGoalsFormatter,
            actionsFormatter = actionsFormatter,
            triggerFormatter = triggerFormatter,
            goalFormatter = goalFormatter,
        )

    val userPromptWithHints =
        createUserPrompt(
            name = "UserMessageWithHints",
            withRemarks = false,
            admissibleBeliefsFormatter = admissibleBeliefsFormatter,
            beliefsFormatter = beliefsFormatter,
            admissibleGoalsFormatter = admissibleGoalsFormatter,
            actionsFormatter = actionsFormatter,
            triggerFormatter = triggerFormatter,
            goalFormatter = goalFormatter,
        )

    val userPromptWithoutHints =
        createUserPrompt(
            name = "UserMessageNoHints",
            withRemarks = false,
            admissibleBeliefsFormatter = admissibleBeliefsFormatterWithoutHints,
            beliefsFormatter = beliefsFormatterWithoutHints,
            admissibleGoalsFormatter = admissibleGoalsFormatterWithoutHints,
            actionsFormatter = actionsFormatterWithoutHints,
            triggerFormatter = triggerFormatter,
            goalFormatter = goalFormatter,
        )
}
