package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.asl

/**
 * Used to convert occurrences of +! or -! with two conventional predicates "plusBang" and "minusBang".
 * It's a preprocessing step that takes into account how operators are parsed in 2p-kt.
 */
class AgentSpeakNormalizer {
    fun normalize(input: String): String {
        val sb = StringBuilder()
        var i = 0

        while (i < input.length) {
            when {
                i + 1 < input.length && input[i] == '+' && input[i + 1] == '!' -> {
                    i += 2
                    i = skipWhitespace(input, i)
                    val (term, newIndex) = extractTerm(input, i)
                    sb.append("plusBang(").append(term).append(")")
                    i = newIndex
                }
                i + 1 < input.length && input[i] == '-' && input[i + 1] == '!' -> {
                    i += 2
                    i = skipWhitespace(input, i)
                    val (term, newIndex) = extractTerm(input, i)
                    sb.append("minusBang(").append(term).append(")")
                    i = newIndex
                }
                else -> {
                    sb.append(input[i])
                    i++
                }
            }
        }
        return sb.toString()
    }

    private fun skipWhitespace(
        s: String,
        start: Int,
    ): Int {
        var i = start
        while (i < s.length && s[i].isWhitespace()) i++
        return i
    }

    private fun extractTerm(
        s: String,
        start: Int,
    ): Pair<String, Int> {
        if (start >= s.length) return Pair("", start)

        return when {
            s[start].isLetter() || s[start] == '_' -> extractIdentifier(s, start)
            s[start] == '(' -> extractParenthesized(s, start)
            else -> extractSymbol(s, start)
        }
    }

    private fun extractIdentifier(
        s: String,
        start: Int,
    ): Pair<String, Int> {
        val out = StringBuilder()
        var i = start

        // Read identifier name
        while (i < s.length && (s[i].isLetterOrDigit() || s[i] == '_' || s[i] == ':')) {
            out.append(s[i])
            i++
        }

        // Handle function arguments if present
        if (i < s.length && s[i] == '(') {
            i = extractBalancedParentheses(s, i, out)
        }

        return Pair(out.toString(), i)
    }

    private fun extractParenthesized(
        s: String,
        start: Int,
    ): Pair<String, Int> {
        val out = StringBuilder()
        val i = extractBalancedParentheses(s, start, out)
        return Pair(out.toString(), i)
    }

    private fun extractSymbol(
        s: String,
        start: Int,
    ): Pair<String, Int> {
        val out = StringBuilder()
        var i = start

        while (i < s.length && !s[i].isWhitespace() && !isDelimiter(s[i])) {
            out.append(s[i])
            i++
        }

        return Pair(out.toString(), i)
    }

    private fun extractBalancedParentheses(
        s: String,
        start: Int,
        out: StringBuilder,
    ): Int {
        var depth = 0
        var i = start

        while (i < s.length) {
            val c = s[i]
            out.append(c)
            when (c) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) {
                        i++
                        break
                    }
                }
            }
            i++
        }
        return i
    }

    private fun isDelimiter(c: Char) = c in setOf(',', ';', ':', '<', '-', ')', '.', ']', '[')
}
