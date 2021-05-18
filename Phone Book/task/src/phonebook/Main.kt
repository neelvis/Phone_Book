package phonebook

import java.io.File
import kotlin.math.ceil
import kotlin.math.sqrt

class PhoneBook {
    private data class Entry(val input: String) {
        val phone: String = input.substringBefore(" ")
        val name: String = input.substringAfter(" ")
    }
    private val phonebook = mutableListOf<Entry>()
    private lateinit var phonebookSorted: MutableList<Entry>
    private lateinit var phonebookHashed: HashMap<String, String>
    private val timeToSearch = mutableListOf<Long>()
    private val toFind = arrayListOf<String>()
    private var linearTime = 0L
    private var bubbleTime = 0L
    private var jumpTime = 0L
    private var qsortTime = 0L
    private var binaryTime = 0L
    private var hashTableTime = 0L
    private var hashSearchTime = 0L
    private var bubbleBreak = false

    init {
        var timeInit = System.currentTimeMillis()
        File("/home/pixel/Documents/Kotlin/directory.txt").forEachLine { phonebook.add(Entry(it)) }
//        println("Dictionary loaded in ${convertMsToTime(System.currentTimeMillis() - timeInit)}")
        timeInit = System.currentTimeMillis()
        File("/home/pixel/Documents/Kotlin/find.txt").forEachLine { toFind.add(it) }
//        println("Requested names loaded in ${convertMsToTime(System.currentTimeMillis() - timeInit)}")
    }
    private fun convertMsToTime(time: Long): String {
        val ms = time % 1000
        val s = (time / 1000) % 60
        val m = time / 60_000
        var result = "$m min. $s sec. "
        // if (m > 0) result += "$m min. "
        // if (s > 0) result += "$s sec. "
        return "$result$ms ms."
    }
    fun runLinearSearch() {
        println("Start searching (linear search)...")
        val found = linearSearch()
        println("""
        Found $found / ${toFind.size} entries.
        Time taken to find: = ${convertMsToTime(linearTime)}
        
    """.trimIndent())
    }
//    Average time to search: ${convertMsToTime(timeToSearch.average().toLong())}
    private fun linearSearch(): Int {
        val timeStart = System.currentTimeMillis()
        var found = 0
        loop@ for (name in toFind) {
            val time1 = System.currentTimeMillis()
            for (entry in phonebook) {
                if (entry.name == name) {
                    found++
                    timeToSearch.add(System.currentTimeMillis() - time1)
                    continue@loop
                }
            }
        }
        val timeEnd = System.currentTimeMillis()
        linearTime = timeEnd - timeStart
        return found
    }
    fun runJumpSearch() {
        println("Start searching (bubble sort + jump search)...")
        bubbleSort()
        val found = if (bubbleBreak) {
            linearSearch()
        } else {
            jumpSearch()
        }
        val searchingTime = if (bubbleBreak) linearTime else jumpTime
        println("""
            Found $found / ${toFind.size} entries. Time taken: ${convertMsToTime(bubbleTime + searchingTime)}
            Sorting time: ${convertMsToTime(bubbleTime)} ${if (bubbleBreak) " - STOPPED, moved to linear search" else ""}
            Searching time: ${convertMsToTime(searchingTime)}
            
        """.trimIndent())
    }
    private fun bubbleSort() {
        phonebookSorted = phonebook.toMutableList()
        for (i in 0 until phonebookSorted.size - 1) {
            val timeStart = System.currentTimeMillis()
            for (j in 1 until phonebookSorted.size - i) {
                if (phonebookSorted[i].name < phonebookSorted[j].name) {
                    phonebookSorted[i] = phonebookSorted[j].also { phonebookSorted[j] = phonebookSorted[i] }
                }
            }
            val timeEnd = System.currentTimeMillis()
            bubbleTime += timeEnd - timeStart
            if (bubbleTime / linearTime >= 10) {
                bubbleBreak = true
                return
            }
        }
    }
    private fun jumpSearch(): Int {
        var found = 0
        val timeStart = System.currentTimeMillis()
        val width = sqrt(phonebookSorted.size.toDouble()).toInt()
        val steps = ceil(phonebookSorted.size.toDouble() / width).toInt()
        for (name in toFind)
            iteration@for (iteration in 0 until steps) {
                val blockStart = iteration * width
                val blockEnd = if (blockStart + width >= phonebookSorted.size) phonebookSorted.size - 1 else blockStart + width - 1
                if (name > phonebookSorted[blockEnd].name) {
                    continue@iteration
                } else {
                    for (i in blockEnd downTo blockStart step 1) {
                        if (name == phonebookSorted[i].name) {
                            found++
                            break@iteration
                        }
                    }
                    break@iteration
                }
        }
        val timeEnd = System.currentTimeMillis()
        jumpTime = timeEnd - timeStart
        return found
    }
    fun runBinarySearch() {
        println("Start searching (quick sort + binary search)...")
        phonebookSorted = phonebook.toMutableList()
        var timeStart = System.currentTimeMillis()
        quickSort(0, phonebookSorted.lastIndex)
        qsortTime = System.currentTimeMillis() - timeStart
        timeStart = System.currentTimeMillis()
        var found = 0
        for (elem in toFind)
            found += binarySearch(elem, 0, phonebookSorted.lastIndex)
        binaryTime = System.currentTimeMillis() - timeStart
        println("""
            Found $found / ${toFind.size} entries. Time taken: ${convertMsToTime(qsortTime + binaryTime)}
            Sorting time: ${convertMsToTime(qsortTime)}
            Searching time: ${convertMsToTime(binaryTime)}
            
        """.trimIndent())
    }
    private fun quickSort(startIndex: Int, endIndex: Int) {
        if (startIndex < endIndex) {
            val index = partition(startIndex, endIndex)
            quickSort(startIndex, index - 1)
            quickSort(index + 1, endIndex)
        }
    }
    private fun partition(startIndex: Int, endIndex: Int): Int {
        val pivot: String = phonebookSorted[(startIndex + endIndex) / 2].name
        var pivotIndex = (startIndex + endIndex) / 2
        var i = startIndex - 1
        for (j in startIndex..endIndex) {
            if (j == pivotIndex) continue
            if (phonebookSorted[j].name < pivot) {
                i++
                if (i == j) continue
                if (i == pivotIndex) pivotIndex = j
                swap(i, j)
            }
        }
        swap(i + 1, pivotIndex)
        return i + 1
    }
    private fun swap(i: Int, j: Int) {
        phonebookSorted[i] = phonebookSorted[j].also {
            phonebookSorted[j] = phonebookSorted[i]
        }
    }
    private fun binarySearch(s: String, startIndex: Int, endIndex: Int): Int {
        if (phonebookSorted[startIndex].name > s ||
            phonebookSorted[endIndex].name < s) return 0
        val middleIndex = (startIndex + endIndex) / 2
        if (phonebookSorted[middleIndex].name == s) return 1
        return if (phonebookSorted[middleIndex].name > s) binarySearch(s, startIndex, middleIndex - 1)
        else binarySearch(s, middleIndex + 1, endIndex)
    }
    fun runHashedSearch() {
        println("Start searching (hash table)...")
        var timeStart = System.currentTimeMillis()
        makeHashTable()
        hashTableTime = System.currentTimeMillis() - timeStart
        timeStart = System.currentTimeMillis()
        var found = 0
        for (name in toFind)
            found += hashSearch(name)
        hashSearchTime = System.currentTimeMillis() - timeStart
        println("""
            Found $found / ${toFind.size} entries. Time taken: ${convertMsToTime(hashTableTime + hashSearchTime)}
            Creating time: ${convertMsToTime(hashTableTime)}
            Searching time: ${convertMsToTime(hashSearchTime)}
        """.trimIndent())

    }
    private fun makeHashTable() {
        phonebookHashed = HashMap(phonebook.size)
        for (e in phonebook) {
            phonebookHashed[e.name] = e.phone
        }
    }
    private fun hashSearch(s: String): Int {
        return if (phonebookHashed.containsKey(s)) 1 else 0
    }
}
fun main() {
    val phoneBook = PhoneBook()
    phoneBook.runLinearSearch()
    phoneBook.runJumpSearch()
    phoneBook.runBinarySearch()
    phoneBook.runHashedSearch()
}
