package org.jto.tabletool

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultDirectedGraph
import org.junit.Test

class JGraphPlay {

    @Test
    fun createGraphTest() {
        val g: Graph<Int, StringEdge> = DefaultDirectedGraph(StringEdge::class.java)
        g.addVertex(1)
        g.addVertex(2)
        g.addEdge(1,2, StringEdge("test"))
        g.addVertex(4)
        g.addEdge(1,4, StringEdge("te2"))
        g.addVertex(3)
        g.addEdge(3,4, StringEdge("test"))

        val res = g.edgeSet().filter { edge -> edge.label == "test" }.map { it.toString() }

        println(res)
    }

    @Test
    fun createGraphTest2() {
        val g: Graph<Int, StringEdge> = DefaultDirectedGraph(StringEdge::class.java)
        g.addVertex(1)
        g.addVertex(2)
        g.addEdge(1,2, StringEdge("test"))
        g.addVertex(4)
        g.addEdge(1,4, StringEdge("te2"))
        g.addVertex(3)
        g.addEdge(3,4, StringEdge("test"))

        val res = g.edgeSet().filter { edge -> edge.label == "test" }.map { "${it.getSourceVertex()}->${it.getTargetVertex()}" }

        println(res)
    }
}