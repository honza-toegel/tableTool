package org.jto.tabletool.graph

import org.jgrapht.Graph

fun <E, V> Graph<V, E>.findVerticesByEdge(
    sourceLabel: String,
    targetLabel: String,
    predicate: (edge: E) -> Boolean
): Set<Map<String, V>> = findVerticesByEdge(sourceLabel, targetLabel) { edge, _, _ -> predicate(edge) }

fun <E, V> Graph<V, E>.findVerticesByEdge(
    sourceLabel: String,
    targetLabel: String,
    predicate: (edge: E, source: V, target: V) -> Boolean
): Set<Map<String, V>> {
    return edgeSet().filter { edge ->
        predicate(edge, getEdgeSource(edge), getEdgeTarget(edge))
    }.map { edge ->
        mapOf(sourceLabel to getEdgeSource(edge), targetLabel to getEdgeTarget(edge))
    }.toSet()
}


