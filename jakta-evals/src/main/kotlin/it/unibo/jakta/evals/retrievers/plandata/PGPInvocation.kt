package it.unibo.jakta.evals.retrievers.plandata

import com.aallam.openai.api.chat.ChatMessage
import it.unibo.jakta.agents.bdi.engine.beliefs.AdmissibleBelief
import it.unibo.jakta.agents.bdi.engine.events.AdmissibleGoal
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PGPInvocation")
data class PGPInvocation(
    val pgpId: String?,
    val history: List<ChatMessage>,
    val rawMessageContents: List<String>,
    val generatedPlans: List<Plan> = emptyList(),
    val generatedAdmissibleGoals: List<AdmissibleGoal> = emptyList(),
    val generatedAdmissibleBeliefs: List<AdmissibleBelief> = emptyList(),
    val plansNotParsed: Int = 0,
    val admissibleGoalsNotParsed: Int = 0,
    val admissibleBeliefNotParsed: Int = 0,
    val completionTime: Long? = 0,
    val executable: Boolean = true,
    val achievesGoal: Boolean = true,
    val generationConfig: LMGenerationConfig? = null,
    val chatCompletionId: String? = null,
)
