package ir.ac.yazd

import java.io.File

fun main() {
    File("src/main/resources/graph.txt").bufferedReader()
        .lines()
        .limit(10)
        .forEach { println(it) }
}
