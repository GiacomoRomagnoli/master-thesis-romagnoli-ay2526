package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.schema

import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.schema.ObjectDsl.Companion.schema
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.schema.SchemaDsl.Companion.jsonSchema

object DefaultSchemas {
    val JSON_SCHEMA =
        jsonSchema {
            name("ASL plan generation")
            schema {
                property("plans", array(PLAN), "List of plans defining the agent's behavior in response to events.")
                property(
                    "newAdmissibleGoalsAndBeliefs",
                    array(NEW_ADMISSIBLE),
                    """
                    List of newly recognized goals or beliefs, 
                    each with a logical term and a natural language description.
                    """.trimIndent(),
                )
            }
        }

    val PLAN =
        schema {
            property(
                "event",
                "An event representing a change or intention that triggers the agent’s plan.",
            ) {
                property(
                    "triggerType",
                    enum("achieve"),
                    """
                    Specifies the keyword that prefixes the event, indicating how the agent should handle it.
                    achieve denotes a goal the agent actively commits to pursuing.
                    """.trimIndent(),
                )
                property(
                    "term",
                    string,
                    """
                    The logical term associated with the event, specified after the trigger keyword. 
                    For example, in achieve reach(home), it is reach(home).
                    """.trimIndent(),
                )
            }
            property(
                "conditions",
                array(string),
                "First-order logic formulas evaluated against the agent’s current beliefs to determine plan applicability.",
            )
            property(
                "operations",
                array {
                    property(
                        "operationType",
                        enum("achieve", "execute", "add", "remove", "update"),
                        """
                        Specifies the type of operation to perform.
                        e.g. achieving a goal, executing an action, or modifying beliefs.
                        """.trimIndent(),
                    )
                    property(
                        "term",
                        string,
                        """
                        The logical term or action associated with the operation.
                        e.g., reach(Home) for an achieve operation.
                        """.trimIndent(),
                    )
                },
                "A list of operations the agent should carry out when the plan is executed.",
            )
        }

    val NEW_ADMISSIBLE =
        schema {
            property("termType", enum("goal", "belief"), "Specifies whether the term is a goal or a belief.")
            property("term", string, "The logical term representing the goal or belief.")
            property("purpose", string, "Natural language interpretation of the term.")
        }
}
