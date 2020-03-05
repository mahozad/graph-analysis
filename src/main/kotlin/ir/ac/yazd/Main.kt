package ir.ac.yazd

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode.HTML
import org.thymeleaf.templateresolver.FileTemplateResolver
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path

private val sourceFilePath: Path = Path.of("src/main/resources/graph.txt")
private val outputFilePath: Path = Path.of("result.html")
private val templateFilePath: Path = Path.of("src/main/resources/html/template.html")
private val stringWriter = StringWriter()

fun main() {
    val edgeCounts = generateListOfIngoingEdgeCount()
    val edgeCountToFreq = mergeEdgeCounts(edgeCounts)

    val thContext = Context()
    thContext.setVariable("edgeCounts", edgeCountToFreq.map { it.key })
    thContext.setVariable("edgeCountFreq", edgeCountToFreq.map { it.value.size })

    setupTemplateEngine(thContext)

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

private fun setupTemplateEngine(thymeleafCxt: Context): TemplateEngine {
    val templateResolver = FileTemplateResolver().apply { templateMode = HTML }
    val templateEngine = TemplateEngine().apply { setTemplateResolver(templateResolver) }
    templateEngine.process(templateFilePath.toString(), thymeleafCxt, stringWriter)
    return templateEngine
}
