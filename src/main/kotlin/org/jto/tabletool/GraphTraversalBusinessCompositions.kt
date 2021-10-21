package org.jto.tabletool

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex

fun <S, E, E2> GraphTraversal<S, E>.matchServerGroup(
    prefix: String,
    naVertex: () -> GraphTraversal<Vertex, Vertex>
): GraphTraversal<S, E2> {
    return select<E2>("${prefix}Component")
        .match<E2>(
            matchOutEdgeLabelOptionally("${prefix}Component", "deployedOn", "${prefix}ServerGroup", naVertex),
            matchOutEdgeLabelOptionally(
                "${prefix}ServerGroup",
                "restrictedServesManEnv",
                "${prefix}RestrictedServesManEnv",
                naVertex
            ),
            matchSameVertexOptionally("${prefix}RestrictedServesManEnv", "${prefix}ManEnvAppl")
        ).selectAs("${prefix}ServerGroup")
}