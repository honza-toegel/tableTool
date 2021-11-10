package org.jto.tabletool

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFColor
import org.jgrapht.Graph
import org.jto.tabletool.graph.Edge
import org.jto.tabletool.graph.Vertex
import org.slf4j.LoggerFactory.getLogger
import java.io.File


class ExcelGraphLoader(
    val inputFileName: String,
    val graphTraversal: Graph<Vertex, Edge>
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun loadGraph() {
        logger.info("Loading input excel $inputFileName into graph")
        val workbook: Workbook = WorkbookFactory.create(File(inputFileName), null, true)
        for (sheet: Sheet in workbook.filter { it.sheetName.startsWith("~") }) {
            logger.info("Loading sheet: ${sheet.sheetName}")
            loadVertexFromSheet(sheet, graphTraversal)
        }
    }

    private fun loadVertexHeaderFromSheet(sheet: Sheet): VertexSheetHeader {

        fun getHeaderCellFunction(vertexCell: Cell, edgeCell: Cell): CellColorRelationType {
            val rgbColor = (vertexCell.cellStyle.fillBackgroundColorColor as XSSFColor).argbHex
            require(rgbColor == (edgeCell.cellStyle.fillBackgroundColorColor as XSSFColor).argbHex)
            { "The header cell in row 1 must have same collor as header cell in row 2, to express consistent function" }
            return when (rgbColor.substring(2)) {
                "FFE599" -> CellColorRelationType.InRelation
                "A4C2F4" -> CellColorRelationType.Main
                "F4CCCC" -> CellColorRelationType.OutRelation
                else -> error("Not allowed header color ${rgbColor}, allowed FFE599 (In), A4C2F4 (Main), F4CCCC (Out)")
            }
        }

        val vertexHeaderRow =
            requireNotNull(sheet.getRow(0)) { "The first header row is mandatory, cant be empty, sheet:${sheet.sheetName}" }
        val edgeHeaderRow =
            requireNotNull(sheet.getRow(1)) { "The second header row is mandatory, cant be empty, sheet:${sheet.sheetName}" }

        val headerVertexes: List<HeaderVertex> = (vertexHeaderRow zip edgeHeaderRow).map {
            when (getHeaderCellFunction(it.first, it.second)) {
                CellColorRelationType.Main -> MainHeaderVertex(it.first.columnIndex, it.first.getCellStringValue())
                CellColorRelationType.InRelation -> RelatedHeaderVertex(
                    it.first.columnIndex,
                    it.first.getCellStringValue(),
                    it.second.getCellStringValue(),
                    VertexRelationType.InRelation
                )
                CellColorRelationType.OutRelation -> RelatedHeaderVertex(
                    it.first.columnIndex,
                    it.first.getCellStringValue(),
                    it.second.getCellStringValue(),
                    VertexRelationType.OutRelation
                )
            }
        }

        val mainVertexInfo = requireNotNull(
            headerVertexes.filterIsInstance<MainHeaderVertex>().singleOrNull()
        ) { "Exactly one main vertex column is required, ${headerVertexes.filterIsInstance<MainHeaderVertex>()} provided" }
        val relatedVertexInfos = headerVertexes.filterIsInstance<RelatedHeaderVertex>()

        logger.info("$mainVertexInfo".replaceNewLines())
        logger.info("Related vertexes: $relatedVertexInfos".replaceNewLines())

        return VertexSheetHeader(mainVertexInfo, relatedVertexInfos)
    }

    private fun loadVertexFromSheet(sheet: Sheet, g: Graph<Vertex, Edge>) {
        with(loadVertexHeaderFromSheet(sheet)) {

            for (row: Row in sheet.drop(2)) {
                logger.info(row.joinToString(" | ", "| ", " |") { it.getCellStringValue().replaceNewLines() })
                row.getCell(mainVertexInfo.colIndex)?.parseVertexData()?.forEach { mainVertexData ->
                    val mainVertex = searchOrCreateNamedVertex(g, mainVertexInfo.labels, mainVertexData)
                    relatedVertexHeaders.forEach { relatedVertexInfo ->
                        row.getCell(relatedVertexInfo.colIndex).parseVertexData().forEach { relatedVertex ->
                            processRelatedVertex(relatedVertex, g, relatedVertexInfo, mainVertex)
                        }
                    }
                }
            }
        }
    }

    private fun processRelatedVertex(
        relatedVertexData: VertexData,
        g: Graph<Vertex, Edge>,
        relatedVertexInfo: RelatedHeaderVertex,
        mainVertex: Vertex
    ) {
        val relatedVertex = searchOrCreateNamedVertex(g, relatedVertexInfo.labels, relatedVertexData)
        when (relatedVertexInfo.relationType) {
            VertexRelationType.InRelation ->
                g.addEdge(relatedVertex, mainVertex, Edge(relatedVertexInfo.edgeLabel))
            VertexRelationType.OutRelation ->
                g.addEdge(mainVertex, relatedVertex, Edge(relatedVertexInfo.edgeLabel))
        }
    }

    private fun searchOrCreateNamedVertex(
        g: Graph<Vertex, Edge>,
        defaultVertexLabels: List<VertexLabel>,
        vertexData: VertexData
    ): Vertex {
        val vertexLabel = vertexData.label
        val vertexName = vertexData.name
        val vertexLabels = defaultVertexLabels.filter {
            when (vertexLabel.isNotBlank()) {
                true -> it.alias == vertexLabel || it.label == vertexLabel
                false -> true
            }
        }
        require(vertexLabels.isNotEmpty()) { "No vertex labels left in order to search/create vertex after filtering by '$vertexLabel', extracted from: '$vertexData'. Check the list of defined labels $defaultVertexLabels with the given name: '$vertexData'" }
        vertexLabels.forEach { vertexLabel ->
            val existingVertex =
                g.vertexSet().filter { v -> v.label == vertexLabel.label && v.name == vertexName }.toSet()
            if (existingVertex.isNotEmpty())
                return existingVertex.first()
        }
        val singleVertexLabel = requireNotNull(vertexLabels.singleOrNull())
        { "Please use one of the vertex labels $vertexLabels as prefix before actual vertex '$vertexData' in order to create new vertex. If you like to re-use existing vertex, check the vertex label/name '$vertexData' because it was not found" }.label

        return Vertex(singleVertexLabel, vertexName).apply {
            g.addVertex(this)
        }
    }
}