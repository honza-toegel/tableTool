package org.jto.tabletool.graph

class Vertex(val label: String, val name: String, val properties: Map<String, String> = emptyMap()) {
    override fun toString(): String = name
}