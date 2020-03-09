package ir.ac.yazd

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode.HTML
import org.thymeleaf.templateresolver.FileTemplateResolver
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.math.pow

private val sourceFilePath: Path = Path.of("src/main/resources/sample-graph.txt")
private val outputFilePath: Path = Path.of("result.html")
private val templateFilePath: Path = Path.of("src/main/resources/html/template.html")
private val stringWriter = StringWriter()

// You can use a VM option named ss to adjust the maximum stack size.
// A VM option is usually passed using -X{option}. So you can use java -Xss1M to set the maximum of
// stack size to 1 Megabyte

fun main() {
    //    determineIfAdheresPowerLaw()
    determineIfGraphIsBowTie()
}

private fun determineIfAdheresPowerLaw() {
    val edgeCounts = generateListOfIngoingEdgeCount()
    val edgeCountFreq = mergeEdgeCounts(edgeCounts)

    val thContext = Context()
    thContext.setVariable("edgeCounts", edgeCountFreq.map { it.first })
    thContext.setVariable("edgeCountFreq", edgeCountFreq.map { it.second })
    // groupByPowerOf10(edgeCountFreq, thContext)

    setupTemplateEngine(thContext)

    Files.newBufferedWriter(outputFilePath).use { writer -> writer.write(stringWriter.toString()) }
}

private fun groupByPowerOf10(edgeCountToFreq: List<Pair<Int, Int>>, thContext: Context) {
    val freq = mutableMapOf<String, Int>()
    try {
        for (i in 0..10) {
            val from = 10.0.pow(i).toInt() - 1
            val to = 10.0.pow(i + 1).toInt() - 2
            for (j in from until to) {
                freq.merge("10^${i}-10^${i + 1}", edgeCountToFreq[j].second) { t, u -> t + u }
            }
        }
    } catch (e: Exception) {

    }
    thContext.setVariable("edgeCounts", freq.keys)
    thContext.setVariable("edgeCountFreq", freq.values)
}

private fun generateListOfIngoingEdgeCount(): List<Int> {
    return Files.newBufferedReader(sourceFilePath)
            .lineSequence()
            // .take(1000)
            // Map node to its in-going edge count (substitute Before/After to switch between out and in)
            .groupBy({ it.substringAfter(" ").toInt() }, { it.substringBefore(" ").toInt() })
            .map { it.value.size }
}

private fun mergeEdgeCounts(list: List<Int>) = list
        .groupBy { it }
        .entries.sortedBy { it.key }
        .map { Pair(it.key, it.value.size) }

private fun setupTemplateEngine(thContext: Context): TemplateEngine {
    val templateResolver = FileTemplateResolver().apply { templateMode = HTML }
    val templateEngine = TemplateEngine().apply { setTemplateResolver(templateResolver) }
    templateEngine.process(templateFilePath.toString(), thContext, stringWriter)
    return templateEngine
}

private fun determineIfGraphIsBowTie() {
    val map = Files.newBufferedReader(sourceFilePath)
            .lineSequence()
            .take(100_000)
            .groupBy({ it.substringBefore(" ").toInt() }, { it.substringAfter(" ").toInt() })


    val visiting = mutableSetOf<Int>()
    fun canFirstNodeReachTheSecondNode(first: Int, second: Int): Boolean {
        if (!map.containsKey(first)) return false

        if (map.getValue(first).contains(second)) return true

        visiting.add(first)
        var isConnected = false
        for (n in map.getValue(first)) {
            if (!visiting.contains(n)) {
                isConnected = canFirstNodeReachTheSecondNode(n, second)
                if (isConnected) break
            }
        }
        return isConnected
    }


    val queue: Queue<Int> = ArrayDeque(map.keys)
    val sets = mutableListOf(mutableSetOf(queue.element()))
    var currentSet = 0

    fun isNodeInSets(node: Int): Boolean {
        for (set in sets) if (set.contains(node)) return true
        return false
    }

    while (!queue.isEmpty()) {
        val node = queue.remove()
        visiting.clear()
        if (!isNodeInSets(node) && !canFirstNodeReachTheSecondNode(sets[currentSet].first(), node)) {
            currentSet++
            sets.add(mutableSetOf(node))
        }
        for (target in map.getValue(node)) {
            visiting.clear()
            if (canFirstNodeReachTheSecondNode(target, node)) {
                sets[currentSet].add(target)
            }
        }
    }

    sets.forEach { println(it) }
}
