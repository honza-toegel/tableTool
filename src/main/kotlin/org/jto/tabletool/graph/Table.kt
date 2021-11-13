package org.jto.tabletool.graph

import java.lang.IllegalArgumentException

data class Table<V>(val header: Set<String>, val data: Set<Map<String, V>>) {

    fun filterByOneColumn(
        columnName: String,
        predicate: (value: V?) -> Boolean
    ): Table<V> = Table(
        header, data.filter { row ->
            predicate(row[columnName])
        }.toSet()
    )

    fun filterByTwoColumns(
        columnName1: String,
        columnName2: String,
        predicate: (value1: V?, value2: V?) -> Boolean
    ): Table<V> = Table(
        header, data.filter { row ->
            predicate(row[columnName1], row[columnName2])
        }.toSet()
    )

    /**
     * Find single common label from both tables and join them via this label
     */
    fun join(
        rightTable: Table<V>,
        joinType: JoinType = JoinType.Inner,
        ignoreRightOnMultipleJoinColumns: Boolean = false
    ): Table<V> {
        if (header.isEmpty() || rightTable.header.isEmpty())
            throw IllegalArgumentException("There must be at leas one column in both tables in order find common join column")
        fun getSingleCommonLabel(): String {
            val commonLabels = header.intersect(rightTable.header)
            if (commonLabels.size == 1)
                return commonLabels.first()
            else if (commonLabels.isEmpty())
                throw IllegalArgumentException(String.format("Can't find any join column between %s and %s", header, rightTable.header))
            else
                throw IllegalArgumentException(String.format("Multiple possible join columns %s, please specify one explicitly as first parameter", commonLabels))
        }

        val label = getSingleCommonLabel()
        return join(label, rightTable, joinType, ignoreRightOnMultipleJoinColumns)
    }

    fun join(
        label: String,
        rightTable: Table<V>,
        joinType: JoinType = JoinType.Inner,
        ignoreRightOnMultipleJoinColumns: Boolean = false
    ): Table<V> =
        join(label, label, label, rightTable, joinType, ignoreRightOnMultipleJoinColumns)

    fun join(
        leftLabel: String,
        rightLabel: String,
        resultLabel: String,
        rightTable: Table<V>,
        joinType: JoinType = JoinType.Inner,
        ignoreRightOnMultipleJoinColumns: Boolean = false
    ): Table<V> {
        val leftMatchedRows = mutableSetOf<Map<String, V>>()
        val rightMatchedRows = mutableSetOf<Map<String, V>>()
        val innerJoinResult = data.flatMap<Map<String, V>, Map<String, V>> { leftRow ->
            if (!leftRow.containsKey(leftLabel)) emptySet()
            else {
                val rightRows = rightTable.data.filter { rightRow -> rightRow[rightLabel] == leftRow[leftLabel] }
                val matchedValue = leftRow[leftLabel]!!
                rightRows.mapNotNull { rightRow ->
                    val commonLabels = leftRow.keys.intersect(rightRow.keys)
                    //There must be always only one column used for join
                    if ((leftLabel == rightLabel && commonLabels.size == 1) || (leftLabel != rightLabel && commonLabels.isEmpty())) {
                        leftMatchedRows += leftRow
                        rightMatchedRows += rightRow
                        mapOf(resultLabel to matchedValue) + leftRow.filter { it.key != leftLabel } + rightRow.filter { it.key != rightLabel }
                    } else {
                        if (!ignoreRightOnMultipleJoinColumns)
                            throw IllegalArgumentException(
                                String.format(
                                    "Table is inconsistent, there is %d of same labels in left row %s and right row %s",
                                    commonLabels.size,
                                    leftRow.toString(),
                                    rightRows.toString()
                                )
                            )
                        else
                            null
                    }
                }
            }
        }.toSet()
        val resultHeader = header + rightTable.header - leftLabel - rightLabel + resultLabel
        val resultTableData = when (joinType) {
            JoinType.Inner -> innerJoinResult
            JoinType.LeftOuter -> innerJoinResult + (this.data - leftMatchedRows)
            JoinType.RightOuter -> innerJoinResult + (rightTable.data - rightMatchedRows)
            JoinType.FullOuter -> innerJoinResult + (this.data - leftMatchedRows) + (rightTable.data - rightMatchedRows)
        }
        return Table(resultHeader, resultTableData)
    }
}

fun<V> emptyTable(): Table<V> = Table(emptySet(), emptySet())
fun<V> emptyTable(header: Set<String>): Table<V> = Table(header, emptySet())

fun Table<Vertex>.filterNonEqualNames(
    columnName1: String,
    columnName2: String,
    ignoreNotAvailableValues: Boolean = true
): Table<Vertex> = Table(
    header, data.filter { row ->
        val val1 = row[columnName1]
        val val2 = row[columnName2]
        if (ignoreNotAvailableValues)
            val1 == null || val2 == null || val1.name == val2.name
        else
            val1 != null && val2 != null && val1.name == val2.name
    }.toSet()
)

fun Table<Vertex>.filterByLabel(
    columnName1: String,
    columnLabel1: String = columnName1,
    ignoreNotAvailableValues: Boolean = true
): Table<Vertex> = Table(
    header, data.filter { row ->
        val val1 = row[columnName1]
        if (ignoreNotAvailableValues)
            val1 == null || (val1.label == columnLabel1)
        else
            val1 != null && (val1.label == columnLabel1)
    }.toSet()
)

fun Table<Vertex>.filterByTwoLabels(
    columnName1: String,
    columnName2: String,
    columnLabel1: String = columnName1,
    columnLabel2: String = columnName2,
    ignoreNotAvailableValues: Boolean = true
): Table<Vertex> =
    filterByLabel(columnName1, columnLabel1, ignoreNotAvailableValues).filterByLabel(
        columnName2,
        columnLabel2,
        ignoreNotAvailableValues
    )

enum class JoinType {
    Inner,
    LeftOuter,
    RightOuter,
    FullOuter,
}


fun <V> joinAll(vararg tables: Table<V>): Table<V> {
    return tables.reduce { acc, set ->
        acc.join(set)
    }
}

fun <V> joinAll(joinType: JoinType, vararg tables: Table<V>): Table<V> {
    return tables.reduce { acc, set ->
        acc.join(set, joinType)
    }
}