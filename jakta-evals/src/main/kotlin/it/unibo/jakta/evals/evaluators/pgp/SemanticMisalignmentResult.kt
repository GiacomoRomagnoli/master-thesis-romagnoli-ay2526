package it.unibo.jakta.evals.metrics.plandata

data class SemanticMisalignmentResult(
    val notParsed: Int,
    val alreadyAdmissible: Set<Any>,
    val admissibleNotUsed: Set<Any>,
    val usedNotAdmissible: Set<Any>,
)
