package it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.core.Role
import it.unibo.jakta.agents.bdi.dsl.ScopeBuilder
import java.io.File

class PromptScope(
    private val headingLevel: Int = 1,
) : ScopeBuilder<String> {
    private val sections = mutableListOf<PromptSection>()

    fun section(
        title: String,
        f: PromptScope.() -> Unit = {},
    ) {
        val text = PromptScope(headingLevel + 1).also(f).build()
        sections.add(PromptSection(title, text))
    }

    fun fromFile(filePath: String) {
        val text = File(filePath).bufferedReader().use { it.readText() }
        sections.add(PromptSection(text = text))
    }

    fun fromResource(filePath: String) {
        val text = readResourceFile(filePath)
        sections.add(PromptSection(text = text))
    }

    fun <T> fromFormatter(
        input: T,
        formatter: (T) -> String?,
    ) {
        val text = formatter(input)
        if (text != null) {
            sections.add(PromptSection(text = text))
        } else {
            sections.add(PromptSection(text = "None."))
        }
    }

    fun fromString(text: String) {
        sections.add(PromptSection(text = text))
    }

    fun buildAsMessage(role: Role): ChatMessage = ChatMessage(role, build())

    override fun build(): String = sections.joinToString(separator = "\n") { it.toString(headingLevel) }.trim()

    private data class PromptSection(
        val title: String? = null,
        val text: String,
    ) {
        fun toString(level: Int): String =
            when {
                title != null -> {
                    val heading = "#".repeat(level)
                    "\n$heading $title\n\n$text"
                }
                else -> text
            }
    }

    companion object {
        private fun readResourceFile(resourcePath: String): String {
            val classLoader = object {}.javaClass.enclosingClass?.classLoader ?: ClassLoader.getSystemClassLoader()
            val inputStream = classLoader.getResourceAsStream(resourcePath) ?: return "File not found: $resourcePath"
            return inputStream.bufferedReader().use { it.readText() }
        }
    }
}
