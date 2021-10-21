package org.jto.tabletool

import org.apache.poi.ss.usermodel.Sheet

class TableExcelWriter(
    val dataTable: Set<Map<String, TableValue>>,
    val headerColumns: List<OutputHeaderColumn>,
    val wb: StyledWorkbook,
    val sheetName: String,
    val groupByField: String = "mftType",
    val sortByFileds: List<String> = listOf("senderEnvironment", "receiverEnvironment", "senderMandator", "receiverMandator", "senderServer", "receiverServer")
) {

    fun createDefinitionsSheet() {
        with(wb.createSheet(sheetName)) {
            createHeaderRow(this)
            createDataRows(this)
            (0..headerColumns.lastIndex).forEach { columnIndex -> autoSizeColumn(columnIndex) }
        }
    }

    private fun createHeaderRow(sheet: Sheet) {
        with(sheet) {
            val headerRow = createRow(0)
            headerColumns.forEachIndexed { columnIndex, column ->
                headerRow.createCell(columnIndex).apply {
                    setCellValue(column.alias)
                    cellStyle = wb.styles["header"]
                }
            }
        }
    }

    private fun createDataRows(sheet: Sheet) {
        with(sheet) {
            val sortBySelectors = sortByFileds.map<String, (Map<String,TableValue>) -> String?> { sortByField -> {it[sortByField].toString()} }
            val sortByCriteria = compareBy(*sortBySelectors.toTypedArray())
            val groupedDataTable = dataTable.groupBy { it[groupByField].toString() }
            groupedDataTable.keys.sorted().forEach { key ->
                groupedDataTable[key]?.sortedWith(sortByCriteria)?.forEach { dataRow ->
                    with(createRow(lastRowNum + 1)) {
                        headerColumns.forEachIndexed { columnIndex, column ->
                            createCell(columnIndex).apply {
                                val tableValue = dataRow.getOrDefault(column.name, StringTableValue.Empty)
                                setCellValue(tableValue.toString())
                                when (tableValue) {
                                    is StringTableValue -> cellStyle = wb.styles["normal"]
                                    is RegExpTableValue -> {
                                        cellStyle = wb.styles["regex"]
                                        setCellComment("TT", tableValue.regex.pattern)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}