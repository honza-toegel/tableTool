package org.jto.tabletool.graph

import org.jgrapht.Graph

class EdgeContainer<E, V>(val edge: E, val source: V, val target: V)

fun Graph<Vertex, Edge>.findVerticesByEdgeLabel(
    edgeLabel: String
): Set<Map<String, Vertex>> = findVerticesByEdge{ edge, _, _ -> edge.label == edgeLabel }

fun Graph<Vertex, Edge>.findVerticesByEdgeLabel(
    sourceLabel: String,
    targetLabel: String,
    edgeLabel: String
): Set<Map<String, Vertex>> = findVerticesByEdge(sourceLabel, targetLabel) { edge, _, _ -> edge.label == edgeLabel }

fun <E, V> Graph<V, E>.findVerticesByEdge(
    sourceLabel: String,
    targetLabel: String,
    predicate: (edge: E) -> Boolean
): Set<Map<String, V>> = findVerticesByEdge(sourceLabel, targetLabel) { edge, _, _ -> predicate(edge) }

private fun <E, V> Graph<V, E>.filterVerticesByEdgePredicate(
    predicate: (edge: E, source: V, target: V) -> Boolean
): List<EdgeContainer<E, V>> {
    return edgeSet().map { edge ->
        EdgeContainer(edge, getEdgeSource(edge), getEdgeTarget(edge))
    }.filter { edgeContainer ->
        predicate(edgeContainer.edge, edgeContainer.source, edgeContainer.target)
    }
}

private fun <E, V> List<EdgeContainer<E, V>>.createTableFromVerticesContainerList(
    sourceLabel: String,
    targetLabel: String
) = map { edgeContainer ->
    mapOf(sourceLabel to edgeContainer.source, targetLabel to edgeContainer.target)
}

private fun List<EdgeContainer<Edge, Vertex>>.createTableFromVerticesContainerList(): List<Map<String, Vertex>> =
    map { edgeContainer ->
        mapOf(
            edgeContainer.source.label to edgeContainer.source,
            edgeContainer.target.label to edgeContainer.target
        )
    }

fun <E, V> Graph<V, E>.findVerticesByEdge(
    sourceLabel: String,
    targetLabel: String,
    predicate: (edge: E, source: V, target: V) -> Boolean
): Set<Map<String, V>> {
    return filterVerticesByEdgePredicate(predicate)
        .createTableFromVerticesContainerList(sourceLabel, targetLabel)
        .toSet()
}

fun Graph<Vertex, Edge>.findVerticesByEdge(
    predicate: (edge: Edge, source: Vertex, target: Vertex) -> Boolean
): Set<Map<String, Vertex>> {
    return filterVerticesByEdgePredicate(predicate)
        .createTableFromVerticesContainerList()
        .toSet()
}




