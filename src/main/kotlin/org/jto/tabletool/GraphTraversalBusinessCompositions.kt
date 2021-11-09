package org.jto.tabletool

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.or

fun <S, E, E2> GraphTraversal<S, E>.matchServerGroup(
    prefix: String
): GraphTraversal<S, E2> {
    return select<E2>("${prefix}Component")
        .match<E2>(
            matchOutEdgeLabel("${prefix}Component", "deployedOn", "${prefix}ServerGroup"),
            matchOutEdgeLabel(
                "${prefix}ServerGroup",
                "restrictedServesManEnv",
                "${prefix}RestrictedServesManEnv"
            )
        ).match<E2>(
            or<Vertex> (
                matchNotAvailableVertex("${prefix}RestrictedServesManEnv"),
                matchSameVertex("${prefix}RestrictedServesManEnv", "${prefix}ManEnvAppl")
            )
        ).selectAs("${prefix}ServerGroup")
}