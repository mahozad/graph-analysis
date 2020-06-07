/**
 * Author: Mahdi Hosseinzadeh
 * Based on "Bow-tie decomposition in directed graphs" paper by Rong Yang
 */
package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

private val sourceFilePath = Path.of("src/main/resources/graph.txt")
private var nodes = mutableSetOf<Int>()
private var graph = mutableMapOf<Int, MutableList<Int>>()
private var graphR = mutableMapOf<Int, MutableList<Int>>()

// VM options: -Xmx3072m -Xss64m
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
    println("Others: ${(nodes.size) - (coreNodes.size + outNodes.size + inNodes.size)}")
    println("Time: ${Duration.between(startTime, Instant.now()).toSeconds()}s")
}

fun extractCore(): Set<Int> {
    val graphOrder = graph.size
    while (graph.isNotEmpty()) {
        val node = nodes.random()
        val nodeReaching = findNodesReachingTo(node) // Those that can reach to this node
        val nodeReachable = findNodesReachableFrom(node) // Those that can be reached from this node

        val scc = nodeReaching intersect nodeReachable
        graph.keys.removeAll(scc) // Very important
        graphR.keys.removeAll(scc)

        // If the strongly connected component is big enough, it is probably the core
        if (scc.size > 0.40 * graphOrder) return scc
    }
    return emptySet()
}

fun extractIn(core: Set<Int>) = findNodesReachingTo(core.random()) - core

fun extractOut(core: Set<Int>) = findNodesReachableFrom(core.random()) - core

fun findNodesReachingTo(node: Int) = findNodeLineage(node, graphR)

fun findNodesReachableFrom(node: Int) = findNodeLineage(node, graph)

fun <T> Map<T, List<T>>.neighborsOf(node: T) = getOrDefault(node, emptyList())

fun findNodeLineage(node: Int, graph: Map<Int, List<Int>>): Set<Int> {
    val result = mutableSetOf(node)
    val visited = mutableSetOf(node)

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
    links().onEach { nodes.addAll(it.toList()) }.groupByTo(graph, { it.first }, { it.second })
    links().groupByTo(graphR, { it.second }, { it.first })

    for (node in nodes) {
        graph.putIfAbsent(node, mutableListOf())
        graphR.putIfAbsent(node, mutableListOf())
    }
}

fun links() = Files.newBufferedReader(sourceFilePath).lineSequence()
    .map { Pair(it.substringBefore(" ").toInt(), it.substringAfter(" ").toInt()) }
