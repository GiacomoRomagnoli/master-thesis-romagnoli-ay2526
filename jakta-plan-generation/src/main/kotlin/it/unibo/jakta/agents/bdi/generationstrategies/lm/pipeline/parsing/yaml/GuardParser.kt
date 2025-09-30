package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.yaml

import it.unibo.jakta.agents.bdi.engine.Jakta.toLeftNestedAnd
import it.unibo.jakta.agents.bdi.engine.JaktaParser.tangleStruct
import it.unibo.jakta.agents.bdi.engine.Prolog2Jakta
import it.unibo.jakta.agents.bdi.engine.visitors.SourceWrapperVisitor
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth
import kotlin.text.replace

object GuardParser {
    fun wrapBelief(struct: Struct): Struct = SourceWrapperVisitor.wrapSelectively(struct).castToStruct()

    fun processGuard(plan: PlanData): Struct? {
        val individualConditions =
            plan.conditions
                .mapNotNull { c ->
                    val text =
                        c
                            .replace("¬", "~")
                            .replace("!=", "\\=")
                            .replace(Regex("\\bnot\\b"), "~")
                            .replace("true", "True")
                            .replace("false", "False")
                            .replace("∨", "|")
                            .replace("∧", "&")
                            .replace(Regex("\\bor\\b"), "|")
                            .replace(Regex("\\band\\b"), "&")

                    if (c.contains("<none>")) {
                        Truth.TRUE
                    } else {
                        tangleStruct(text)?.accept(Prolog2Jakta)?.castToStruct()
                    }
                }.map { if (it != Truth.TRUE) wrapBelief(it) else it }

        return individualConditions.toLeftNestedAnd()
    }
}
