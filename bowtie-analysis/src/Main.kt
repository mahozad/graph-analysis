/**
 * Based on the paper "Bow-tie decomposition in directed graphs" by Rong Yang.
 *
 * @author Mahdi Hosseinzadeh
 */

import java.io.File
import java.time.Duration
import java.time.Instant.now

typealias Node = Int
typealias Graph = Map<Node, List<Node>>

data class Link(val source: Node, val target: Node)

val src = File("src/main/resources/graph.txt")
val nodes = mutableSetOf<Node>()
val graph = mutableMapOf<Node, MutableList<Node>>()
val graphR = mutableMapOf<Node, MutableList<Node>>()

// NOTE: Set VM options -Xmx3072m and -Xss16m for the program
fun main() {
    val startTime = now()

    constructGraphs()
    val core = extractCore()
    /*Re*/constructGraphs()
    val outs = extractOut(core)
    val ins = extractIn(core)

    println("Core size: ${core.size}")
    println("Out size: ${outs.size}")
    println("In size: ${ins.size}")
    println("Others: ${(nodes - core - outs - ins).size}")
    println("Time: ${Duration.between(startTime, now()).toSeconds()} s")
}

fun extractCore(): Set<Node> {
    while (graph.isNotEmpty()) {
        val node = nodes.random()
        val nodeReaching = findNodesReachingTo(node)
        val nodeReachable = findNodesReachableFrom(node)
        val scc = nodeReaching intersect nodeReachable
        graph.keys.removeAll(scc)  // ⎛ Very important ⎞
        graphR.keys.removeAll(scc) // ⎝ Very important ⎠
        // If the strongly connected component is big enough, it is probably the core
        if (scc.size > 0.4 * graph.size) return scc
    }
    return emptySet()
}

fun extractIn(core: Set<Node>) = findNodesReachingTo(core.random()) - core

fun extractOut(core: Set<Node>) = findNodesReachableFrom(core.random()) - core

fun Graph.neighborsOf(node: Node) = getOrDefault(node, emptyList())

fun findNodesReachingTo(node: Node) = findNodeLineage(node, graphR)

fun findNodesReachableFrom(node: Node) = findNodeLineage(node, graph)

fun String.toLink() = Link(substringBefore(" ").toInt(), substringAfter(" ").toInt())

fun findNodeLineage(node: Node, graph: Graph): Set<Node> {
    val result = mutableSetOf(node)
    val visited = mutableSetOf(node)
    fun traverse(node: Node) {
        visited += node
        for (neighbor in graph.neighborsOf(node)) {
            if (neighbor in visited) continue
            result.add(neighbor)
            traverse(neighbor)
        }
    }
    traverse(node)
    return result
}

fun constructGraphs() {
    links().groupByTo(graph, Link::source, Link::target)
    links().groupByTo(graphR, Link::target, Link::source)
    graph.flatMapTo(nodes) { it.value + it.key }
    nodes.forEach { graph.putIfAbsent(it, mutableListOf()) }
}

fun links() = src.bufferedReader().lineSequence().map(String::toLink)
