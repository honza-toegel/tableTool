package org.jto.tabletool

import org.apache.poi.ss.usermodel.*
import org.slf4j.LoggerFactory
import java.io.File

class ExcelTableLoader(val inputFileName: String) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)

        val defaultHeader = listOf(
            "mftService",
            "mftType",
            "senderServer",
            "receiverServer",
            "senderServerGroup",
            "receiverServerGroup",
            "postScript",
            "postScriptParameters",
            "receiverDirectory",
            "senderUID",
            "receiverUID",
            "senderMandator",
            "senderEnvironment",
            "receiverMandator",
            "receiverEnvironment",
            "instance",
            "validFrom",
            "validTo",
            "state"
        )
    }

    fun graphLoadTest(): Set<Map<String, String>> {
        logger.info("Loading input excel $inputFileName as table data")
        val workbook: Workbook = WorkbookFactory.create(File(inputFileName))

        workbook.filterNot { it.sheetName.startsWith("~") }.also {
            if (it.isNotEmpty()) logger.info("Skipping following sheets which doesnt begin with ~ '$it'")
        }
        return workbook.filter { it.sheetName.startsWith("~") }.flatMap { loadDataFromSheet(it) }.toSet()
    }

    private fun loadDataFromSheet(sheet: Sheet): Set<Map<String, String>> {
        logger.info("Loading sheet: ${sheet.sheetName}")

        val hasExcelHeader = sheet.firstOrNull()?.firstOrNull()?.stringCellValue?.startsWith('~') ?: false
        val header = when (hasExcelHeader) {
            true -> loadHeaderFromSheet(sheet)
            false -> defaultHeader
        }

        return sheet.drop(1).map {
            header.mapIndexed { columnIndex, columnName -> columnName to it.getCellStringValue(columnIndex) }.toMap()
        }.filter { it.values.any { it.isNotBlank() } }.apply {
            forEach { logger.debug(it.toString()) }
            logger.info("Sheet ${sheet.sheetName} loaded $size rows")
        }.toSet()
    }

    private fun loadHeaderFromSheet(sheet: Sheet): List<String> =
        sheet.getRow(0).map { it.getCellStringValue().removePrefix("~") }.filter { it.isNotBlank() }

}
