/**
 * Based on the paper "Bow-tie decomposition in directed graphs" by Rong Yang.
 *
 * @author Mahdi Hosseinzadeh
 */

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

data class Link(val source: Int, val target: Int)

val src = Path.of("src/main/resources/graph.txt")
val nodes = mutableSetOf<Int>()
val graph = mutableMapOf<Int, MutableList<Int>>()
val graphR = mutableMapOf<Int, MutableList<Int>>()

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
        if (scc.size > 0.4 * graph.size) return scc
    }
    return emptySet()
}

fun extractIn(core: Set<Int>) = findNodesReachingTo(core.random()) - core

fun extractOut(core: Set<Int>) = findNodesReachableFrom(core.random()) - core

fun findNodesReachingTo(node: Int) = findNodeLineage(node, graphR)

fun findNodesReachableFrom(node: Int) = findNodeLineage(node, graph)

fun <T> Map<T, List<T>>.neighborsOf(node: T) = getOrDefault(node, emptyList())

fun String.toLink() = Link(substringBefore(" ").toInt(), substringAfter(" ").toInt())

fun findNodeLineage(node: Int, graph: Map<Int, List<Int>>): Set<Int> {
    val result = mutableSetOf(node)
    val visited = mutableSetOf(node)

    fun execute(node: Int) {
        visited.add(node)
        for (neighbor in graph.neighborsOf(node)) {
            if (neighbor in visited) continue
            result.add(neighbor)
            execute(neighbor)
        }
    }

    execute(node)
    return result
}

fun constructGraphs() {
    links().groupByTo(graph, Link::source, Link::target)
    links().groupByTo(graphR, Link::target, Link::source)
    graph.flatMapTo(nodes) { it.value + it.key }
    nodes.forEach { graph.putIfAbsent(it, mutableListOf()) }
}

fun links() = Files.newBufferedReader(src).lineSequence().map(String::toLink)
