package ir.ac.yazd

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode.HTML
import org.thymeleaf.templateresolver.FileTemplateResolver
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.math.log10

private val sourceFilePath = Path.of("src/main/resources/graph.txt")
private val outputFilePath = Path.of("result.html")
private val templateFilePath = Path.of("src/main/resources/html/template.html")

// You can use a VM option named ss to adjust the maximum stack size.
// A VM option is usually passed using -X{option}. So you can use java -Xss1M to set the maximum of stack size to 1 Megabyte
// VM options: -Xmx4096m -Xss128m

fun main() {
    val startTime = Instant.now()

    val edgeCountList = generateListOfIngoingEdgeCount()
    val edgeCountFreq = groupEdgeCounts(edgeCountList)
    val data = filterOnlyPowersOf10(edgeCountFreq)
    val (alpha, gamma) = calculateAlphaAndGamma(edgeCountFreq)

    generateTheResultOutput(data, alpha, gamma, startTime)
}

private fun generateListOfIngoingEdgeCount(): List<Int> {
    return Files.newBufferedReader(sourceFilePath)
        .lineSequence()
        // Map node to its in-going edge count (substitute Before/After to switch between out and in)
        .groupBy({ it.substringAfter(" ").toInt() }, { it.substringBefore(" ").toInt() })
        .map { it.value.size }
}

// Map edge size to number of nodes that have that edge size
private fun groupEdgeCounts(list: List<Int>) = list.groupBy { it }.mapValues { it.value.size }.toSortedMap()

private fun filterOnlyPowersOf10(edgeCountFreq: Map<Int, Int>) = edgeCountFreq.filter {
    it.key == 1 || it.key == 10 || it.key == 100 || it.key == 1000 || it.key == 10000 || it.key == 100000
}

// https://math.stackexchange.com/questions/410894/power-law-probability-distribution-from-observations
private fun calculateAlphaAndGamma(edgeCountFreq: Map<Int, Int>): Pair<Double, Double> {
    fun calculateGamma(): Double {
        var denominator = 0.0
        for (ecf in edgeCountFreq) denominator += 2 * log10(ecf.value.toDouble())
        return 1 + (edgeCountFreq.size / denominator)
    }

    fun calculateAlpha(gamma: Double): Double {
        val sampleElement = edgeCountFreq.entries.iterator().next()
        return gamma * log10(sampleElement.key.toDouble()) + log10(sampleElement.value.toDouble())
    }

    val gamma = calculateGamma()
    val alpha = calculateAlpha(gamma)

    return Pair(alpha, gamma)
}

private fun generateTheResultOutput(data: Map<Int, Int>, alpha: Double, gamma: Double, startTime: Instant) {
    val thContext = Context()
    thContext.setVariable("time", Duration.between(startTime, Instant.now()))
    thContext.setVariable("alpha", alpha)
    thContext.setVariable("gamma", gamma)
    thContext.setVariable("edgeCounts", data.map { it.key })
    thContext.setVariable("edgeCountFreq", data.map { log10(it.value.toDouble()) })

    val stringWriter = StringWriter()
    val templateResolver = FileTemplateResolver().apply { templateMode = HTML }
    val templateEngine = TemplateEngine().apply { setTemplateResolver(templateResolver) }
    templateEngine.process(templateFilePath.toString(), thContext, stringWriter)

    Files.newBufferedWriter(outputFilePath).use { writer -> writer.write(stringWriter.toString()) }
}
