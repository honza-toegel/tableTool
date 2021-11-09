package org.jto.tabletool

import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.process.traversal.Traversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.*
import org.apache.tinkerpop.gremlin.structure.Vertex


fun <S, E, E2> GraphTraversal<S, E>.selectAs(
    selectKey: String,
    asLabel: String = selectKey
): GraphTraversal<S, E2> {
    return this.select<E2>(selectKey).`as`(asLabel)
}

/**
 * Matching traversals from any source vertex which has out edge with specific label: outEdgeLabel to destination vertex
 */
fun matchOutEdgeLabel(
    sourceAlias: String,
    outEdgeLabel: String,
    destinationAlias: String
): GraphTraversal<String, Vertex> =
    As<String>(sourceAlias).out(outEdgeLabel).`as`(destinationAlias)


/**
 * Match all traversals where source vertex is same as destination vertex
 */
fun matchSameVertex(
    sourceAlias: String,
    destinationAlias: String
): GraphTraversal<String, String> = As<String>(sourceAlias).where(P.eq<String>(destinationAlias))

/*fun matchSameVertexOptionaly(
    sourceAlias: String,
    destinationAlias: String,
    notAvailableLabel: String = "notAvailable"
): GraphTraversal<String, String> = As<String>(destinationAlias).where(P.eq<String>(sourceAlias).or(P.eq()))*/

/**
 * Match all traversals where source vertex is same as destination vertex      (source -> destination)
 * adding all traversals where source vertex is not same as destination vertex (source -> na)
 */
fun matchNotAvailableVertex(
    sourceAlias: String,
    notAvailableLabel: String = "notAvailable"
): GraphTraversal<String, String> =
    As<String>(sourceAlias).hasLabel(notAvailableLabel)

