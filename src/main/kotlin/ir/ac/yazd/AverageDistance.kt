package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.Collections.synchronizedList
import java.util.Collections.synchronizedMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.DAYS
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private val sourceFilePath = Path.of("src/main/resources/graph.txt")
private val graph = Files.newBufferedReader(sourceFilePath)
    .lineSequence()
    .groupBy({ it.substringBefore(" ").toInt() }, { it.substringAfter(" ").toInt() })

//private val graphNodes = Files.newBufferedReader(sourceFilePath).lineSequence().map { it.substringBefore(" ").toInt() }.toSet()
private val allDistancesFoundSoFar = synchronizedMap(HashMap<Pair<Int, Int>, Int>())
private val targetNodesDistances = synchronizedList(ArrayList<Int>())

fun main() {
    val startTime = Instant.now()
    val executorService = Executors.newFixedThreadPool(4)

    val nodes = getRandomNodes()
    for (i in 0 until 1000) {
        executorService.submit(DistanceCalculator(nodes.elementAt(i), nodes.elementAt(nodes.size - i - 1)))
    }

    executorService.shutdown()
    executorService.awaitTermination(1, DAYS)

    println("Size: ${targetNodesDistances.size}")
    println("Average distance: ${targetNodesDistances.reduce { total, distance -> total + distance } / targetNodesDistances.size}")
    println("Time: ${Duration.between(startTime, Instant.now()).toMinutes()}m")
}

/**
 * Uses BFS (Breadth-First-Search) algorithm.
 */
private fun calculateShortestDistance(from: Int, to: Int): Int {
    if (from == to) return 0
    if (graph.getValue(from).contains(to)) return +1
    if (allDistancesFoundSoFar.keys.contains(Pair(from, to))) return allDistancesFoundSoFar[Pair(from, to)]!!

    val queue: Queue<Int> = LinkedList()
    queue.add(from)
    queue.add(null)
    var depth = 1
    val visited = mutableSetOf<Int>()

    while (queue.isNotEmpty()) {
        val next = queue.remove()
        if (next == null) {
            depth++
            continue
        }

        if (visited.contains(next)) continue
        if (!graph.keys.contains(next)) {
            visited.add(next)
            continue
        }
        if (graph.getValue(next).contains(to)) return depth

        visited.add(next)
        queue.addAll(graph.getValue(next))
        if (queue.element() == null) queue.add(null) // All nodes of this level finished so add flag for next level
    }
    return -1
}

private fun getRandomNodes(): Set<Int> {
    val nodes = mutableSetOf<Int>()
    while (nodes.size < 2000) nodes.add(graph.keys.random())
    return nodes
}

class DistanceCalculator(private val node1: Int, private val node2: Int) : Runnable {
    override fun run() {
        val distance = calculateShortestDistance(node1, node2)
        if (distance > 0) targetNodesDistances.add(distance)
    }
}

private fun edgesOf(node: Int): Collection<Int> {
    return Files.newBufferedReader(Path.of("src/main/resources/edges/out/${node % 1000}.txt"))
        .lineSequence()
        .first { it.startsWith("$node->") }
        .substringAfter("->")
        .split(" ")
        .map { it.toInt() }
}
