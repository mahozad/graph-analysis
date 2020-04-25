package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path
import java.util.*

private val sourceFilePath = Path.of("src/main/resources/sample-graph.txt")

fun main() {
    determineIfGraphIsBowTie()
}

private fun determineIfGraphIsBowTie() {
    val graph = Files.newBufferedReader(sourceFilePath)
        .lineSequence()
        .groupBy({ it.substringBefore(" ").toInt() }, { it.substringAfter(" ").toInt() })


    val visiting = mutableSetOf<Int>()
    fun canFirstNodeReachTheSecondNode(first: Int, second: Int): Boolean {
        if (!graph.containsKey(first)) return false

        if (graph.getValue(first).contains(second)) return true

        visiting.add(first)
        var isConnected = false
        for (n in graph.getValue(first)) {
            if (!visiting.contains(n)) {
                isConnected = canFirstNodeReachTheSecondNode(n, second)
                if (isConnected) break
            }
        }
        return isConnected
    }


    val queue: Queue<Int> = ArrayDeque(graph.keys)
    val sets = mutableListOf(mutableSetOf(queue.element()))
    var currentSet = 0

    fun isNodeInSets(node: Int): Boolean {
        for (set in sets) if (set.contains(node)) return true
        return false
    }

    while (!queue.isEmpty()) {
        val node = queue.remove()
        visiting.clear()
        if (!isNodeInSets(node) && !canFirstNodeReachTheSecondNode(sets[currentSet].first(), node)) {
            currentSet++
            sets.add(mutableSetOf(node))
        }
        for (target in graph.getValue(node)) {
            visiting.clear()
            if (canFirstNodeReachTheSecondNode(target, node)) {
                sets[currentSet].add(target)
            }
        }
    }

    sets.forEach { println(it) }
}
