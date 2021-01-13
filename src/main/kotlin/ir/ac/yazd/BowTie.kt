/**
 * Based on the paper "Bow-tie decomposition in directed graphs" by Rong Yang.
 *
 * @author Mahdi Hosseinzadeh
 */
package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

private val src = Path.of("src/main/resources/graph.txt")
private val nodes = mutableSetOf<Int>()
private val graph = mutableMapOf<Int, MutableList<Int>>()
private val graphR = mutableMapOf<Int, MutableList<Int>>()

// NOTE: Set VM options -Xmx3072m and -Xss16m for the program
fun main() {
    val startTime = Instant.now()

    constructGraphs()
    val coreNodes = extractCore()
    /*Re*/constructGraphs()
    val outNodes = extractOut(coreNodes)
    val inNodes = extractIn(coreNodes)

    println("Core size: ${coreNodes.size}")
    println("Out size: ${outNodes.size}")
    println("In size: ${inNodes.size}")
    println("Others: ${(nodes.size) - (coreNodes.size + outNodes.size + inNodes.size)}")
    println("Time: ${Duration.between(startTime, Instant.now()).toSeconds()}s")
}

fun extractCore(): Set<Int> {
    while (graph.isNotEmpty()) {
        val node = nodes.random()
        val nodeReaching = findNodesReachingTo(node) // Those that can reach to this node
        val nodeReachable = findNodesReachableFrom(node) // Those that can be reached from this node

        val scc = nodeReaching intersect nodeReachable
        graph.keys.removeAll(scc)  // ⎛ Very important ⎞
        graphR.keys.removeAll(scc) // ⎝ Very important ⎠

        // If the strongly connected component is big enough, it is probably the core
        if (scc.size > 0.40 * graph.size) return scc
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

data class Link(val source: Int, val target: Int)

fun constructGraphs() {
    links().groupByTo(graph, Link::source, Link::target)
    links().groupByTo(graphR, Link::target, Link::source)
           .flatMapTo(nodes, { it.value + it.key })
           .forEach {
               graph.putIfAbsent(it, mutableListOf())
               graphR.putIfAbsent(it, mutableListOf())
           }
}

// fun links() = lines(src).map { line -> line.split(" ").map { it.toInt() } } // and then remove Link class and use List::first and list::last
fun links() = lines().map { Link(it.substringBefore(' ').toInt(), it.substringAfter(' ').toInt()) }

fun lines() = Files.newBufferedReader(src).lineSequence()
