package org.jto.tabletool

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.junit.Assert
import org.junit.Test

class MatchOutEdgeLabelTest {

    @Test
    fun matchOutEdgeLabel_OnePair_noMatch() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        val vb = g.addV("b").next()
        g.addE("edgeName").from(va).to(vb).next()
        val notAvailableVertex = g.addV("notAvailable").next()
        fun naVertex(): GraphTraversal<Vertex, Vertex> = g.V(notAvailableVertex.id())

        val result = g.V().match<Vertex>(
            matchOutEdgeLabel("a", "edgeNameWrong", "b")
        ).toSet()

        Assert.assertEquals(emptySet<Map<String, Vertex>>(), result)
    }

    @Test
    fun matchOutEdgeLabel_OnePair_match() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        val vb = g.addV("b").next()
        g.addE("edgeName").from(va).to(vb).next()
        val notAvailableVertex = g.addV("notAvailable").next()
        fun naVertex(): GraphTraversal<Vertex, Vertex> = g.V(notAvailableVertex.id())

        val result = g.V().match<Vertex>(
            matchOutEdgeLabel("a", "edgeName", "b")
        ).toSet()

        val expected = setOf(mapOf<String, Vertex>("a" to va, "b" to vb))

        Assert.assertEquals(expected, result)
    }

    @Test
    fun matchOutEdgeLabel_TwoPairs_match() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        val vb = g.addV("b").next()
        g.addE("edgeName").from(va).to(vb).next()

        val vc = g.addV("a").next()
        val vd = g.addV("b").next()
        g.addE("edgeName").from(vc).to(vd).next()

        val notAvailableVertex = g.addV("notAvailable").next()
        fun naVertex(): GraphTraversal<Vertex, Vertex> = g.V(notAvailableVertex.id())

        val result = g.V().match<Vertex>(
            matchOutEdgeLabel("a", "edgeName", "b")
        ).toSet()

        val expected = setOf(
            mapOf<String, Vertex>("a" to va, "b" to vb),
            mapOf<String, Vertex>("a" to vc, "b" to vd)
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun matchOutEdgeLabel_TwoPairs_withNA() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        val vb = g.addV("b").next()
        g.addE("edgeName").from(va).to(vb).next()
        val notAvailableVertex = g.addV("notAvailable").next()
        fun naVertex(): GraphTraversal<Vertex, Vertex> = g.V(notAvailableVertex.id())

        val result = g.V().match<String>(
            matchOutEdgeLabelOptionally("a", "edgeName", "b") {naVertex()}
        ).toSet()

        val expected = setOf(
            //From any vertex out via "edgeName" to something
            mapOf<String, Vertex>("a" to va, "b" to vb),

            //From any vertex to notAvailable
            mapOf<String, Vertex>("a" to vb, "b" to notAvailableVertex)
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun matchOutEdgeLabel_TwoPairsAndOneSingle_withNA() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val va = g.addV("a").next()
        val vb = g.addV("b").next()
        g.addE("edgeName").from(va).to(vb).next()
        val vc = g.addV("c").next()
        val notAvailableVertex = g.addV("notAvailable").next()

        fun naVertex(): GraphTraversal<Vertex, Vertex> = g.V(notAvailableVertex.id())

        val result = g.V().match<String>(
            matchOutEdgeLabelOptionally("a", "edgeName", "b") {naVertex()}
        ).toSet()

        val expected = setOf(
            //From any vertex out via "edgeName" to something
            mapOf<String, Vertex>("a" to va, "b" to vb),

            //From any vertex to notAvailable
            mapOf<String, Vertex>("a" to vb, "b" to notAvailableVertex),
            mapOf<String, Vertex>("a" to vc, "b" to notAvailableVertex)
        )

        Assert.assertEquals(expected, result)
    }
}