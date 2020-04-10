package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path

private val sourceFilePath = Path.of("src/main/resources/sample-graph.txt")
// private val graph = Files.newBufferedReader(sourceFilePath).lineSequence().groupBy({ it.substringBefore(" ").toInt() }, { it.substringAfter(" ").toInt() })
private val graphNodes = Files.newBufferedReader(sourceFilePath).lineSequence().map { it.substringBefore(" ").toInt() }.toSet()

fun main() {
    val nodes = mutableSetOf<Int>()
    for (i in 0..1000) nodes.add(graphNodes.random())

    for (i in 0..nodes.size) {
        for (j in i + 1..nodes.size) {
            val node1 = nodes.elementAt(i)
            val node2 = nodes.elementAt(j)
            println(calculateShortestDistance(node1, node2))
            println(calculateShortestDistance(node2, node1))
        }
    }
}

/**
 * Uses BFS (Breadth-First-Search) algorithm.
 */
private fun calculateShortestDistance(from: Int, to: Int): Int {
    val visited = mutableSetOf<Int>()
    fun calculate(from: Int, to: Int): Int {
        if (from == to) return 0
        if (!graphNodes.contains(from)) return -1 // Required
        if (edgesOf(from).contains(to)) return +1

        visited.add(from)
        val distances = mutableListOf<Int>()
        for (neighbor in edgesOf(from)) {
            if (visited.contains(neighbor)) continue
            val neighborDistance = calculate(neighbor, to)
            if (neighborDistance != -1) distances.add(1 + neighborDistance)
        }

        return distances.min() ?: -1
    }

    return calculate(from, to)
}

private fun edgesOf(node: Int): Collection<Int> {
    return Files.newBufferedReader(Path.of("src/main/resources/edges/out/${node % 1000}.txt"))
            .lineSequence()
            .first { it.startsWith("$node->") }
            .substringAfter("->")
            .split(" ")
            .map { it.toInt() }
}
