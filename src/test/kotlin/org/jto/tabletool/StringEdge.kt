package org.jto.tabletool

import org.jgrapht.graph.DefaultEdge

class StringEdge(val label:String) : DefaultEdge() {
    fun getSourceVertex():Int = source as Int
    fun getTargetVertex():Int = target as Int
}