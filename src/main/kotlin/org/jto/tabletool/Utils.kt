package org.jto.tabletool

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFFont
import java.io.File


fun String.replaceNewLines() = replace("\n", "; ")

private val dataFormatter = DataFormatter()
fun Cell.getCellStringValue() = dataFormatter.formatCellValue(this)
fun Row.getCellStringValue(column: Int) = dataFormatter.formatCellValue(getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL))
fun Cell.parseVertexData(): List<VertexData> {
    val font = sheet.workbook.getFontAt(cellStyle.fontIndexAsInt) as XSSFFont
    return when (font.xssfColor?.argbHex) {
        "FF0070C0" -> VertexData.parseFromJSONString(stringCellValue);
        else -> VertexData.parseFromSimpleString(stringCellValue);
    }
}

fun Cell.setCellComment(commentAuthor: String, commentText: String) {
    val factory = sheet.workbook.creationHelper

    //Show the comment box at the bottom right corner
    val anchor = factory.createClientAnchor().apply {
        setCol1(columnIndex + 1) //the box of the comment starts at this given column...
        setCol2(columnIndex + 4) //...and ends at that given column
        row1 = row.rowNum + 1 //one row below the cell...
        row2 = row.rowNum + 5 //...and 4 rows high
    }

    cellComment = sheet.createDrawingPatriarch().createCellComment(anchor).apply {
        //Set the comment text and commentAuthor
        string = factory.createRichTextString(commentText)
        author = commentAuthor
    }
}

fun fileNameWithoutExtension(fileName:String) = File(fileName).nameWithoutExtension