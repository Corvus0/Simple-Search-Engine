package search

import java.io.File

fun main(args: Array<String>) {
    val dataOption = args.indexOf("--data")
    val filename = args[dataOption + 1]
    val search = Search(filename)
    while (true) {
        printMenu()
        when (readln().toInt()) {
            0 -> {
                println("\nBye!")
                return
            }
            1 -> search.findPerson()
            2 -> search.printPeople()
            else -> println("\nIncorrect option! Try again.")
        }
    }
}

private fun printMenu() {
    println("\n=== Menu ===\n"+
            "1. Find a person\n" +
            "2. Print all people\n" +
            "0. Exit")
}

class Search(private val filename: String) {
    private val people = mutableListOf<String>()
    private val invertedIndex = mutableMapOf<String, Set<Int>>()

    private enum class Strategy {
        ALL, ANY, NONE
    }

    init {
        val lines = File(filename).readLines()
        for (line in lines) {
            people.add(line.trim())
        }
    }

    private fun findAny(query: List<String>): MutableSet<String>? {
        val results = mutableSetOf<String>()
        val notFound = query.toMutableList()
        for (q in query) {
            val indices = invertedIndex[q]
            if (indices != null) {
                notFound.remove(q)
                for (i in indices) {
                    results.add(people[i])
                }
            }
        }
        if (notFound.isEmpty()) return results
        for (q in notFound) {
            val resultIndices = mutableSetOf<Int>()
            person@ for (i in people.indices) {
                val person = people[i]
                    for (personPart in person.split(" ")) {
                        if (q == personPart.lowercase()) {
                            results.add(person)
                            resultIndices.add(i)
                            continue@person
                    }
                }
            }
            if (resultIndices.isNotEmpty()) invertedIndex[q] = resultIndices
        }
        return results.ifEmpty { null }
    }

    private fun findAll(query: List<String>): MutableList<String>? {
        val results = mutableListOf<String>()
        val allQueries = query.joinToString()
        val indices = invertedIndex[allQueries]
        if (indices != null) {
            for (i in indices) {
                results.add(people[i])
            }
        }
        if (results.isNotEmpty()) return results
        val resultIndices = mutableSetOf<Int>()
        person@ for (i in people.indices) {
            val person = people[i]
            for (q in query) {
                for (personPart in person.split(" ")) {
                    if (q != personPart.lowercase()) continue@person
                }
                results.add(person)
                resultIndices.add(i)
                continue@person
            }
        }
        if (resultIndices.isNotEmpty()) invertedIndex[allQueries] = resultIndices
        return results.ifEmpty { null }
    }

    private fun findNone(query: List<String>): MutableList<String>? {
        val results = mutableListOf<String>()
        val allQueries = query.joinToString()
        val noneQuery = "!${allQueries}"
        val indices = invertedIndex[noneQuery]
        if (indices != null) {
            for (i in indices) {
                results.add(people[i])
            }
        }
        if (results.isNotEmpty()) return results
        val resultIndices = mutableSetOf<Int>()
        person@ for (i in people.indices) {
            val person = people[i]
            for (q in query) {
                for (personPart in person.split(" ")) {
                    if (q == personPart.lowercase()) continue@person
                }
            }
            results.add(person)
            resultIndices.add(i)
        }
        if (resultIndices.isNotEmpty()) invertedIndex[noneQuery] = resultIndices
        return results.ifEmpty { null }
    }

    fun findPerson() {
        println("\nSelect a matching strategy: ALL, ANY, NONE")
        val strategy = readln().trim().lowercase()
        println("\nEnter a name or email to search all matching people.")
        val query = readln().trim().lowercase().split(" ")
        val results = when (strategy) {
            "all" -> findAll(query)
            "any" -> findAny(query)
            "none" -> findNone(query)
            else -> return
        }
        if (results != null) {
            println("${results.size} person${if (results.size > 1) "s" else ""} found:")
            for (person in results) {
                println(person)
            }
        } else {
            println("No matching people found.")
        }
    }

    fun printPeople() {
        println("=== List of people ===")
        for (person in people) {
            println(person)
        }
    }
}
