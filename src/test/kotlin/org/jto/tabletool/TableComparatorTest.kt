package org.jto.tabletool

import org.junit.Test

class TableComparatorTest {
    @Test
    fun testTableDataComparatorTest() {

        val leftTable = setOf( //Extract
            mapOf("mftType" to "GNZL3X01", "suman" to "001", "deman" to "019"),
            mapOf("mftType" to "GNZL3X01", "suman" to "001", "deman" to "019"), //Duplikat
            mapOf("mftType" to "L3XPTK01", "suman" to "891", "deman" to "891"),
            mapOf("mftType" to "L3XPTK01", "suman" to "895", "deman" to "895")
        ).map { it.map { it.key to StringTableValue(it.value) }.toMap() }.toSet()
        val rightTable = setOf( //Definitions
            mapOf("mftType" to "GNZL3X01", "suman" to "001"), //Partially in previous set
            mapOf("mftType" to "GNZL3X01", "suman" to "019"), //Not in the previous set
            mapOf("mftType" to "L3XPTK01", "suman" to "891", "deman" to "891"), //Fully in previous set
            mapOf("mftType" to "L3XPTK01", "suman" to "895", "deman" to "890")
        ).map { it.map { it.key to StringTableValue(it.value) }.toMap() }.toSet()

        val result = TableComparator(leftTable, rightTable, emptySet()).compareTables()
        result.forEach { println(it) }
    }
}