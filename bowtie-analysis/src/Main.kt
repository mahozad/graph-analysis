/**
 * Based on the algorithm from the paper "Bow-tie decomposition in directed graphs" by Rong Yang.
 *
 * Reads a directed, unweighted graph from an input file and detects its bowtie components.
 *
 * Every record (line) in the file should denote an edge
 * from a start node id to an end node id (i.e. two numbers delimited by a space).
 * For example, `31 7` denotes an edge from the vertex with id `31` to the vertex with id `7`.
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

/**
 * I wrote this function myself.
 * Interesting enough, I saw a solution for this problem (DFS traverse)
 * that used the approach just like me (inner function etc.).
 * Cannot remember where I saw it. Maybe it was
 * [here](https://programmer.group/kotlin-entry-6-function.html).
 *
 * If using recursion, we can use Kotlin 1.7 deep recursive functions
 * to prevent stackoverflows.
 * See https://youtu.be/54WEfLKtCGk?t=945
 */
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
