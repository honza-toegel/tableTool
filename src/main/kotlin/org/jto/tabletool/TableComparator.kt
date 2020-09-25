package org.jto.tabletool

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch

class CellCompareResult(
    val leftValue: String?,
    val rightValue: String?,
    val compareResult: Double, //0..1 (0=no match, 1=exact match)
    val diffTextResult: List<DiffMatchPatch.Diff>,
    ignored: Boolean
) {

    val display: CellCompareDisplay = when (ignored) {
        true -> CellCompareDisplay.Ignored
        else ->
            if (leftValue == null && rightValue == null)
                CellCompareDisplay.NoValue
            else if (leftValue == null)
                CellCompareDisplay.RightOnly
            else if (rightValue == null)
                CellCompareDisplay.LeftOnly
            else
                CellCompareDisplay.Comparable
    }



    override fun toString(): String = when (compareResult == 1.0) {
        true -> "Eq: $leftValue"
        false -> "(L:$leftValue<>R:$rightValue) LL:$compareResult"
    }
}

enum class CellCompareDisplay {
    NoValue,
    LeftOnly,
    RightOnly,
    Comparable,
    Ignored
}

enum class RowCompareDisplay {
    LeftRightCompared, //The two records matches with enough comparableScore, will be displayed as diff of each other
    LeftOnly,          //The two records not matches, left record should be displayed
    RightOnly          //The two records not matches, left record should be displayed
}

class RowCompareResult(
    val cells: Map<String, CellCompareResult>, leftToRight: Boolean, minimumComparableScore: Double
) : Comparable<RowCompareResult> {
    val totalPoints: Double = cells.values.sumByDouble { it.compareResult }
    val comparableScore: Double = totalPoints / cells.values.filter { it.display == CellCompareDisplay.Comparable }.size
    val totalScore: Double = totalPoints / cells.values.filter { it.display != CellCompareDisplay.Ignored }.size

    /**
     * Indicate how should be the result of row compare displayed
     */
    val display: RowCompareDisplay = when (comparableScore > minimumComparableScore) {
        true -> RowCompareDisplay.LeftRightCompared
        else -> when (leftToRight) {
            true -> RowCompareDisplay.LeftOnly
            false -> RowCompareDisplay.RightOnly
        }
    }

    override fun compareTo(other: RowCompareResult): Int = totalPoints.compareTo(other.totalPoints)
    override fun toString(): String =
        "Cells: $cells totalPoints: $totalPoints, comparableScore: $comparableScore, totalScore: $totalScore, Display: $display"
}

class TableDataComparator(
    leftTable: Set<Map<String, String>>,
    rightTable: Set<Map<String, String>>,
    val ignoredColumns: Set<String>,
    val groupByField: String = "mftType",
    val minimumComparableScore: Double = 0.8
) {

    val leftTables: Map<String, Set<Map<String, String>>> =
        leftTable.groupBy { (it[groupByField] ?: "") }.map { it.key to it.value.toSet() }.toMap()
    val rightTables: Map<String, Set<Map<String, String>>> =
        rightTable.groupBy { it[groupByField] ?: "" }.map { it.key to it.value.toSet() }.toMap()

    fun compareTables(): Map<String, List<RowCompareResult>> =
        (leftTables.keys + rightTables.keys).map {
            it to
                    compareGroupedTables(
                        leftTables[it] ?: setOf(mapOf(groupByField to it)),
                        rightTables[it] ?: setOf(mapOf(groupByField to ""))
                    )
                        .sortedWith(compareBy { it.totalPoints })
        }.toMap()


    /**
     * Compare two tables (set of rows)
     * Row is set of column names & values Set<String, String>
     *
     */
    private fun compareGroupedTables(
        leftTable: Set<Map<String, String>>,
        rightTable: Set<Map<String, String>>
    ): Set<RowCompareResult> {

        val leftToRightResults = leftTable.mapNotNull { leftTableRow ->
            rightTable
                .map { rightTableRow ->
                    rightTableRow to compareTableRow(
                        leftTableRow,
                        rightTableRow,
                        true,
                        minimumComparableScore
                    )
                }
                .maxWith(compareBy { it.second })
        }
        val rightToLeftResults = (rightTable subtract leftToRightResults.map { it.first }).mapNotNull { rightTableRow ->
            leftTable
                .map { leftTableRow -> compareTableRow(leftTableRow, rightTableRow, false, minimumComparableScore) }
                .maxWith(compareBy { it })
        }

        return (leftToRightResults.map { it.second } + rightToLeftResults).toSet()
    }

    private fun compareTableRow(
        leftTableRow: Map<String, String>,
        rightTableRow: Map<String, String>,
        leftToRight: Boolean,
        minimumComparableScore: Double
    ) = RowCompareResult(
        ((leftTableRow.keys + rightTableRow.keys).map {
            it to compareCellValues(leftTableRow[it], rightTableRow[it], ignoredColumns.contains(it))
        }).toMap(), leftToRight, minimumComparableScore
    )

    private fun compareCellValues(
        leftValue: String?,
        rightValue: String?,
        ignoreCompareResult: Boolean
    ): CellCompareResult {
        val leftValueNotNull = leftValue ?: ""
        val rightValueNotNull = rightValue ?: ""

        val dmp = DiffMatchPatch()
        val diff = dmp.diffMain(leftValueNotNull, rightValueNotNull, false)
        val compareResult = when (ignoreCompareResult) {
            true -> 0.0 //To be ignored
            false -> (1.0 - (dmp.diffLevenshtein(diff).toFloat() / kotlin.math.max(
                leftValueNotNull.length,
                rightValueNotNull.length
            )))
        }
        return CellCompareResult(leftValue, rightValue, compareResult, diff, ignoreCompareResult)
    }
}