package it.unibo.jakta.exp

import com.aallam.openai.api.chat.ChatChoice
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import io.mockk.coEvery
import io.mockk.mockk
import it.unibo.jakta.agents.bdi.engine.FileUtils.processLog
import it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.extractAgentLogFiles
import it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.findMasLogFiles
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.LogFileUtils.extractPgpLogFiles
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMMessageReceived
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.generation.LMPlanGenerator
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl.AgentSpeakParser
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.yaml.YamlParser
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.RequestHandler
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.LMGenerationStrategy
import java.io.File
import kotlin.collections.component2

object MockGenerationStrategy {
    private var callCount = 0

    fun getChatMessages(expPath: String): List<ChatMessage> {
        val history = mutableListOf<ChatMessage>()
        val expFile = File(expPath)

        if (expFile.isFile()) {
            val pgpFiles = extractPgpLogFiles(expPath, "")
            if (pgpFiles?.isNotEmpty() == true) {
                pgpFiles.forEach { (pgpLogFile, _) ->
                    processLog(pgpLogFile) { logEntry ->
                        val event = logEntry.message.event
                        if (event is LMMessageReceived) {
                            event.chatMessage.let { history.add(it) }
                        }
                        true
                    }
                }
            } else {
                val msgContent = expFile.readText()
                if (msgContent.isNotBlank()) {
                    val chatMessage = ChatMessage(ChatRole.Assistant, msgContent)
                    history.add(chatMessage)
                }
            }
        } else {
            val masFiles = findMasLogFiles(expPath)
            val masLogFile = masFiles?.keys?.firstOrNull()
            val masId = masFiles?.values?.firstOrNull()

            if (masLogFile != null && masId != null) {
                extractAgentLogFiles(expPath, masId).forEach { (_, agentId) ->
                    extractPgpLogFiles(expPath, agentId)?.forEach { (pgpLogFile, _) ->
                        processLog(pgpLogFile) { logEntry ->
                            val event = logEntry.message.event
                            if (event is LMMessageReceived) {
                                event.chatMessage.let { history.add(it) }
                            }
                            true
                        }
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
        val responseParser = if (config.useAslSyntax) AgentSpeakParser() else YamlParser()
        val planGenerator = LMPlanGenerator.of(requestHandler, responseParser)
        return LMGenerationStrategy.of(planGenerator, config)
    }
}
