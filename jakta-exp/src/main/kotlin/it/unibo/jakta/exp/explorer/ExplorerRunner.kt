package it.unibo.jakta.exp.explorer

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal

fun main(args: Array<String>): Unit =
    ExplorerExperiment()
        .context { terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true) }
        .main(args)
