package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path

private val sourceFilePath: Path = Path.of("src/main/resources/sample-graph.txt")
private lateinit var map: Map<Int, List<Int>>
fun main() {
    map = Files.newBufferedReader(sourceFilePath)
            .lineSequence()
            .groupBy({ it.substringBefore(" ").toInt() }, { it.substringAfter(" ").toInt() })

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
        if (!map.containsKey(from)) return -1
        if (map[from]!!.contains(to)) return +1

        visited.add(from)
        val distances = mutableListOf<Int>()
        for (node in map[from]!!) {
            if (!visited.contains(node)) {
                val distance = calculate(node, to)
                if (distance != -1) distances.add(distance + 1)
            }
        }

        return distances.min() ?: -1
    }

    return calculate(from, to)
}
