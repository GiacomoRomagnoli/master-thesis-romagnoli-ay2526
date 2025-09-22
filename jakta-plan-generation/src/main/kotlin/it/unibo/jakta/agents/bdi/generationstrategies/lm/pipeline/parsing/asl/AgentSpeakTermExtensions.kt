package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term

object AgentSpeakTermExtensions {
    fun Term.isBeliefAddition() = this is Struct && functor == "+" && arity == 1

    fun Term.isBeliefDeletion() = this is Struct && functor == "-" && arity == 1

    fun Term.isBeliefUpdate() = this is Struct && functor == "minusPlus" && arity == 1

    fun Term.isAchievementGoal() = this is Struct && functor == "plusBang" && arity == 1

    fun Term.isComposition() = this is Struct && functor == ";" && arity == 2

    fun Term.getInnerStruct(): Struct? = (this as? Struct)?.args?.getOrNull(0) as? Struct
}
