package org.jto.tabletool

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch

class CellCompareResult(
    val leftValue: TableValue?,
    val rightValue: TableValue?,
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

    /**
     * Used for sort by criteria
     */
    fun toSortByString(): String = when (display) {
        CellCompareDisplay.NoValue -> ""
        CellCompareDisplay.Ignored -> leftValue.toString()
        CellCompareDisplay.RightOnly -> rightValue.toString()
        CellCompareDisplay.LeftOnly -> leftValue.toString()
        CellCompareDisplay.Comparable -> when (compareResult == 1.0) {
            true -> leftValue.toString()
            false -> "$leftValue$rightValue"
        }
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
    LeftRightCompared, //The two records matches with enough totalScore, will be displayed as diff of each other
    LeftOnly,          //The two records not matches, left record should be displayed
    RightOnly          //The two records not matches, left record should be displayed
}

class RowCompareResult(
    val cells: Map<String, CellCompareResult>, leftToRight: Boolean, minimumComparableScore: Double
) : Comparable<RowCompareResult> {
    val totalPoints: Double =
        cells.values.filter { it.display == CellCompareDisplay.Comparable }.sumByDouble { it.compareResult }
    val totalScore: Double = totalPoints / cells.values.filter { it.display == CellCompareDisplay.Comparable }.size

    /**
     * Indicate how should be the result of row compare displayed
     */
    val display: RowCompareDisplay = when (totalScore >= minimumComparableScore) {
        true -> RowCompareDisplay.LeftRightCompared
        else -> when (leftToRight) {
            true -> RowCompareDisplay.LeftOnly
            false -> RowCompareDisplay.RightOnly
        }
    }

    override fun compareTo(other: RowCompareResult): Int = totalPoints.compareTo(other.totalPoints)
    override fun toString(): String =
        "totalPoints: $totalPoints, totalScore: $totalScore, Display: $display"
}

class TableComparator(
    leftTable: Set<Map<String, TableValue>>,
    rightTable: Set<Map<String, TableValue>>,
    val ignoredColumns: Set<String>,
    val groupByField: String = "mftType",
    val minimumComparableScore: Double = 0.8
) {

    private val leftTables: Map<String, Set<Map<String, TableValue>>> =
        leftTable.groupBy { it[groupByField].toString() }.map { it.key to it.value.toSet() }.toMap()
    private val rightTables: Map<String, Set<Map<String, TableValue>>> =
        rightTable.groupBy { it[groupByField].toString() }.map { it.key to it.value.toSet() }.toMap()

    fun compareTables(): Map<String, List<RowCompareResult>> =
        (leftTables.keys + rightTables.keys).map {
            it to
                    compareGroupedTables(
                        leftTables[it] ?: setOf(mapOf(groupByField to StringTableValue(it))),
                        rightTables[it] ?: setOf(mapOf(groupByField to StringTableValue(it)))
                    )
                        .sortedWith(compareBy { it.totalPoints })
        }.toMap()


    /**
     * Compare two tables (set of rows)
     * Row is set of column names & values Set<String, String>
     *
     */
    private fun compareGroupedTables(
        leftTable: Set<Map<String, TableValue>>,
        rightTable: Set<Map<String, TableValue>>
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
                }.maxWith(compareBy { it.second.totalPoints })
        }
        val rightToLeftResults = (rightTable subtract leftToRightResults.map { it.first }).mapNotNull { rightTableRow ->
            leftTable
                .map { leftTableRow -> compareTableRow(leftTableRow, rightTableRow, false, minimumComparableScore) }
                .maxWith(compareBy { it.totalPoints })
        }

        return (leftToRightResults.map { it.second } + rightToLeftResults).toSet()
    }

    private fun compareTableRow(
        leftTableRow: Map<String, TableValue>,
        rightTableRow: Map<String, TableValue>,
        leftToRight: Boolean,
        minimumComparableScore: Double
    ) = RowCompareResult(
        ((leftTableRow.keys + rightTableRow.keys).map {
            it to compareCellValues(leftTableRow[it], rightTableRow[it], ignoredColumns.contains(it))
        }).toMap(), leftToRight, minimumComparableScore
    )

    private fun compareCellValues(
        leftValue: TableValue?,
        rightValue: TableValue?,
        ignoreCompareResult: Boolean
    ): CellCompareResult {
        val leftValueNotNull:TableValue = leftValue ?: StringTableValue.Empty
        val rightValueNotNull:TableValue = rightValue ?: StringTableValue.Empty

        val diff = leftValueNotNull.diff(rightValueNotNull)
        val maxLength = kotlin.math.max(
            leftValueNotNull.toString().length,
            rightValueNotNull.toString().length
        )
        val compareResult = when (ignoreCompareResult) {
            true -> 0.0 //To be ignoredCompare
            false -> when (maxLength > 0) {
                true -> 1.0 - (TableValue.dmp.diffLevenshtein(diff).toFloat() / maxLength)
                false -> 1.0
            }
        }
        return CellCompareResult(leftValue, rightValue, compareResult, diff, ignoreCompareResult)
    }
}