package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

private val sourceFilePath = Path.of("src/main/resources/graph.txt")
private lateinit var graph: MutableMap<Int, List<Int>>
private lateinit var graphReverse: MutableMap<Int, List<Int>>

// VM options: -Xmx4096m -Xss64m
fun main() {
    val startTime = Instant.now()

    constructGraphs()
    partitionNodes()

    val duration = Duration.between(startTime, Instant.now())
    println("Time: ${duration.toMinutes()}m ${duration.toSeconds() % 60}s")
}

private fun partitionNodes() {
    while (graph.isNotEmpty()) {
        val node = graph.keys.random()
        val nodeReachable = findNodesReachableToOrFrom(graph, node) // Those that can reach from node
        val nodeReaching = findNodesReachableToOrFrom(graphReverse, node) // Those that can reach to node

        val stronglyConnectedComponent = setOf(node) union (nodeReachable intersect nodeReaching)
        graph.keys.removeAll(stronglyConnectedComponent)
        graphReverse.keys.removeAll(stronglyConnectedComponent)

        if (stronglyConnectedComponent.size > 1000) {
            println("Found a new connected component with size ${stronglyConnectedComponent.size}")
            println("Remaining nodes: ${graph.size}")
        }
    }
}

fun findNodesReachableToOrFrom(graph: Map<Int, List<Int>>, node: Int): Set<Int> {
    val result = mutableSetOf<Int>()
    val visited = mutableSetOf<Int>()

    fun run(node: Int): Set<Int> {
        if (!graph.containsKey(node)) return result
        visited.add(node)
        for (neighbor in graph.neighborsOf(node)) {
            if (visited.contains(neighbor)) continue
            result.add(neighbor)
            run(neighbor)
        }
        return result
    }

    return run(node)
}

fun constructGraphs() {
    graph = Files.newBufferedReader(sourceFilePath)
        .lineSequence()
        .groupBy({ it.substringBefore(" ").toInt() }, { it.substringAfter(" ").toInt() })
        .toMutableMap()

    graphReverse = Files.newBufferedReader(sourceFilePath)
        .lineSequence()
        .groupBy({ it.substringAfter(" ").toInt() }, { it.substringBefore(" ").toInt() })
        .toMutableMap()
}

private fun Map<Int, List<Int>>.neighborsOf(node: Int) = getValue(node)
