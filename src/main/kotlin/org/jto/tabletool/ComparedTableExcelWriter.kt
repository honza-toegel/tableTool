package org.jto.tabletool

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch
import org.slf4j.LoggerFactory

class IgnoredOutputHeaderColumn(
    col: OutputHeaderColumn,
    val ignoredDisplayLeft: Boolean //Indicate if the left value should be displayed on ignored column, or right value
) : OutputHeaderColumn(col.name, col.alias, col.ignored)

class ComparatorResultExcelWriter(
    private val compareResult: Map<String, List<RowCompareResult>>,
    headerColumns: List<OutputHeaderColumn>,
    private val wb: StyledWorkbook
) {

    private val headerColumns: List<OutputHeaderColumn> = headerColumns.flatMap {
        when (it.ignored) {
            true -> listOf(IgnoredOutputHeaderColumn(it, true), IgnoredOutputHeaderColumn(it, false))
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
            compareResult.keys.sorted().forEach { key ->
                compareResult[key]?.forEach { rowCompareResult ->
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
                    when (rowCompareResult.comparableScore == 1.0) {
                        true -> {
                            setCellValue("= tp:${rowCompareResult.totalPoints} cs:${rowCompareResult.comparableScore} ts:${rowCompareResult.totalScore}"); cellStyle = wb.styles["normal"]
                        }
                        false -> {
                            setCellValue("~"); cellStyle = wb.styles["comparable"]
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
            } else {
                setCellValue("")
            }
        }
    }

    private fun setDataCellValue(
        cell: Cell,
        outputHeaderColumn: OutputHeaderColumn,
        rowCompareResult: RowCompareResult,
        cellCompareResult: CellCompareResult
    ) {
        with (cellCompareResult) {
            val value = when (outputHeaderColumn.ignored) {
                true -> when ((outputHeaderColumn as IgnoredOutputHeaderColumn).ignoredDisplayLeft) {
                    true ->  when (rowCompareResult.display) {
                        RowCompareDisplay.RightOnly -> ""
                        else -> leftValue ?: ""
                    }
                    false -> when (rowCompareResult.display) {
                        RowCompareDisplay.LeftOnly -> ""
                        else -> rightValue ?: ""
                    }
                }
                false -> when (rowCompareResult.display) {
                    RowCompareDisplay.LeftOnly -> leftValue ?: ""
                    RowCompareDisplay.RightOnly -> cell.setCellValue(rightValue ?: "")
                    RowCompareDisplay.LeftRightCompared -> {
                        when (display) {
                            CellCompareDisplay.Comparable -> createComparedRichText(diffTextResult)
                            CellCompareDisplay.LeftOnly -> leftValue ?: ""
                            CellCompareDisplay.RightOnly -> rightValue ?: ""
                            else -> "N/A"
                        }
                    }
                }
            }

            when (value) {
                is String -> cell.setCellValue(value)
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
        cell.cellStyle = when (outputHeaderColumn.ignored) {
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

    private fun createComparedRichText(diffTextResult: List<DiffMatchPatch.Diff>): XSSFRichTextString {
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
}