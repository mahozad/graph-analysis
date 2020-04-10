package ir.ac.yazd

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.APPEND

private val sourceFilePath = Path.of("src/main/resources/graph.txt")

fun main() {
    // extractNodesOutEdges()
    extractNodesInEdges()
}

private fun extractNodesOutEdges() {
    val graph = Files.newBufferedReader(sourceFilePath).lineSequence().groupBy(
            { it.substringBefore(" ").toInt() },
            { it.substringAfter(" ").toInt() }
    )

    for (file in File("src/main/resources/edges/out/").listFiles()) file.delete()

    graph.entries.forEach {
        val file = File("src/main/resources/edges/out/${it.key % 1000}.txt")
        if (!file.exists()) file.createNewFile()
        Files.writeString(file.toPath(),"${it.key}->${it.value.joinToString(" ")}\r\n", APPEND)
    }
}

private fun extractNodesInEdges() {
    val graph = Files.newBufferedReader(sourceFilePath).lineSequence().groupBy(
            { it.substringAfter(" ").toInt() },
            { it.substringBefore(" ").toInt() }
    )

    for (file in File("src/main/resources/edges/in/").listFiles()) file.delete()

    graph.entries.forEach {
        val file = File("src/main/resources/edges/in/${it.key % 1000}.txt")
        if (!file.exists()) file.createNewFile()
        Files.writeString(file.toPath(),"${it.key}<-${it.value.joinToString(" ")}\r\n", APPEND)
    }
}
