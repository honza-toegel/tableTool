package org.jto.tabletool

import kotlinx.cli.*
import java.io.FileOutputStream

class ExtractOnlyCommand : Subcommand("extract", "Extract the table data out of given input") {
    val inputType by option(
        ArgType.Choice<InputType>(),
        shortName = "t",
        description = "Type of the input \nTable is the simple excel file with ~sheets containing table data\nGraph definition is excel file containing graph definition which can be loaded, and given graph query is executed to extract data table"
    ).default(InputType.GraphDefinitionFile)
    val inputFile by option(
        ArgType.String,
        shortName = "f",
        description = "input file for the given input type"
    ).required()

    val outputFile by option(
        ArgType.String,
        shortName = "of",
        description = "Output file where will be the result stored"
    ).required()

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
            "id:Id:i;mftService:Service Name;mftType:ASI File Type;senderServer:Supplier Host;receiverServer:Receiver Host;senderServerGroup:Supplier HostGroup;receiverServerGroup:Receiver HostGroup;postScript:Postprocessing Command;" +
                    "postScriptParams:Postprocessing Arguments;receiverDirectory:Receiver Directory;senderUID:Supplier UID;receiverUID:Receiver UID;senderMandator:SUMAN;senderEnvironment:SURTE;receiverMandator:DEMAN;receiverEnvironment:DERTE;" +
                    "instance:Instance;validFrom:Valid From;validTo:Valid To;state:State"
        )


    override fun execute() {
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