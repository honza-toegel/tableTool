package org.jto.tabletool.JGraphUtilsTest

import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals
import org.jto.tabletool.graph.filterByTwoColumns;

class FilterByTwoColumns {
    @Test
    fun when_filterCriteriaMatch3Results_then_match3Results2() {
        val table1 = setOf(
            mapOf("a" to "a1", "colX" to "c3", "colX_reducedTo" to "c3"),
            mapOf("a" to "a1", "colX" to "c2", "colX_reducedTo" to "c3"),
            mapOf("a" to "a2", "colX" to "c1", "colX_reducedTo" to "c0"),
            mapOf("a" to "a3", "colX" to "c0", "colX_reducedTo" to "c0"),
            mapOf("a" to "a4", "colX" to "c8")
        )
        val filteredTable1 = table1.filterByTwoColumns("colX", "colX_reducedTo") {
            colX, reducedTo -> colX == reducedTo || reducedTo == null
        }
        val expectedResult = setOf(
            mapOf("a" to "a1", "colX" to "c3", "colX_reducedTo" to "c3"),
            mapOf("a" to "a3", "colX" to "c0", "colX_reducedTo" to "c0"),
            mapOf("a" to "a4", "colX" to "c8")
        )
        assertEquals(expectedResult, filteredTable1)
    }
}


