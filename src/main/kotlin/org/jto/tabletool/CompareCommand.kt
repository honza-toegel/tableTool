package org.jto.tabletool

import org.jgrapht.Graph
import org.jgrapht.graph.DirectedPseudograph
import org.jto.tabletool.graph.Edge
import org.jto.tabletool.graph.Vertex
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.FileOutputStream
import kotlin.system.exitProcess


@Command(name = "extract", description = ["Compare two data tables (left <> right) and  "])
class CompareCommand: Runnable {

    val logger = LoggerFactory.getLogger("Main")

    @Option(names = ["-lt", "--left-input-type"], required = false, description = ["Type of the left input \nTable is the simple excel file with ~sheets containing table data\nGraph definition is excel file containing graph definition which can be loaded, and given graph query is executed to extract data table"])
    var leftInputType: InputType = InputType.GraphDefinitionFile

    @Option(names = ["-lf", "--left-input-file"], required = true, description = ["Left input file for the given input type"])
    var leftInputFile: String = ""

    @Option(names = ["-rt", "--left-input-file"], required = false, description = ["Type of the right input \n" +
            "Table is the simple excel file with ~sheets containing table data\n" +
            "Graph definition is excel file containing graph definition which can be loaded, and given graph query is executed to extract data table"])
    var rightInputType: InputType= InputType.Table

    @Option(names = ["-rf", "--right-input-file"], required = true, description = ["Right input file for the given input type"])
    var rightInputFile: String = ""

    @Option(names = ["-of", "--output-file"], required = true, description = ["Output file where will be the compare result stored"])
    var outputFile: String = ""

    @Option(names = ["-ms"], required = false, description = ["Minimum diff score to asses results as equal"])
    var minimumComparableScore : Double = 1.0

    val groupByField :String = "mftType"

    @Option(names = ["-oc"], required = false, description = ["Column list which would be reported, in exact order and format (colNameFromGraph:colNameTobeReported;..)"])
    var outputColumns :String =
            "id:Id:ib;mftService:Service Name;mftType:ASI File Type;senderServer:Supplier Host;receiverServer:Receiver Host;senderServerGroup:Supplier HostGroup:i;receiverServerGroup:Receiver HostGroup:i;postScript:Postprocessing Command;" +
                    "postScriptParams:Postprocessing Arguments;receiverDirectory:Receiver Directory;senderUID:Supplier UID;receiverUID:Receiver UID;senderMandator:SUMAN;senderEnvironment:SURTE;receiverMandator:DEMAN;receiverEnvironment:DERTE;" +
                    "instance:Instance;validFrom:Valid From;validTo:Valid To;state:State"

    override fun run() {
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
                    val g: Graph<Vertex, Edge> = DirectedPseudograph(Edge::class.java)
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

fun main(args: Array<String>) {
    val exitCode: Int = CommandLine(CompareCommand()).execute(*args)
    exitProcess(exitCode)
}