package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.Collections.synchronizedList
import java.util.Collections.synchronizedMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.DAYS

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
    for (i in nodes.indices) {
        executorService.submit(DistanceCalculator(nodes.elementAt(i), nodes.elementAt(nodes.size - i - 1)))
    }

    executorService.shutdown()
    executorService.awaitTermination(1, DAYS)

    println("Average distance: ${targetNodesDistances.reduce { total, distance -> total + distance } / targetNodesDistances.size}")
    println("Time: ${Duration.between(startTime, Instant.now()).toSeconds()}s")
}

/**
 * Uses BFS (Breadth-First-Search) algorithm.
 */
private fun calculateShortestDistance(from: Int, to: Int): Int {
    val visited = mutableSetOf<Int>()
    fun calculate(from: Int, to: Int): Int {
        if (from == to) return 0
        if (!graph.keys.contains(from)) return -1 // This statement is required
        if (graph.getValue(from).contains(to)) return +1
        if (allDistancesFoundSoFar.keys.contains(Pair(from, to))) return allDistancesFoundSoFar[Pair(from, to)]!!

        visited.add(from)
        val distances = mutableListOf<Int>()
        for (neighbor in graph.getValue(from)) {
            if (visited.contains(neighbor)) continue
            val neighborDistance = calculate(neighbor, to)
            if (neighborDistance != -1) distances.add(1 + neighborDistance)
        }

        val minDistance = distances.min()
        if (minDistance != null) allDistancesFoundSoFar[Pair(from, to)] = minDistance

        return minDistance ?: -1
    }

    return calculate(from, to)
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
