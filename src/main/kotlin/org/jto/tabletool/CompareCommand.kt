package org.jto.tabletool

import kotlinx.cli.*
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.slf4j.LoggerFactory
import java.io.FileOutputStream

class CompareCommand : Subcommand("compare", "Compare two data tables (left <> right) and  ") {

    val logger = LoggerFactory.getLogger("Main")

    val leftInputType by option(
        ArgType.Choice<InputType>(),
        shortName = "lt",
        description = "Type of the left input \nTable is the simple excel file with ~sheets containing table data\nGraph definition is excel file containing graph definition which can be loaded, and given graph query is executed to extract data table"
    ).default(InputType.GraphDefinitionFile)
    val leftInputFile by option(
        ArgType.String,
        shortName = "lf",
        description = "Left input file for the given input type"
    ).required()

    val rightInputType by option(
        ArgType.Choice<InputType>(),
        shortName = "rt",
        description = "Type of the right input \nTable is the simple excel file with ~sheets containing table data\nGraph definition is excel file containing graph definition which can be loaded, and given graph query is executed to extract data table"
    ).default(InputType.Table)
    val rightInputFile by option(
        ArgType.String,
        shortName = "rf",
        description = "Left input file for the given input type"
    ).required()

    val outputFile by option(
        ArgType.String,
        shortName = "of",
        description = "Output file where will be the compare result stored"
    ).required()

    val minimumComparableScore by option(
        ArgType.Double,
        shortName = "ms",
        description = "Minimum diff score to asses results as equal"
    ).default(1.0)
    val groupByField by option(
        ArgType.String,
        shortName = "gf",
        description = "Group results by field"
    ).default("mftType")
    val outputColumns by option(
        ArgType.String,
        shortName = "oc",
        description = "Column list which would be reported, in exact order, by default () "
    )
        .default(
            "id:Id:ib;mftService:Service Name;mftType:ASI File Type;senderServer:Supplier Host;receiverServer:Receiver Host;senderServerGroup:Supplier HostGroup:i;receiverServerGroup:Receiver HostGroup:i;postScript:Postprocessing Command;" +
                    "postScriptParams:Postprocessing Arguments;receiverDirectory:Receiver Directory;senderUID:Supplier UID;receiverUID:Receiver UID;senderMandator:SUMAN;senderEnvironment:SURTE;receiverMandator:DEMAN;receiverEnvironment:DERTE;" +
                    "instance:Instance;validFrom:Valid From;validTo:Valid To;state:State"
        )

    override fun execute() {

        val outputHeaderColumns =
            outputColumns.split(';').map {
                val parsedColumn = requireNotNull(
                    "^(\\w+):([\\w\\s]+)(:(i[b]?))?$".toRegex().find(it)?.destructured
                ) { "The column $it in column list must follow the regexp ^(\\w+):([\\w\\s]+)(:(i))?\$" }
                OutputHeaderColumn(
                    parsedColumn.component1(),
                    parsedColumn.component2(),
                    parsedColumn.component4().isNotBlank(),
                    when (parsedColumn.component4().endsWith('b')) {
                        true -> OutputDisplay.Both
                        else -> OutputDisplay.NoOutput
                    }
                )
            }

        // Left source table
        val leftTable = loadInputTable(leftInputType, leftInputFile)

        // Right source table
        val rightTable = loadInputTable(rightInputType, rightInputFile)

        val leftInputName = fileNameWithoutExtension(leftInputFile)
        val rightInputName = fileNameWithoutExtension(rightInputFile)

        // Compare left with right table
        val result = TableComparator(leftTable, rightTable, setOf(groupByField) + outputHeaderColumns.filter { it.ignoredCompare }.map { it.name },
            groupByField, minimumComparableScore
        ).compareTables()

        logger.info("Preparing result excel: $outputFile")
        val workbook = StyledWorkbook()
        logger.info("Adding compare left <> right sheet")
        ComparatorResultExcelWriter(result, outputHeaderColumns, leftInputName, rightInputName, workbook).createCompareResultSheet()
        logger.info("Adding left file sheet")
        TableExcelWriter(leftTable, outputHeaderColumns, workbook, leftInputName, groupByField ).createDefinitionsSheet()
        logger.info("Adding right file sheet")
        TableExcelWriter(rightTable, outputHeaderColumns, workbook, rightInputName, groupByField ).createDefinitionsSheet()

        logger.info("Writing compare result into file: $outputFile")
        workbook.write(FileOutputStream(outputFile))
        logger.info("Done")
    }

    companion object {
        fun loadInputTable(inputType: InputType, inputFile: String): Set<Map<String, TableValue>> {
            return when (inputType) {
                InputType.GraphDefinitionFile -> {
                    val graph: Graph = TinkerGraph.open()
                    val g: GraphTraversalSource = graph.traversal()
                    //Load graph from excel
                    ExcelGraphLoader(inputFile, g).loadGraph()
                    //Extract data out of graph (can be replaced by groovy to have configurable query)
                    GraphQueryTableLoader(g).extractData()
                }
                InputType.Table -> {
                    ExcelTableLoader(inputFile).loadTableData()
                }
            }.mapIndexed { rowIndex, row -> row + ("id" to StringTableValue("$rowIndex")) }.toSet()
        }
    }
}