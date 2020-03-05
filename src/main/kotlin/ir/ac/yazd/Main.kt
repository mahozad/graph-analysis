package ir.ac.yazd

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode.HTML
import org.thymeleaf.templateresolver.FileTemplateResolver
import java.io.File
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Paths

const val SOURCE_FILE_PATH = "src/main/resources/graph.txt"

fun main() {
    val edgeCounts = generateListOfIngoingEdgeCount()
    val edgeCountFreq = mergeEdgeCounts(edgeCounts)

    val templateResolver = FileTemplateResolver().apply { templateMode = HTML }
    val templateEngine = TemplateEngine().apply { setTemplateResolver(templateResolver) }

    val context = Context()
    context.setVariable("edgeCounts", edgeCountFreq.map { it.key })
    context.setVariable("edgeCountFreq", edgeCountFreq.map { it.value.size })
    val stringWriter = StringWriter()
    templateEngine.process("src/main/resources/html/template.html", context, stringWriter)

    Files.newBufferedWriter(Paths.get("result.html")).use { writer -> writer.write(stringWriter.toString()) }
}

private fun generateListOfIngoingEdgeCount(): List<Int> {
    return File(SOURCE_FILE_PATH)
            .bufferedReader()
            .lineSequence()
            // Map node to its in-going edge count (substitute Before/After to switch between out and in)
            .groupBy({ it.substringAfter(" ").toInt() }, { it.substringBefore(" ").toInt() })
            .map { it.value.size }
}

private fun mergeEdgeCounts(list: List<Int>) = list.groupBy { it }.entries.sortedBy { it.key }
