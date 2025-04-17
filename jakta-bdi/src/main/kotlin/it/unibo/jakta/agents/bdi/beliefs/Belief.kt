package it.unibo.jakta.agents.bdi.beliefs

import it.unibo.jakta.agents.bdi.beliefs.impl.BeliefImpl
import it.unibo.jakta.agents.bdi.parsing.Templated
import it.unibo.jakta.agents.bdi.parsing.templates.LiteratePrologTemplate
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var

interface Belief : Templated {
    val rule: Rule

    fun applySubstitution(substitution: Substitution): Belief

    companion object {

        val SOURCE_PERCEPT: Term = Struct.of("source", Atom.of("percept"))
        val SOURCE_SELF: Term = Struct.of("source", Atom.of("self"))
        val SOURCE_UNKNOWN: Term = Struct.of("source", Var.of("Source"))

        fun wrap(
            head: Struct,
            body: Iterable<Term> = emptyList(),
            wrappingTag: Term = SOURCE_UNKNOWN,
            template: LiteratePrologTemplate? = null,
            slotValues: List<Pair<String, String>> = emptyList(),
        ): Belief {
            if (head.arity >= 1 && head[0].let { it is Struct && it.arity == 1 && it.functor == "source" }) {
                return BeliefImpl(Rule.of(head, body), template, slotValues)
            }
            return BeliefImpl(Rule.of(head.addFirst(wrappingTag), body), template, slotValues)
        }

        fun of(
            head: Struct,
            body: Iterable<Term>,
            isFromPerceptSource: Boolean,
            template: LiteratePrologTemplate? = null,
            slotValues: List<Pair<String, String>> = emptyList(),
        ): Belief {
            val headArguments = (if (isFromPerceptSource) listOf(SOURCE_PERCEPT) else listOf(SOURCE_SELF)) + head.args
            return BeliefImpl(
                Rule.of(
                    Struct.of(head.functor, headArguments),
                    body,
                ),
                template,
                slotValues,
            )
        }

        fun of(
            head: Struct,
            body: Iterable<Term>,
            from: String,
            template: LiteratePrologTemplate? = null,
            slotValues: List<Pair<String, String>> = emptyList(),
        ): Belief {
            val headArguments = listOf(Struct.of("source", Atom.of(from))) + head.args
            return BeliefImpl(
                Rule.of(
                    Struct.of(head.functor, headArguments),
                    body,
                ),
                template,
                slotValues,
            )
        }

        fun fromSelfSource(head: Struct, vararg body: Term): Belief =
            fromSelfSource(head, body.asIterable())

        fun fromSelfSource(head: Struct, body: Sequence<Term>): Belief =
            fromSelfSource(head, body.asIterable())

        fun fromSelfSource(head: Struct, body: Iterable<Term>): Belief =
            of(head, body, false)

        fun fromPerceptSource(head: Struct, vararg body: Term): Belief =
            fromPerceptSource(head, body.asIterable())

        fun fromPerceptSource(head: Struct, body: Sequence<Term>): Belief =
            fromPerceptSource(head, body.asIterable())

        fun fromPerceptSource(head: Struct, body: Iterable<Term>): Belief =
            of(head, body, true)

        fun fromMessageSource(from: String, head: Struct, vararg body: Term): Belief =
            fromMessageSource(from, head, body.asIterable())

        fun fromMessageSource(from: String, head: Struct, body: Sequence<Term>): Belief =
            fromMessageSource(from, head, body.asIterable())

        fun fromMessageSource(from: String, head: Struct, body: Iterable<Term>): Belief =
            of(head, body, from)

        fun from(
            rule: Rule,
            template: LiteratePrologTemplate? = null,
            slotValues: List<Pair<String, String>> = emptyList(),
        ): Belief {
            if (rule.head.args.isNotEmpty() &&
                rule.head.args.first() is Struct &&
                rule.head.args.first().castToStruct().functor == "source"
            ) {
                return BeliefImpl(rule, template, slotValues)
            }
            throw IllegalArgumentException("The rule is not a belief: $rule")
        }

        fun from(struct: Struct): Belief = from(Rule.of(struct))
    }
}
