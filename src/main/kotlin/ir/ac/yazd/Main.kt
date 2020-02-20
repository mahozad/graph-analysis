package ir.ac.yazd

import java.io.File

fun main() {
    val edgeCounts = generateListOfEdgeCount()
    val edgeCountFreq = mergeEdgeCounts(edgeCounts)
    edgeCountFreq.forEach { println("Edge count: ${it.key}, Frequency: ${it.value.size}") }
}

private fun generateListOfEdgeCount(): List<Int> {
    return File("src/main/resources/graph.txt")
        .bufferedReader()
        .lineSequence()
        // .take(200)
        /* A map from node to its in-going edge count (substitute Before/After to switch between out- and in-degree) */
        .groupBy({ it.substringAfter(" ").toInt() }, { it.substringBefore(" ").toInt() })
        .map { it.value.size }
}

private fun mergeEdgeCounts(list: List<Int>) = list.groupBy { it }.entries.sortedBy { it.key }
