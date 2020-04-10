package ir.ac.yazd

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

private val sourceFilePath = Path.of("src/main/resources/graph.txt")

fun main() {
    // extractNodesOutEdges()
    extractNodesInEdges()
}

private fun extractNodesOutEdges() {
    val graph = Files.newBufferedReader(sourceFilePath).lineSequence().groupBy(
            { it.substringBefore(" ").toInt() },
            { it.substringAfter(" ").toInt() }
    )
    File("src/main/resources/out-edges.txt").writeText(
            graph.entries.joinToString(
                    separator = "\r\n",
                    transform = { "${it.key}->${it.value.joinToString(" ")}" }
            )
    )
}

private fun extractNodesInEdges() {
    val graph = Files.newBufferedReader(sourceFilePath).lineSequence().groupBy(
            { it.substringAfter(" ").toInt() },
            { it.substringBefore(" ").toInt() }
    )
    File("src/main/resources/in-edges.txt").writeText(
            graph.entries.joinToString(
                    separator = "\r\n",
                    transform = { "${it.key}<-${it.value.joinToString(" ")}" }
            )
    )
}
