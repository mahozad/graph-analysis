package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path
import java.util.*

private val sourceFilePath = Path.of("src/main/resources/graph.txt")
private lateinit var graph: Map<Int, List<Int>>

// -Xmx4096m -Xss128m
fun main() {
    determineIfGraphIsBowTie()
}

private fun determineIfGraphIsBowTie() {
    readGraph()

    val queue: Queue<Int> = ArrayDeque(graph.keys)
    val sets = mutableListOf(mutableSetOf<Int>())
    val visited = mutableSetOf<Int>()
    var currentSet = 0
    while (queue.isNotEmpty()) {
        val node = queue.remove()
        if (visited.contains(node)) continue
        sets[currentSet].add(node)
        visited.add(node)
        if (!graph.containsKey(node)) continue // If has no outgoing ...
        val thisClusterQueue = ArrayDeque(graph.neighborsOf(node))

        while (thisClusterQueue.isNotEmpty()) {
            val neighbor = thisClusterQueue.remove()
            if (visited.contains(neighbor)) continue
            if (canFirstNodeReachTheSecondNode(neighbor, node)) {
                sets[currentSet].add(neighbor)
                visited.add(neighbor)
                queue.remove(neighbor)
                thisClusterQueue.addAll(graph.neighborsOf(neighbor))
            }
        }

        sets.add(mutableSetOf())
        currentSet++
    }

    sets.forEach { println(it) }
}

fun readGraph() {
    graph = Files.newBufferedReader(sourceFilePath)
        .lineSequence()
        .groupBy({ it.substringBefore(" ").toInt() }, { it.substringAfter(" ").toInt() })
}

private fun Map<Int, List<Int>>.neighborsOf(node: Int) = getValue(node)

fun canFirstNodeReachTheSecondNode(first: Int, second: Int): Boolean {
    val visited = mutableSetOf<Int>()

    fun run(first: Int, second: Int): Boolean {
        if (!graph.containsKey(first)) return false
        if (graph.neighborsOf(first).contains(second)) return true

        visited.add(first)
        var isConnected = false
        for (neighbor in graph.neighborsOf(first)) {
            if (!visited.contains(neighbor)) {
                isConnected = run(neighbor, second)
                if (isConnected) break
            }
        }

        return isConnected
    }

    visited.clear()
    return run(first, second)
}
