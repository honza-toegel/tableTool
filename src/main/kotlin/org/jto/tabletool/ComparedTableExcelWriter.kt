package org.jto.tabletool

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch
import org.slf4j.LoggerFactory

class IgnoredOutputHeaderColumn(
    col: OutputHeaderColumn,
    val ignoredDisplayLeft: Boolean //Indicate if the left value should be displayed on ignoredCompare column, or right value
) : OutputHeaderColumn(col.name, col.alias, col.ignoredCompare)

class ComparatorResultExcelWriter(
    private val compareResult: Map<String, List<RowCompareResult>>,
    headerColumns: List<OutputHeaderColumn>,
    private val leftName: String,
    private val rightName: String,
    private val wb: StyledWorkbook,
    private val sortByFileds: List<String> = listOf("senderEnvironment", "receiverEnvironment", "senderMandator", "receiverMandator", "senderServer", "receiverServer")
) {
    //private val leftShortcut = leftName.substring(0..1)
    //private val rightShortcut = rightName.substring(0..1)

    private val headerColumns: List<OutputHeaderColumn> = headerColumns.flatMap {
        when (it.ignoredCompare) {
            true ->
                when (it.ignoredOutputDisplay) {
                    OutputDisplay.Both -> listOf(
                        IgnoredOutputHeaderColumn(it, true),
                        IgnoredOutputHeaderColumn(it, false)
                    )
                    OutputDisplay.NoOutput -> emptyList()
                }
            false -> listOf(it)
        }
    }

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    fun createCompareResultSheet() {
        val headerOffset = 1

        with(wb.createSheet("Compare Result")) {
            createHeaderRow(this, headerOffset)
            createDataRows(this, headerOffset)
            (0..headerColumns.lastIndex + headerOffset).forEach { columnIndex -> autoSizeColumn(columnIndex) }
        }
    }


    private fun createDataRows(sheet: Sheet, headerOffset: Int) {
        with(sheet) {
            val sortBySelectors = sortByFileds.map<String, (RowCompareResult) -> String?> { sortByField -> {it.cells[sortByField]?.toSortByString()} }
            val sortByCriteria = compareBy(*sortBySelectors.toTypedArray())
            compareResult.keys.sorted().forEach { key ->
                compareResult[key]?.sortedWith(sortByCriteria)?.forEach { rowCompareResult ->
                    with(createRow(lastRowNum + 1)) {
                        createDataRowHeaderCell(createCell(0), rowCompareResult)
                        createDataRowDataCells(rowCompareResult, this, headerOffset)
                    }
                }
            }
        }
    }

    private fun createHeaderRow(sheet: Sheet, headerOffset: Int) {
        with(sheet) {
            val headerRow = createRow(0)
            headerRow.createCell(0).apply {
                setCellValue("<>")
                cellStyle = wb.styles["header"]
            }
            headerColumns.forEachIndexed { columnIndex, column ->
                headerRow.createCell(columnIndex + headerOffset).apply {
                    setCellValue(column.alias)
                    cellStyle = wb.styles["header"]
                }
            }
        }
    }

    private fun createDataRowHeaderCell(
        resultCell: Cell,
        rowCompareResult: RowCompareResult
    ) {
        with(resultCell) {
            when (rowCompareResult.display) {
                RowCompareDisplay.LeftOnly -> {
                    setCellValue("+")
                    cellStyle = wb.styles["leftOnly"]
                }
                RowCompareDisplay.RightOnly -> {
                    setCellValue("-")
                    cellStyle = wb.styles["rightOnly"]
                }
                RowCompareDisplay.LeftRightCompared -> {
                    when (rowCompareResult.totalScore == 1.0) {
                        true -> {
                            setCellValue("=")
                            setCellComment(
                                "Comparator",
                                "Total points:${rowCompareResult.totalPoints}\nComparable score:${rowCompareResult.totalScore}"
                            )
                            cellStyle = wb.styles["normal"]

                        }
                        false -> {
                            setCellValue("~")
                            setCellComment(
                                "Comparator",
                                "Total points:${rowCompareResult.totalPoints}\nComparable score:${rowCompareResult.totalScore}"
                            )
                            cellStyle = wb.styles["comparable"]
                        }
                    }
                }
            }
        }
    }

    private fun createDataRowDataCells(rowCompareResult: RowCompareResult, row: Row, headerOffset: Int) {
        with(row) {
            headerColumns.forEachIndexed { columnIndex, column ->
                createResultDataCell(
                    createCell(columnIndex + headerOffset),
                    rowCompareResult, column
                )
            }
        }
    }


    private fun createResultDataCell(
        resultCell: Cell,
        rowCompareResult: RowCompareResult,
        outputHeaderColumn: OutputHeaderColumn
    ) {
        with(resultCell) {
            val cellCompareResult = rowCompareResult.cells[outputHeaderColumn.name]
            if (cellCompareResult != null) {
                setDataCellStyle(resultCell, outputHeaderColumn, rowCompareResult, cellCompareResult)
                setDataCellValue(resultCell, outputHeaderColumn, rowCompareResult, cellCompareResult)
                setDataCellComment(resultCell, outputHeaderColumn, rowCompareResult, cellCompareResult)
            } else {
                setCellValue("")
            }
        }
    }

    private fun setDataCellComment(
        cell: Cell,
        outputHeaderColumn: OutputHeaderColumn,
        rowCompareResult: RowCompareResult,
        cellCompareResult: CellCompareResult
    ) {
        with(cellCompareResult) {
            val value: String = when (outputHeaderColumn.ignoredCompare) {
                true -> ""
                false -> when (rowCompareResult.display) {
                    RowCompareDisplay.LeftOnly -> ""
                    RowCompareDisplay.RightOnly -> ""
                    RowCompareDisplay.LeftRightCompared -> {
                        //"cr:${cellCompareResult.compareResult}"
                        when (display) {
                            CellCompareDisplay.Comparable -> when (cellCompareResult.compareResult == 1.0) {
                                true -> ""
                                false -> "$leftName: '$leftValue'\n$rightName: '$rightValue'"
                            }
                            CellCompareDisplay.LeftOnly -> ""
                            CellCompareDisplay.RightOnly -> ""
                            else -> ""
                        }
                    }
                }
            }
            if (value.isNotBlank())
                cell.setCellComment("Comparator", "$value\ncr:${cellCompareResult.compareResult}" )
        }
    }

    private fun setDataCellValue(
        cell: Cell,
        outputHeaderColumn: OutputHeaderColumn,
        rowCompareResult: RowCompareResult,
        cellCompareResult: CellCompareResult
    ) {
        with(cellCompareResult) {
            val leftVal:TableValue = leftValue ?: StringTableValue.Empty
            val rightVal:TableValue = rightValue ?: StringTableValue.Empty
            val value:Any = when (outputHeaderColumn.ignoredCompare) {
                true -> when ((outputHeaderColumn as IgnoredOutputHeaderColumn).ignoredDisplayLeft) {
                    true -> when (rowCompareResult.display) {
                        RowCompareDisplay.RightOnly -> ""
                        else -> leftVal
                    }
                    false -> when (rowCompareResult.display) {
                        RowCompareDisplay.LeftOnly -> ""
                        else -> rightVal
                    }
                }
                false -> when (rowCompareResult.display) {
                    RowCompareDisplay.LeftOnly -> leftVal
                    RowCompareDisplay.RightOnly -> rightVal
                    RowCompareDisplay.LeftRightCompared -> {
                        when (display) {
                            CellCompareDisplay.Comparable -> when ( compareResult > 0.96 ) {
                                true -> createComparedRichText(diffTextResult)
                                false -> createLeftRightText(leftVal, rightVal)
                            }
                            CellCompareDisplay.LeftOnly -> leftVal
                            CellCompareDisplay.RightOnly -> rightVal
                            CellCompareDisplay.Ignored -> leftVal
                            else -> "N/A"
                        }
                    }
                }
            }

            when (value) {
                is String -> cell.setCellValue(value)
                is TableValue -> cell.setCellValue(value.toString())
                is XSSFRichTextString -> cell.setCellValue(value)
            }
        }
    }


    private fun setDataCellStyle(
        cell: Cell,
        outputHeaderColumn: OutputHeaderColumn,
        rowCompareResult: RowCompareResult,
        cellCompareResult: CellCompareResult
    ) {
        cell.cellStyle = when (outputHeaderColumn.ignoredCompare) {
            true -> wb.styles["normal"]
            false -> when (rowCompareResult.display) {
                RowCompareDisplay.LeftOnly -> wb.styles["leftOnly"]
                RowCompareDisplay.RightOnly -> wb.styles["rightOnly"]
                RowCompareDisplay.LeftRightCompared -> {
                    when (cellCompareResult.display) {
                        CellCompareDisplay.Comparable -> {
                            when (cellCompareResult.diffTextResult.size == 1) {
                                true -> when (cellCompareResult.diffTextResult[0].operation) {
                                    DiffMatchPatch.Operation.INSERT -> wb.styles["rightOnly"]
                                    DiffMatchPatch.Operation.DELETE -> wb.styles["leftOnly"]
                                    DiffMatchPatch.Operation.EQUAL -> wb.styles["normal"]
                                }
                                false -> wb.styles["comparable"]
                            }
                        }
                        CellCompareDisplay.LeftOnly -> wb.styles["notComparableLeft"]
                        CellCompareDisplay.RightOnly -> wb.styles["notComparableRight"]
                        else -> wb.styles["normal"]
                    }
                }
            }
        }
    }

    private fun createComparedRichText(
        diffTextResult: List<DiffMatchPatch.Diff>
    ): XSSFRichTextString {
        val richText = XSSFRichTextString()
        diffTextResult.forEach {
            richText.append(
                it.text,
                when (it.operation) {
                    DiffMatchPatch.Operation.INSERT -> wb.fonts["underline"]
                    DiffMatchPatch.Operation.DELETE -> wb.fonts["strikeout"]
                    DiffMatchPatch.Operation.EQUAL -> wb.fonts["normal"]
                }
            )
        }
        return richText
    }

    private fun createLeftRightText(leftVal: TableValue, rightVal: TableValue): XSSFRichTextString {
        val richText = XSSFRichTextString()
        richText.append(leftVal.toString(), wb.fonts["underline"])
        richText.append(rightVal.toString(), wb.fonts["strikeout"])
        return richText
    }
}