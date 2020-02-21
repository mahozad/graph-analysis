package ir.ac.yazd

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode.HTML
import org.thymeleaf.templateresolver.FileTemplateResolver
import java.io.File
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val edgeCounts = generateListOfEdgeCount()
    val edgeCountFreq = mergeEdgeCounts(edgeCounts)
    edgeCountFreq.forEach { println("Edge count: ${it.key}, Frequency: ${it.value.size}") }


    val templateResolver = FileTemplateResolver().apply { templateMode = HTML }
    val templateEngine = TemplateEngine().apply { setTemplateResolver(templateResolver) }

    val context = Context()
    context.setVariable("edgeCounts", edgeCountFreq.map { it.key })
    context.setVariable("edgeCountFreq", edgeCountFreq.map { it.value.size })
    val stringWriter = StringWriter()
    templateEngine.process("src/main/resources/html/template.html", context, stringWriter)

    Files.newBufferedWriter(Paths.get("result.html")).use { writer -> writer.write(stringWriter.toString()) }
}

private fun generateListOfEdgeCount(): List<Int> {
    return File("src/main/resources/graph.txt")
        .bufferedReader()
        .lineSequence()
        // .take(200)
        /* A map from node to its in-going edge count (substitute Before/After to switch between out- and in-degree) */
        .groupBy({ it.substringAfter(" ").toInt() }, { it.substringBefore(" ").toInt() })
        .map { it.value.size }
}

private fun mergeEdgeCounts(list: List<Int>) = list.groupBy { it }.entries.sortedBy { it.key }
