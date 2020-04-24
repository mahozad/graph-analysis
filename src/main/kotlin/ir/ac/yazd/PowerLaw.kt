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
private val stringWriter = StringWriter()

// You can use a VM option named ss to adjust the maximum stack size.
// A VM option is usually passed using -X{option}. So you can use java -Xss1M to set the maximum of
// stack size to 1 Megabyte

fun main() {
    val startTime = Instant.now()

    val edgeCounts = generateListOfIngoingEdgeCount()
    val edgeCountFreq = mergeEdgeCounts(edgeCounts)
    val result = filterOnlyPowersOf10(edgeCountFreq)
    val (alpha, gamma) = calculateAlphaAndGamma(edgeCountFreq)

    val thContext = Context()
    thContext.setVariable("alpha", alpha)
    thContext.setVariable("gamma", gamma)
    thContext.setVariable("time", Duration.between(startTime, Instant.now()))
    thContext.setVariable("edgeCounts", result.map { it.first })
    thContext.setVariable("edgeCountFreq", result.map { log10(it.second.toDouble()) })

    setupTemplateEngine(thContext)

    Files.newBufferedWriter(outputFilePath).use { writer -> writer.write(stringWriter.toString()) }
}

private fun generateListOfIngoingEdgeCount(): List<Int> {
    return Files.newBufferedReader(sourceFilePath)
        .lineSequence()
        // Map node to its in-going edge count (substitute Before/After to switch between out and in)
        .groupBy({ it.substringAfter(" ").toInt() }, { it.substringBefore(" ").toInt() })
        .map { it.value.size }
}

private fun mergeEdgeCounts(list: List<Int>) = list
    .groupBy { it }
    .entries.sortedBy { it.key }
    .map { Pair(it.key, it.value.size) }

private fun filterOnlyPowersOf10(edgeCountFreq: List<Pair<Int, Int>>) = edgeCountFreq.filter {
    it.first == 1 || it.first == 10 || it.first == 100 || it.first == 1000 || it.first == 10000 || it.first == 100000
}

// https://math.stackexchange.com/questions/410894/power-law-probability-distribution-from-observations
private fun calculateAlphaAndGamma(edgeCountFreq: List<Pair<Int, Int>>): Pair<Double, Double> {
    var denominator = 0.0
    for (freq in edgeCountFreq) denominator += 2 * log10(freq.second.toDouble())
    val gamma = 1 + (edgeCountFreq.size / denominator)

    val sampleElement = edgeCountFreq.first()
    val alpha = gamma * log10(sampleElement.first.toDouble()) + log10(sampleElement.second.toDouble())

    return Pair(alpha, gamma)
}

private fun setupTemplateEngine(thContext: Context): TemplateEngine {
    val templateResolver = FileTemplateResolver().apply { templateMode = HTML }
    val templateEngine = TemplateEngine().apply { setTemplateResolver(templateResolver) }
    templateEngine.process(templateFilePath.toString(), thContext, stringWriter)
    return templateEngine
}
