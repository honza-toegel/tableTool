package org.jto.tabletool

class VertexLabel(val label:String, alias:String) {
    companion object {
        fun toLabels(labels: String):List<VertexLabel> = labels.split("[\n,]".toRegex()).map {
            val labelMatch = requireNotNull("(\\w+)(:(\\w))?".toRegex().find(it)) {"Vertex label must be compliant with regexp (w+)(:(w+))"}
            VertexLabel(labelMatch.destructured.component1(), labelMatch.destructured.component3())
        }
    }
    val alias = alias.ifBlank { label }
    override fun toString(): String = "$label:$alias"
}

abstract class HeaderVertex (val colIndex:Int)

class MainHeaderVertex(colIndex:Int, labels:String) : HeaderVertex(colIndex) {
    val labels = VertexLabel.toLabels(labels)
    override fun toString() = "Main vertex label(s): $labels"
}

class RelatedHeaderVertex(colIndex:Int, labels:String, edgeLabel:String, val relationType:VertexRelationType): HeaderVertex(colIndex) {
    val labels = VertexLabel.toLabels(labels)
    val edgeLabel = edgeLabel.ifBlank { "has${this.labels.single().label.capitalize()}" }
    override fun toString() = "relationType $relationType edge: $edgeLabel vertex label(s): $labels"
}

class VertexSheetHeader (val mainVertexInfo: MainHeaderVertex, val relatedVertexHeaders: List<RelatedHeaderVertex>)
