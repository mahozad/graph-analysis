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

const val ANSI_RESET = "\u001B[0m"
const val ANSI_CYAN = "\u001B[1;36m"
const val ANSI_BLUE = "\u001B[1;34m"

private val sourceFilePath = Path.of("src/main/resources/graph.txt")
private val nodes = mutableSetOf<Int>()
private val graph = mutableMapOf<Int, MutableList<Int>>()
private val graphR = mutableMapOf<Int, MutableList<Int>>()

// VM options: -Xmx3072m -Xss64m
fun main() {
    val startTime = Instant.now()

    constructGraphs()
    val coreNodes = extractCore()
    /*re*/constructGraphs()
    val outNodes = extractOut(coreNodes)
    val inNodes = extractIn(coreNodes)

    println("Core size: $ANSI_CYAN${coreNodes.size}$ANSI_RESET")
    println("Out size: $ANSI_CYAN${outNodes.size}$ANSI_RESET")
    println("In size: $ANSI_CYAN${inNodes.size}$ANSI_RESET")
    println("Others: $ANSI_CYAN${(nodes.size) - (coreNodes.size + outNodes.size + inNodes.size)}$ANSI_RESET")
    println("Time: $ANSI_BLUE${Duration.between(startTime, Instant.now()).toSeconds()}s$ANSI_RESET")
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

fun constructGraphs() {
    links().onEach(nodes::addAll).groupByTo(graph, { it.component1() }, { it.component2() })
    links().groupByTo(graphR, { it.component2() }, { it.component1() })

    for (node in nodes) {
        graph.putIfAbsent(node, mutableListOf())
        graphR.putIfAbsent(node, mutableListOf())
    }
}

fun links(): Sequence<List<Int>> =
    Files.newBufferedReader(sourceFilePath).lineSequence()
        .map { line -> line.split(" ").map { it.toInt() } }
