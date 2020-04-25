package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.*

private val sourceFilePath = Path.of("src/main/resources/sample-graph.txt")
private lateinit var graph: Map<Int, List<Int>>
private lateinit var graphReverse: Map<Int, List<Int>>

// -Xmx4096m -Xss128m
fun main() {
    constructGraphs()
    determineIfGraphIsBowTie()
}

private fun determineIfGraphIsBowTie() {
    val startTime = Instant.now()

    val queue: Queue<Int> = ArrayDeque(graph.keys)
    while (queue.isNotEmpty()) {
        val node = queue.remove()
        val nodeReachables = findNodesReachableToOrFrom(graph, node) // reachable from
        val nodeReachings = findNodesReachableToOrFrom(graphReverse, node) // reachable to

        val ssc = nodeReachables.intersect(nodeReachings).union(setOf(node))
        queue.removeAll(ssc)
    }

    println("Time: ${Duration.between(startTime, Instant.now()).toMinutes()}m")
}


fun findNodesReachableToOrFrom(graph:Map<Int, List<Int>>, node: Int): Set<Int> {
    val result = mutableSetOf<Int>()
    val visited = mutableSetOf<Int>()

    fun run(node: Int): Set<Int> {
        if (!graph.containsKey(node)) return result
        visited.add(node)
        for (neighbor in graph.neighborsOf(node)) {
            if (!visited.contains(neighbor)) {
                result.add(neighbor)
                run(neighbor)
            }
        }
        return result
    }

    return run(node)
}

fun constructGraphs() {
    graph = Files.newBufferedReader(sourceFilePath)
        .lineSequence()
        .groupBy({ it.substringBefore(" ").toInt() }, { it.substringAfter(" ").toInt() })
    graphReverse = Files.newBufferedReader(sourceFilePath)
        .lineSequence()
        .groupBy({ it.substringAfter(" ").toInt() }, { it.substringBefore(" ").toInt() })
}

private fun Map<Int, List<Int>>.neighborsOf(node: Int) = getValue(node)
