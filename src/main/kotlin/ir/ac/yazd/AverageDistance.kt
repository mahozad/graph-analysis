package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path

private val sourceFilePath = Path.of("src/main/resources/sample-graph.txt")
private val graph = Files.newBufferedReader(sourceFilePath).lineSequence().groupBy({ it.substringBefore(" ").toInt() }, { it.substringAfter(" ").toInt() })

fun main() {
    val distance = calculateShortestDistance(1, 4)
    println(distance)
}

/**
 * Uses BFS (Breadth-First-Search) algorithm.
 */
private fun calculateShortestDistance(from: Int, to: Int): Int {
    val visited = mutableSetOf<Int>()
    fun calculate(from: Int, to: Int): Int {
        if (from == to) return 0
        if (!graph.containsKey(from)) return -1
        if (graph.getValue(from).contains(to)) return +1

        visited.add(from)
        val distances = mutableListOf<Int>()
        for (neighbor in graph.getValue(from)) {
            if (visited.contains(neighbor)) continue
            val neighborDistance = calculate(neighbor, to)
            if (neighborDistance != -1) distances.add(1 + neighborDistance)
        }

        return distances.min() ?: -1
    }

    return calculate(from, to)
}
