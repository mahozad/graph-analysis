package ir.ac.yazd

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode.HTML
import org.thymeleaf.templateresolver.FileTemplateResolver
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path

val sourceFilePath: Path = Path.of("src/main/resources/graph.txt")
val outputFilePath: Path = Path.of("result.html")
val templateFilePath: Path = Path.of("src/main/resources/html/template.html")

fun main() {
    val edgeCounts = generateListOfIngoingEdgeCount()
    val edgeCountToFreq = mergeEdgeCounts(edgeCounts)

    val templateEngine = setupTemplateEngine()

    val thymeleafCxt = Context()
    thymeleafCxt.setVariable("edgeCounts", edgeCountToFreq.map { it.key })
    thymeleafCxt.setVariable("edgeCountFreq", edgeCountToFreq.map { it.value.size })
    val stringWriter = StringWriter()
    templateEngine.process(templateFilePath.toString(), thymeleafCxt, stringWriter)

    Files.newBufferedWriter(outputFilePath).use { writer -> writer.write(stringWriter.toString()) }
}

private fun generateListOfIngoingEdgeCount(): List<Int> {
    return Files.newBufferedReader(sourceFilePath)
            .lineSequence()
            // .take(1000)
            // Map node to its in-going edge count (substitute Before/After to switch between out and in)
            .groupBy({ it.substringAfter(" ").toInt() }, { it.substringBefore(" ").toInt() })
            .map { it.value.size }
}

private fun mergeEdgeCounts(list: List<Int>) = list.groupBy { it }.entries.sortedBy { it.key }

private fun setupTemplateEngine(): TemplateEngine {
    val templateResolver = FileTemplateResolver().apply { templateMode = HTML }
    return TemplateEngine().apply { setTemplateResolver(templateResolver) }
}
