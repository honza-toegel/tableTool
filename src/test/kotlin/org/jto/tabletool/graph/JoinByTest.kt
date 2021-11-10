package org.jto.tabletool.graph

import org.junit.Assert
import org.junit.Test

class JoinByTest {

    @Test
    fun when_joinTwoEmptyTables_thenExpect_emptyResult() {
        val table1 = emptySet<Map<String, String>>()
        val table2 = emptySet<Map<String, String>>()
        Assert.assertEquals(emptySet<Map<String, String>>(), table1.join("a", "b", "c", table2))
    }

    @Test
    fun when_joinTwoEmptyNonEmptyTablesWithOneIntersection_thenExpect_oneResult() {
        val table1 = setOf(mapOf("a" to "a1"))
        val table2 = setOf(mapOf("b" to "a1"))
        Assert.assertEquals(setOf(mapOf("c" to "a1")), table1.join("a", "b", "c", table2))
    }

    @Test
    fun when_usingSameColumnNameAndOneMatch_thenExpect_oneResult() {
        val table1 = setOf(mapOf("a" to "a1"))
        val table2 = setOf(mapOf("a" to "a1"))
        Assert.assertEquals(setOf(mapOf("a" to "a1")), table1.join(table2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun when_usingSameColumnNameAndOneMatchButMoreSameColumns_thenExpect_exception() {
        val table1 = setOf(mapOf("a" to "a1", "b" to "c2"))
        val table2 = setOf(mapOf("a" to "a1", "b" to "c3"))
        table1.join(table2) //Should throw exception because there are two same columns (a, b)
    }

    @Test
    fun when_OneMatchAndMoreDifferentColumns_thenExpect_oneResult() {
        val table1 = setOf(mapOf("a" to "a1", "b" to "c2"))
        val table2 = setOf(mapOf("a" to "a1", "c" to "c3"))
        Assert.assertEquals(setOf(mapOf("a" to "a1", "b" to "c2", "c" to "c3")), table1.join(table2))
    }

    @Test
    fun when_OneMatchButMoreDifferentColumnsAndRows_thenExpect_oneResult() {
        val table1 = setOf(mapOf("a" to "a1", "b" to "c2"), mapOf("a" to "a2", "b" to "c2"))
        val table2 = setOf(mapOf("a" to "a1", "c" to "c3"), mapOf("a" to "a3", "c" to "c3"))
        Assert.assertEquals(setOf(mapOf("a" to "a1", "b" to "c2", "c" to "c3")), table1.join(table2))
    }

    @Test
    fun when_2MatchAndMoreDifferentColumnsAndRows_thenExpect_twoResults() {
        val table1 = setOf(mapOf("a" to "a1", "b" to "c2"), mapOf("a" to "a2", "b" to "c2"))
        val table2 = setOf(mapOf("a" to "a1", "c" to "c3"), mapOf("a" to "a2", "c" to "c4"))
        Assert.assertEquals(
            setOf(
                mapOf("a" to "a1", "b" to "c2", "c" to "c3"),
                mapOf("a" to "a2", "b" to "c2", "c" to "c4")
            ), table1.join(table2)
        )
    }

    @Test
    fun when_2MatchAndMoreDifferentColumnsAndRows_thenExpect_twoResults2() {
        val table1 = setOf(
            mapOf("a" to "a1", "b" to "c2")
        )
        val table2 = setOf(
            mapOf("a" to "a1", "c" to "c3"),
            mapOf("a" to "a1", "c" to "c4")
        )
        Assert.assertEquals(
            setOf(
                mapOf("a" to "a1", "b" to "c2", "c" to "c3"),
                mapOf("a" to "a1", "b" to "c2", "c" to "c4")
            ), table1.join(table2)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun when_moreSameColumns_ax_bbbb_JoinedByLabel_thenExpect_exception() {
        val table1 = setOf(mapOf("ax" to "a1", "b" to "c2"), mapOf("ax" to "a2", "bbbb" to "c2"))
        val table2 = setOf(mapOf("ax" to "a1", "c" to "c3"), mapOf("ax" to "a2", "bbbb" to "c4"))
        table1.join("ax", "ax", "c", table2)
    }
}