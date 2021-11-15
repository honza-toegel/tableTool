package org.jto.tabletool.graph

import org.junit.Assert
import org.junit.Test

class JoinByTest {

    @Test
    fun when_joinTwoEmptyTables_thenExpect_emptyResult() {
        val table1 = emptyTable<String>()
        val table2 = emptyTable<String>()
        Assert.assertEquals(emptyTable<String>(setOf("c")), table1.join("a", "b", "c", table2))
    }

    @Test
    fun when_joinTwoEmptyNonEmptyTablesWithOneIntersection_thenExpect_oneResult() {
        val table1 = Table(setOf("a"), setOf(mapOf("a" to "a1")))
        val table2 = Table(setOf("b"), setOf(mapOf("b" to "a1")))
        Assert.assertEquals(Table(setOf("c"), setOf(mapOf("c" to "a1"))), table1.join("a", "b", "c", table2))
    }

    @Test
    fun when_usingSameColumnNameAndOneMatch_thenExpect_oneResult() {
        val table1 = Table(setOf("a"), setOf(mapOf("a" to "a1")))
        val table2 = Table(setOf("a"), setOf(mapOf("a" to "a1")))
        Assert.assertEquals(Table(setOf("a"), setOf(mapOf("a" to "a1"))), table1.join(table2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun when_usingSameColumnNameAndOneMatchButMoreSameColumns_thenExpect_exception() {
        val table1 = Table(setOf("a", "b"), setOf(mapOf("a" to "a1", "b" to "c2")))
        val table2 = Table(setOf("a", "b"), setOf(mapOf("a" to "a1", "b" to "c3")))
        table1.join(table2) //Should throw exception because there are two same columns (a, b)
    }

    @Test
    fun when_OneMatchAndMoreDifferentColumns_thenExpect_oneResult() {
        val table1 = Table(setOf("a", "b"), setOf(mapOf("a" to "a1", "b" to "c2")))
        val table2 = Table(setOf("a", "c"), setOf(mapOf("a" to "a1", "c" to "c3")))
        Assert.assertEquals(
            Table(setOf("a", "b", "c"), setOf(mapOf("a" to "a1", "b" to "c2", "c" to "c3"))),
            table1.join(table2)
        )
    }

    @Test
    fun when_OneMatchButMoreDifferentColumnsAndRows_thenExpect_oneResult() {
        val table1 = Table(setOf("a", "b"), setOf(mapOf("a" to "a1", "b" to "c2"), mapOf("a" to "a2", "b" to "c2")))
        val table2 = Table(setOf("a", "c"), setOf(mapOf("a" to "a1", "c" to "c3"), mapOf("a" to "a3", "c" to "c3")))
        Assert.assertEquals(
            Table(setOf("a", "b", "c"), setOf(mapOf("a" to "a1", "b" to "c2", "c" to "c3"))),
            table1.join(table2)
        )
    }

    @Test
    fun when_2MatchAndMoreDifferentColumnsAndRows_thenExpect_twoResults() {
        val table1 = Table(setOf("a", "b"), setOf(mapOf("a" to "a1", "b" to "c2"), mapOf("a" to "a2", "b" to "c2")))
        val table2 = Table(setOf("a", "c"), setOf(mapOf("a" to "a1", "c" to "c3"), mapOf("a" to "a2", "c" to "c4")))
        Assert.assertEquals(
            Table(
                setOf("a", "b", "c"), setOf(
                    mapOf("a" to "a1", "b" to "c2", "c" to "c3"),
                    mapOf("a" to "a2", "b" to "c2", "c" to "c4")
                )
            ), table1.join(table2)
        )
    }

    @Test
    fun when_2MatchAndMoreDifferentColumnsAndRows_thenExpect_twoResults2() {
        val table1 = Table(
            setOf("a", "b"), setOf(
                mapOf("a" to "a1", "b" to "c2")
            )
        )
        val table2 = Table(
            setOf("a", "c"), setOf(
                mapOf("a" to "a1", "c" to "c3"),
                mapOf("a" to "a1", "c" to "c4")
            )
        )
        Assert.assertEquals(
            Table(
                setOf("a", "b", "c"), setOf(
                    mapOf("a" to "a1", "b" to "c2", "c" to "c3"),
                    mapOf("a" to "a1", "b" to "c2", "c" to "c4")
                )
            ), table1.join(table2)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun when_moreSameColumns_ax_bbbb_JoinedByLabel_thenExpect_exception() {
        val table1 = Table(
            setOf("ax", "b", "bbbb"),
            setOf(mapOf("ax" to "a1", "b" to "c2"), mapOf("ax" to "a2", "bbbb" to "c2"))
        )
        val table2 = Table(
            setOf("ax", "c", "bbbb"),
            setOf(mapOf("ax" to "a1", "c" to "c3"), mapOf("ax" to "a2", "bbbb" to "c4"))
        )
        table1.join("ax", "ax", "c", table2)
    }

    @Test
    fun when_2MatchAnd1LeftOuterJoin_thenExpect_threeResults() {
        val table1 = Table(
            setOf("a", "b"), setOf(
                mapOf("a" to "a1", "b" to "c2"),
                mapOf("a" to "aX", "b" to "**")
            )
        )
        val table2 = Table(
            setOf("a", "b", "c"), setOf(
                mapOf("a" to "a1", "c" to "c3"),
                mapOf("a" to "a1", "c" to "c4"),
                mapOf("a" to "aZ", "b" to "@@")
            )
        )
        Assert.assertEquals(
            Table(
                setOf("a", "b", "c"), setOf(
                    mapOf("a" to "a1", "b" to "c2", "c" to "c3"),
                    mapOf("a" to "a1", "b" to "c2", "c" to "c4"),
                    mapOf("a" to "aX", "b" to "**")
                )
            ), table1.join("a", table2, JoinType.LeftOuter)
        )
    }

    @Test
    fun when_2MatchAnd1RightOuterJoin_thenExpect_threeResults() {
        val table1 = Table(
            setOf("a", "b"), setOf(
                mapOf("a" to "a1", "b" to "c2"),
                mapOf("a" to "aX", "b" to "**")
            )
        )
        val table2 = Table(
            setOf("a", "b", "c"), setOf(
                mapOf("a" to "a1", "c" to "c3"),
                mapOf("a" to "a1", "c" to "c4"),
                mapOf("a" to "aZ", "b" to "@@")
            )
        )
        Assert.assertEquals(
            Table(
                setOf("a", "b", "c"), setOf(
                    mapOf("a" to "a1", "b" to "c2", "c" to "c3"),
                    mapOf("a" to "a1", "b" to "c2", "c" to "c4"),
                    mapOf("a" to "aZ", "b" to "@@")
                )
            ), table1.join("a", table2, JoinType.RightOuter)
        )
    }

    fun when_2MatchAnd2FullOuterJoin_thenExpect_fourResults() {
        val table1 = Table(
            setOf("a", "b"), setOf(
                mapOf("a" to "a1", "b" to "c2"),
                mapOf("a" to "aX", "b" to "**")
            )
        )
        val table2 = Table(
            setOf("a", "b", "c"), setOf(
                mapOf("a" to "a1", "c" to "c3"),
                mapOf("a" to "a1", "c" to "c4"),
                mapOf("a" to "aZ", "b" to "@@")
            )
        )
        Assert.assertEquals(
            Table(
                setOf("a", "b", "c"), setOf(
                    mapOf("a" to "a1", "b" to "c2", "c" to "c3"),
                    mapOf("a" to "a1", "b" to "c2", "c" to "c4"),
                    mapOf("a" to "aX", "b" to "**"),
                    mapOf("a" to "aZ", "b" to "@@")
                )
            ), table1.join(table2, JoinType.FullOuter)
        )
    }

    @Test
    fun when_2matchesButOneWitColumnClashToBeIgnored_thenExpect_oneResults() {
        val table1 = Table(
            setOf("a", "b"), setOf(
                mapOf("a" to "a1", "b" to "c2"),
                mapOf("a" to "aX", "b" to "**")
            )
        )
        val table2 = Table(
            setOf("a", "b", "c"), setOf(
                mapOf("a" to "a1", "c" to "c3"),
                mapOf("a" to "a1", "b" to "c4")
            )
        )
        Assert.assertEquals(
            Table(
                setOf("a", "b", "c"), setOf(
                    mapOf("a" to "a1", "b" to "c2", "c" to "c3")
                )
            ), table1.join("a", table2, ignoreRightOnMultipleJoinColumns = true)
        )
    }

    @Test
    fun when_leftOuterJoin2RowsWithEmptySet_thenExpect_twoResults() {
        val table1 = Table(
            setOf("a", "b"), setOf(
                mapOf("a" to "a1", "b" to "c2"),
                mapOf("a" to "aX", "b" to "**")
            )
        )
        val table2 = emptyTable<String>()
        Assert.assertEquals(
            table1, table1.join("a", table2, JoinType.LeftOuter)
        )
    }

    @Test
    fun when_rightOuterJoin2RowsWithEmptySet_thenExpect_twoResults() {
        val table1 = Table(
            setOf("a", "b"), setOf(
                mapOf("a" to "a1", "b" to "c2"),
                mapOf("a" to "aX", "b" to "**")
            )
        )
        val table2 = emptyTable<String>()
        Assert.assertEquals(
            emptyTable<String>(setOf("a", "b")), table1.join("a", table2, JoinType.RightOuter)
        )
    }

    @Test
    fun when_rightOuterJoinEmptySetWith2Rows_thenExpect_twoResults() {
        val table1 = Table(
            setOf("a", "b"), setOf(
                mapOf("a" to "a1", "b" to "c2"),
                mapOf("a" to "aX", "b" to "**")
            )
        )
        val table2 = emptyTable<String>()
        Assert.assertEquals(
            table1, table2.join("a", table1, JoinType.RightOuter)
        )
    }

    @Test
    fun when_leftOuterJoinEmptySetWith2Rows_thenExpect_twoResults() {
        val table1 = Table(
            setOf("a", "b"), setOf(
                mapOf("a" to "a1", "b" to "c2"),
                mapOf("a" to "aX", "b" to "**")
            )
        )
        val table2 = emptyTable<String>()
        Assert.assertEquals(
            emptyTable<String>(setOf("a", "b")), table2.join("a", table1, JoinType.LeftOuter)
        )
    }
}