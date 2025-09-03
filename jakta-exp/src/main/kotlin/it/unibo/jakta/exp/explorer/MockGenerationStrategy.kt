package it.unibo.jakta.exp.explorer

import com.aallam.openai.api.chat.ChatChoice
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import io.mockk.coEvery
import io.mockk.mockk
import it.unibo.jakta.agents.bdi.engine.FileUtils.processFile
import it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.extractAgentLogFiles
import it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.findMasLogFiles
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.LogFileUtils.extractPgpLogFiles
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMMessageReceived
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.generation.LMPlanGenerator
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.Parser
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.RequestHandler
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.LMGenerationStrategy

object MockGenerationStrategy {
    private var callCount = 0

    // TODO handle a reference to a file that stores the conversation in addition to a directory with the logs
    fun getChatMessages(expPath: String): List<ChatMessage> {
        val history = mutableListOf<ChatMessage>()
        val masLogFile = findMasLogFiles(expPath).firstOrNull()
        masLogFile?.let {
            extractAgentLogFiles(expPath, masLogFile).forEach { agentLogFile ->
                extractPgpLogFiles(expPath, agentLogFile).forEach { pgpLogFile ->
                    processFile(pgpLogFile) { logEntry ->
                        val event = logEntry.message.event
                        if (event is LMMessageReceived) {
                            event.chatMessage.let { history.add(it) }
                        }
                        true
                    }
                }
            }
        }
        return history
    }

    fun createLMGenStrategyWithMockedAPI(
        config: LMGenerationConfig.LMGenerationConfigContainer,
        trace: List<String>,
    ): LMGenerationStrategy {
        val api = mockk<OpenAI>()
        coEvery {
            api.chatCompletion(any<ChatCompletionRequest>())
        } answers {
            val index = callCount.coerceAtMost(trace.size - 1)
            val response = trace[index]
            callCount++

            ChatCompletion(
                id = "mock-completion-id",
                choices =
                    listOf(
                        ChatChoice(
                            index = 0,
                            message =
                                ChatMessage(
                                    role = ChatRole.Assistant,
                                    content = response,
                                ),
                        ),
                    ),
                created = System.currentTimeMillis(),
                model = ModelId("mock-model"),
            )
        }

        val requestHandler = RequestHandler.of(generationConfig = config, api = api)
        val responseParser = Parser.create()
        val planGenerator = LMPlanGenerator.of(requestHandler, responseParser)
        return LMGenerationStrategy.of(planGenerator, config)
    }
}
