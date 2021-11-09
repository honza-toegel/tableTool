package org.jto.tabletool

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.or
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.junit.Assert
import org.junit.Test

class MatchSameVertexTest {
    @Test
    fun matchOutEdgeLabel_differentVertex_expectedNoMatch() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        val vb = g.addV("b").next()
        g.addE("edgeName").from(va).to(vb).next()
        val notAvailableVertex = g.addV("notAvailable").next()
        fun naVertex(): GraphTraversal<Vertex, Vertex> = g.V(notAvailableVertex.id())

        val result = g.V().match<Vertex>(
            matchOutEdgeLabel("a", "edgeName", "b"),
            matchSameVertex("a", "b")
        ).toSet()

        Assert.assertEquals(emptySet<Map<String, Vertex>>(), result)
    }

    @Test
    fun matchOutEdgeLabel_sameVertex_ExpectedOneMatch() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        g.addE("edgeName").from(va).to(va).next()
        val notAvailableVertex = g.addV("notAvailable").next()
        fun naVertex(): GraphTraversal<Vertex, Vertex> = g.V(notAvailableVertex.id())

        val result = g.V().match<Vertex>(
            matchOutEdgeLabel("a", "edgeName", "b"),
            matchSameVertex("a", "b")
        ).toSet()

        val expected = setOf(mapOf<String, Vertex>("a" to va, "b" to va))

        Assert.assertEquals(expected, result)
    }

    @Test
    fun matchOutEdgeLabel_sameVertexOverOther_ExpectedTwoMatches() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        val vb = g.addV("b").next()
        g.addE("edgeName").from(va).to(vb).next()
        g.addE("edgeName").from(vb).to(va).next()
        val notAvailableVertex = g.addV("notAvailable").next()
        fun naVertex(): GraphTraversal<Vertex, Vertex> = g.V(notAvailableVertex.id())

        val result = g.V().match<Vertex>(
            matchOutEdgeLabel("a", "edgeName", "b"),
            matchOutEdgeLabel("b", "edgeName", "c"),
            matchSameVertex("a", "c")
        ).select<Vertex>("a", "c").toSet()

        val expected = setOf(
            mapOf<String, Vertex>("a" to va, "c" to va),
            mapOf<String, Vertex>("a" to vb, "c" to vb)
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun matchOutEdgeLabelOptionally_a2NotAvailable_ExpectedOneOptionalMatch() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        val notAvailableVertex = g.addV("notAvailable").next()
        g.addE("edgeName").from(va).to(notAvailableVertex).next()

        val result = g.V().match<Vertex>(
            matchOutEdgeLabel("a", "edgeName", "b"),
            matchNotAvailableVertex("b")
        ).toSet()

        val expected = setOf(mapOf<String, Vertex>("a" to va, "b" to notAvailableVertex))

        Assert.assertEquals(expected, result)
    }

    @Test
    fun matchOutEdgeLabelOptionally_a2NotAvailable_ExpectedNoOptionalMatchOnSource() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        val notAvailableVertex = g.addV("notAvailable").next()
        g.addE("edgeName").from(va).to(notAvailableVertex).next()

        val result = g.V().match<Vertex>(
            matchOutEdgeLabel("a", "edgeName", "b"),
            matchNotAvailableVertex("a")
        ).toSet()

        val expected = emptySet<Map<String, Vertex>>()

        Assert.assertEquals(expected, result)
    }

    @Test
    fun matchOutEdgeLabelExactlyOrOptionally_a2NotAvailable_ExpectedOneOptionalMatch() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        val notAvailableVertex = g.addV("notAvailable").next()

        g.addE("edgeName").from(va).to(notAvailableVertex).next()

        val result = g.V().match<Vertex>(
            matchOutEdgeLabel("a", "edgeName", "b"),
            or<Vertex>(
                matchSameVertex("a", "b"),
                matchNotAvailableVertex("b")
            )
        ).toSet()

        val expected = setOf(mapOf<String, Vertex>("a" to va, "b" to notAvailableVertex))

        Assert.assertEquals(expected, result)
    }

    @Test
    fun matchOutEdgeLabelExactlyOrOptionally_a2b2NotAvailable_ExpectedOneOptionalMatch() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        val vb = g.addV("b").next()
        g.addE("edgeName").from(va).to(vb).next()

        val notAvailableVertex = g.addV("notAvailable").next()
        g.addE("edgeName2").from(vb).to(notAvailableVertex).next()

        val result = g.V().match<Vertex>(
            matchOutEdgeLabel("a", "edgeName", "b"),
            matchOutEdgeLabel("b", "edgeName2", "c"),
            or<Vertex>(
                matchSameVertex("a", "c"),
                matchNotAvailableVertex("c")
            )
        ).toSet()

        val expected = setOf(mapOf<String, Vertex>("a" to va, "b" to vb, "c" to notAvailableVertex))

        Assert.assertEquals(expected, result)
    }
}