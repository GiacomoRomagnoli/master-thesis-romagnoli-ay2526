package it.unibo.jakta.evals.evaluators.pgp

import it.unibo.tuprolog.core.Indicator

data class SemanticMisalignmentResult(
    val notParsed: Int,
    val alreadyAdmissible: Set<Indicator>,
    val admissibleNotUsed: Set<Indicator>,
    val usedNotAdmissible: Set<Indicator>,
)
