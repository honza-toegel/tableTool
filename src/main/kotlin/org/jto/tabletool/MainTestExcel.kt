package org.jto.tabletool

import java.io.FileOutputStream

fun main(args: Array<String>) {
    //RighOnly Rose, LeftOnly Green
    val leftTable = setOf( //Extract
        mapOf("mftType" to "GNZL3X01", "suman" to "001", "deman" to "019"),
        mapOf("mftType" to "GNZL3X01", "suman" to "001", "deman" to "019"), //Duplikat
        mapOf("mftType" to "L3XPTK01", "suman" to "891", "deman" to "891"),
        mapOf("mftType" to "L3XPTK01", "suman" to "895", "deman" to "895"),
        mapOf("mftType" to "AXJL3X01", "suman" to "019", "deman" to "895") //LeftOnly
    )
    val rightTable = setOf( //Definitions
        mapOf("mftType" to "GNZL3X01", "suman" to "001"), //Partially in previous set
        mapOf("mftType" to "GNZL3X01", "suman" to "019"), //Not in the previous set - Right Only
        mapOf("mftType" to "L3XPTK01", "suman" to "891", "deman" to "891"), //Fully in previous set
        mapOf("mftType" to "L3XPTK01", "suman" to "895", "deman" to "890")
    )

    val result = TableDataComparator(leftTable, rightTable, setOf("id")).compareTables()

    val workbook = StyledWorkbook()
    ComparatorResultExcelWriter( result, listOf("mftType","suman","deman").map { OutputHeaderColumn(it) }, workbook).createCompareResultSheet()
    workbook.write(FileOutputStream("data/out/Result.xlsx"))
}