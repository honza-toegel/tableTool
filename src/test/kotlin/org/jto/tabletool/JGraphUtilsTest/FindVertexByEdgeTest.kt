package org.jto.tabletool.JGraphUtilsTest

import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jto.tabletool.graph.findVerticesByEdge
import org.junit.Assert
import org.junit.Test

class FindVertexByEdgeTest {
    @Test
    fun findVertexByEdge_when_hasEmptyGraph_then_expectEmptyResult() {
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        Assert.assertEquals(
            emptySet<Map<String, String>>(),
            graph.findVerticesByEdge("a", "b") { edge, source, target -> true })
    }

    @Test
    fun findVertexByEdge_when_hasNonEmptyGraphAndPredicateMatchNothing_then_expectEmptyResult() {
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        graph.addVertex("a")
        graph.addVertex("b")
        graph.addEdge("a", "b")
        Assert.assertEquals(
            emptySet<Map<String, String>>(),
            graph.findVerticesByEdge("a", "b") { edge, source, target -> false })
    }

    @Test
    fun findVertexByEdge_when_hasOneEdgeMatchingPredicate_then_expectOneResult() {
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        graph.addVertex("a")
        graph.addVertex("b")
        val expectedEdge = graph.addEdge("a", "b")
        Assert.assertEquals(
            setOf(mapOf("a" to "a", "b" to "b")),
            graph.findVerticesByEdge(
                "a",
                "b"
            ) { edge, source, target -> edge == expectedEdge && source == "a" && target == "b" })
    }

    @Test
    fun findVertexByEdge_when_hasMultipleEdgesAndOnlyOneEdgeMatchingPredicate_then_expectOneResult() {
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        graph.addVertex("a")
        graph.addVertex("b")
        graph.addVertex("c")
        val expectedEdge = graph.addEdge("a", "b")
        val notExpEdge1 = graph.addEdge("b", "a")
        val notExpEdge2 = graph.addEdge("a", "c")
        val notExpEdge3 = graph.addEdge("b", "c")
        val notExpEdge4 = graph.addEdge("a", "b")
        Assert.assertEquals(
            setOf(mapOf("a" to "a", "b" to "b")),
            graph.findVerticesByEdge(
                "a",
                "b"
            ) { edge, source, target -> edge == expectedEdge && source == "a" && target == "b" })
    }

    @Test
    fun findVertexByEdge_when_hasMultipleEdgesAndOnlyTwoEdgeMatchingPredicate_then_expectTwoResult() {
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        graph.addVertex("a")
        graph.addVertex("b")
        graph.addVertex("c")
        val expectedEdge1 = graph.addEdge("a", "c")
        val notExpEdge1 = graph.addEdge("b", "a")
        val notExpEdge2 = graph.addEdge("a", "c")
        val notExpEdge3 = graph.addEdge("b", "c")
        val expectedEdge2 = graph.addEdge("a", "b")
        Assert.assertEquals(
            setOf(mapOf("a" to "a", "b" to "b"), mapOf("a" to "a", "b" to "c")),
            graph.findVerticesByEdge(
                "a",
                "b"
            ) { edge, source, target -> ((edge == expectedEdge1) || (edge == expectedEdge2)) && source == "a" && (target == "b" || target == "c") })
    }

    @Test
    fun findVertexByEdge_when_hasMultipleEdgesAndOnlyTwoEdgeMatchingPredicate_then_expectTwoResult2() {
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        graph.addVertex("a")
        graph.addVertex("b")
        graph.addVertex("c")
        val expectedEdge1 = graph.addEdge("a", "b")
        val expectedEdge2 = graph.addEdge("a", "c")
        val notExpEdge1 = graph.addEdge("b", "a")
        val notExpEdge3 = graph.addEdge("b", "c")
        Assert.assertEquals(
            setOf(mapOf("a" to "a", "b" to "b"), mapOf("a" to "a", "b" to "c")),
            graph.findVerticesByEdge(
                "a",
                "b"
            ) { edge, source, target -> ((edge == expectedEdge1) || (edge == expectedEdge2)) && source == "a" && (target == "b" || target == "c") })
    }
}