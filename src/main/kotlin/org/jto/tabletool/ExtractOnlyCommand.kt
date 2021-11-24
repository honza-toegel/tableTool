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
        "id:Nr:i;mftService:Service Dir;instance:Instance;mftType:File Type;_senderServerGroup:Supplier HostGroup:i;_receiverServerGroup:Consumer HostGroup:i;" +
                "_senderServer:Supplier Host;_senderUID:Supplier User;" +
                "senderServer:Supplier Host NEW;senderUID:Supplier User NEW;" +
                "_senderMandator:SUMAN;_senderEnvironment:SURTE;" +
                "senderMandator:SUMAN NEW;senderEnvironment:SURTE NEW;" +
                "_receiverServer:Consumer Host;_receiverUID:Consumer User;" +
                "receiverServer:Consumer Host NEW;receiverUID:Consumer User NEW;" +
                "_receiverMandator:DEMAN;_receiverEnvironment:DERTE;" +
                "receiverMandator:DEMAN NEW;receiverEnvironment:DERTE NEW;" +
                "transferType:Transfer Type;" +
                "_postScript:Postprocessing Command;_postScriptParams:Postprocessing Arguments;_receiverDirectory:Destination Directory;" +
                "postScript:Postprocessing Command NEW;postScriptParams:Postprocessing Arguments NEW;receiverDirectory:Destination Directory NEW;" +
                "validFrom:Valid From;uid:Unique ID;split:Split"

    /*
    Nr,Service Dir,Instance,File Type,Supplier HostGroup,Consumer HostGroup,Supplier Host,Supplier User,
    Supplier Host NEW,Supplier User NEW,
    SUMAN,SURTE,
    SUMAN NEW,SURTE NEW, -
    Consumer Host,Consumer User,
    Consumer Host NEW,Consumer User NEW,
    DEMAN,DERTE,
    DEMAN NEW,DERTE NEW,
    Transfer Type,
    Postprocessing Command,Postprocessing Arguments,Destination Directory,
    Postprocessing Command NEW,Postprocessing Arguments NEW,Destination Directory NEW,
    Valid From,Unique ID,Split
     */


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