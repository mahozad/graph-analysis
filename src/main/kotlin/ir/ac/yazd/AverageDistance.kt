package ir.ac.yazd

import java.nio.file.Files
import java.nio.file.Path
import java.util.Collections.synchronizedList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private val sourceFilePath = Path.of("src/main/resources/graph.txt")
private val graph = Files.newBufferedReader(sourceFilePath)
    .lineSequence()
    .groupBy(
        { it.substringBefore(" ").toInt() },
        { it.substringAfter(" ").toInt() }
    )

//private val graphNodes = Files.newBufferedReader(sourceFilePath).lineSequence().map { it.substringBefore(" ").toInt() }.toSet()
private val distances = synchronizedList(ArrayList<Int>())

fun main() {
    val executorService = Executors.newFixedThreadPool(4)

    val nodes = getRandomNodes()

    for (i in 0..nodes.size) {
        for (j in i + 1 until nodes.size) {
            val node1 = nodes.elementAt(i)
            val node2 = nodes.elementAt(j)
            executorService.submit(DistanceCalculator(node1, node2))
            executorService.submit(DistanceCalculator(node2, node1))
        }
    }

    executorService.awaitTermination(30, TimeUnit.DAYS)
    println((distances.reduce { total, distance -> total + distance } / distances.size))
}

/**
 * Uses BFS (Breadth-First-Search) algorithm.
 */
private fun calculateShortestDistance(from: Int, to: Int): Int {
    val visited = mutableSetOf<Int>()
    fun calculate(from: Int, to: Int): Int {
        if (from == to) return 0
        if (!graph.keys.contains(from)) return -1 // Required
        if (graph.getValue(from).contains(to)) return +1

        visited.add(from)
        val distances = mutableListOf<Int>()
        for (neighbor in graph.getValue(from)) {
            if (visited.contains(neighbor)) continue
            val neighborDistance = calculate(neighbor, to)
            if (neighborDistance != -1) distances.add(1 + neighborDistance)
        }

        return distances.min() ?: -1
    }

    return calculate(from, to)
}

private fun getRandomNodes(): Set<Int> {
    val nodes = mutableSetOf<Int>()
    for (i in 0..1000) nodes.add(graph.keys.random())
    return nodes
}

class DistanceCalculator(private val node1: Int, private val node2: Int) : Runnable {
    override fun run() {
        val distance = calculateShortestDistance(node1, node2)
        if (distance > 0) distances.add(distance)
        println(distances.size)
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
