package org.jto.tabletool

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import java.io.File

fun String.replaceNewLines() = replace("\n", "; ")

private val dataFormatter = DataFormatter()
fun Cell.getCellStringValue() = dataFormatter.formatCellValue(this)
fun Row.getCellStringValue(column: Int) = dataFormatter.formatCellValue(getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL))

fun fileNameWithoutExtension(fileName:String) = File(fileName).nameWithoutExtension