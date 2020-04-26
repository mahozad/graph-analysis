package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.Collections.synchronizedList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.DAYS
import kotlin.collections.ArrayList

private const val NUMBER_OF_SAMPLE_PAIRS = 1000
private val sourceFilePath = Path.of("src/main/resources/graph.txt")
private val allDistancesFoundSoFar = Collections.synchronizedMap(mutableMapOf<Pair<Int, Int>, Int>())
private val graph = Files.newBufferedReader(sourceFilePath)
    .lineSequence()
    .groupBy({ it.substringBefore(" ").toInt() }, { it.substringAfter(" ").toInt() })

private val targetNodesDistances = synchronizedList(ArrayList<Int>())

// VM options: -Xmx4096m -Xss128m
fun main() {
    val startTime = Instant.now()
    val executorService = Executors.newFixedThreadPool(4)

    val nodes = getRandomNodes()
    for (i in 0 until NUMBER_OF_SAMPLE_PAIRS) {
        executorService.submit(DistanceCalculator(nodes.elementAt(i), nodes.elementAt(nodes.size - i - 1)))
    }

    executorService.shutdown()
    executorService.awaitTermination(1, DAYS)

    println("Number of connected nodes: ${targetNodesDistances.size}")
    println("Average distance: ${targetNodesDistances.average()}")
    println("Time: ${Duration.between(startTime, Instant.now()).toMinutes()}m")
}

private class DistanceCalculator(private val node1: Int, private val node2: Int) : Runnable {
    override fun run() {
        val distance = calculateShortestDistance(node1, node2)
        if (distance > 0) targetNodesDistances.add(distance)
    }
}

// Uses BFS (Breadth-First-Search) algorithm.
private fun calculateShortestDistance(from: Int, to: Int): Int {
    if (from == to) return 0
    if (graph.getValue(from).contains(to)) return 1 // Shortcut
    if (allDistancesFoundSoFar.containsKey(Pair(from, to))) return allDistancesFoundSoFar.getValue(Pair(from, to))

    val queue: Queue<Int> = LinkedList()
    queue.add(from)
    queue.add(null) // Indicates end of this level
    var depth = 1
    val visited = mutableSetOf<Int>()

    while (queue.isNotEmpty()) {
        val next = queue.remove()
        if (next == null) {
            depth++
            continue
        }

        if (visited.contains(next)) continue
        visited.add(next) // NOTE: Should be after if

        if (!graph.keys.contains(next)) continue
        if (allDistancesFoundSoFar.containsKey(Pair(next, to))) {
            return allDistancesFoundSoFar.getValue(Pair(next, to)) + depth - 1
        }
        if (graph.getValue(next).contains(to)) {
            allDistancesFoundSoFar.put(Pair(from, to), depth)
            return depth
        }

        queue.addAll(graph.getValue(next))
        if (queue.element() == null) queue.add(null) // If all nodes of this level finished, add flag for next level
    }

    return -1
}

private fun getRandomNodes(): Set<Int> {
    val nodes = mutableSetOf<Int>()
    while (nodes.size < NUMBER_OF_SAMPLE_PAIRS * 2) nodes.add(graph.keys.random())
    return nodes
}

private fun edgesOf(node: Int): Collection<Int> {
    return Files.newBufferedReader(Path.of("src/main/resources/edges/out/${node % 1000}.txt"))
        .lineSequence()
        .first { it.startsWith("$node->") }
        .substringAfter("->")
        .split(" ")
        .map { it.toInt() }
}
