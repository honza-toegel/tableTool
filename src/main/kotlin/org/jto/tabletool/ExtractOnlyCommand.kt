package org.jto.tabletool

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.FileOutputStream
import kotlin.system.exitProcess

@Command(name="extract", description = ["Extract the table data out of given input"])
class ExtractOnlyCommand : Runnable {

    @Option(names = ["-t", "--input-type"], required = false, description = ["Type of the left input \nTable is the simple excel file with ~sheets containing table data\nGraph definition is excel file containing graph definition which can be loaded, and given graph query is executed to extract data table"])
    var inputType: InputType = InputType.GraphDefinitionFile

    @Option(names = ["-f", "--input-file"], required = true, description = ["Left input file for the given input type"])
    var inputFile: String = ""

    @Option(names = ["-of", "--output-file"], required = true, description = ["Output file where will be the compare result stored"])
    var outputFile: String = ""

    val groupByField = "mftType"

    @Option(names = ["-oc"], required = false, description = ["Column list which would be reported, in exact order and format (colNameFromGraph:colNameTobeReported;..)"])
    var outputColumns :String =
        "id:Id:i;mftService:Service Name;mftType:ASI File Type;senderServer:Supplier Host;receiverServer:Receiver Host;senderServerGroup:Supplier HostGroup:i;receiverServerGroup:Receiver HostGroup:i;postScript:Postprocessing Command;" +
                "postScriptParams:Postprocessing Arguments;receiverDirectory:Receiver Directory;senderUID:Supplier UID;receiverUID:Receiver UID;senderMandator:SUMAN;senderEnvironment:SURTE;receiverMandator:DEMAN;receiverEnvironment:DERTE;" +
                "instance:Instance;validFrom:Valid From;validTo:Valid To;state:State"


    override fun run() {
        val outputHeaderColumns =
            outputColumns.split(';').map {
                val parsedColumn = requireNotNull(
                    "^(\\w+):([\\w\\s]+)(:(i))?$".toRegex().find(it)?.destructured
                ) { "The column $it in column list must follow the regexp ^(\\w+):([\\w\\s]+)(:(i))?\$" }
                OutputHeaderColumn(
                    parsedColumn.component1(),
                    parsedColumn.component2(),
                    parsedColumn.component4().isNotBlank()
                )
            }

        val dataTable = CompareCommand.loadInputTable(inputType, inputFile)

        val workbook = StyledWorkbook()

        TableExcelWriter(
            dataTable,
            outputHeaderColumns,
            workbook,
            fileNameWithoutExtension(inputFile),
            groupByField
        ).createDefinitionsSheet()

        workbook.write(FileOutputStream(outputFile))
    }
}

fun main(args: Array<String>) {
    val exitCode: Int = CommandLine(ExtractOnlyCommand()).execute(*args)
    exitProcess(exitCode)
}