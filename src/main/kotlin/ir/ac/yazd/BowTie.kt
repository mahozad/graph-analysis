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
    val coreNodes = extractCore()
    /*re*/constructGraphs()
    val outNodes = extractOut(coreNodes)
    val inNodes = extractIn(coreNodes)

    println("Core size: ${coreNodes.size}")
    println("Out size: ${outNodes.size}")
    println("In size: ${inNodes.size}")
    println("Others: ${(graph.size) - (coreNodes.size + outNodes.size + inNodes.size)}")
    println("Time: ${Duration.between(startTime, Instant.now()).toSeconds()}s")
}

fun extractCore(): Set<Int> {
    val graphOrder = graph.size
    while (graph.isNotEmpty()) {
        val node = graph.keys.random()
        val nodeReaching = findNodesReachingTo(node) // Those that can reach this node
        val nodeReachable = findNodesReachableFrom(node) // Those that can be reached from this node

        val stronglyConnectedComponent = setOf(node) union (nodeReachable intersect nodeReaching)
        graph.keys.removeAll(stronglyConnectedComponent)
        graphReverse.keys.removeAll(stronglyConnectedComponent)

        // If the component size is big enough, it is probably the core
        if (stronglyConnectedComponent.size > 0.40 * graphOrder) return stronglyConnectedComponent
    }
    return emptySet()
}

fun extractIn(core: Set<Int>) = findNodesReachingTo(core.random()) - core

fun extractOut(core: Set<Int>) = findNodesReachableFrom(core.random()) - core

fun findNodesReachingTo(node: Int) = findNodeLineage(graphReverse, node)

fun findNodesReachableFrom(node: Int) = findNodeLineage(graph, node)

fun <T> Map<T, List<T>>.neighborsOf(node: T) = getOrDefault(node, emptyList())

fun findNodeLineage(graph: Map<Int, List<Int>>, node: Int): Set<Int> {
    val result = mutableSetOf<Int>()
    val visited = mutableSetOf<Int>()

    fun execute(node: Int) {
        visited.add(node)
        for (neighbor in graph.neighborsOf(node)) {
            if (visited.contains(neighbor)) continue
            result.add(neighbor)
            execute(neighbor)
        }
    }

    execute(node)
    return result
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
