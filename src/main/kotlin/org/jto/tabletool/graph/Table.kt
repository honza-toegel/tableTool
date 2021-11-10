package org.jto.tabletool.graph

import java.lang.IllegalArgumentException

fun <V> Set<Map<String, V>>.filterByOneColumn(
    columnName: String,
    predicate: (value: V?) -> Boolean
): Set<Map<String, V>> =
    filter { row ->
        predicate(row[columnName])
    }.toSet()

fun <V> Set<Map<String, V>>.filterByTwoColumns(
    columnName1: String,
    columnName2: String,
    predicate: (value1: V?, value2: V?) -> Boolean
): Set<Map<String, V>> =
    filter { row ->
        predicate(row[columnName1], row[columnName2])
    }.toSet()

/**
 * Find single common label from both tables and join them via this label
 */
fun <V> Set<Map<String, V>>.join(rightTable: Set<Map<String, V>>): Set<Map<String, V>> {
    if (isEmpty() || rightTable.isEmpty())
        return emptySet()
    fun getSingleCommonLabel(): String {
        val commonLabels = first().keys.intersect(rightTable.first().keys)
        if (commonLabels.size == 1)
            return commonLabels.first()
        else if (commonLabels.isEmpty())
            throw IllegalArgumentException("Can't find any join column")
        else
            throw IllegalArgumentException("Multiple join columns, its required only one")
    }

    val label = getSingleCommonLabel()
    return join(label, label, label, rightTable)
}

fun <V> Set<Map<String, V>>.join(
    leftLabel: String,
    rightLabel: String,
    resultLabel: String,
    rightTable: Set<Map<String, V>>
): Set<Map<String, V>> {
    return flatMap<Map<String, V>, Map<String, V>> { leftRow ->
        if (!leftRow.containsKey(leftLabel)) emptySet()
        else {
            val rightRows = rightTable.filter { rightRow -> rightRow[rightLabel] == leftRow[leftLabel] }
            val matchedValue = leftRow[leftLabel]!!
            rightRows.map { rightRow ->
                val commonLabels = leftRow.keys.intersect(rightRow.keys)
                //There must be always only one column used for join
                if ((leftLabel == rightLabel && commonLabels.size == 1) || (leftLabel != rightLabel && commonLabels.isEmpty()))
                    mapOf(resultLabel to matchedValue) + leftRow.filter { it.key != leftLabel } + rightRow.filter { it.key != rightLabel }
                else
                    throw IllegalArgumentException(
                        String.format(
                            "Table is inconsistent, there is %d of same labels in left row %s and right row %s",
                            commonLabels.size,
                            leftRow.toString(),
                            rightRows.toString()
                        )
                    )
            }
        }
    }.toSet()
}