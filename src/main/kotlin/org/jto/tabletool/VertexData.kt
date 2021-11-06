package org.jto.tabletoolimport com.beust.klaxon.Klaxonimport org.slf4j.LoggerFactorydata class VertexData(val label: String = "", val name: String, val attributes: Map<String, String> = emptyMap()) {    companion object {        /**         * Parse the related vertex data from string         * @param JSONString can be one of following:         *  JSON array of bellow object         *  JSON key : value map         */        fun parseFromJSONString(JSONString: String): List<VertexData> {            val adjustedJSONString = when (JSONString.trim().startsWith('{') && JSONString.trim().endsWith('}')) {                true -> "[$JSONString]"                false -> JSONString.trim()            }            return when (adjustedJSONString.isEmpty()) {                true -> emptyList()                false -> {                    val resultList = Klaxon().parseArray<Map<String, String>>(adjustedJSONString) ?: emptyList()                    return resultList.map {                        VertexData(                            label = it.getOrDefault("label", ""),                            name = requireNotNull(it["name"]) { "vertex name must be given" },                            attributes = it.minus(setOf("name", "label"))                        )                    }                }            }        }        fun parseFromSimpleString(text: String): List<VertexData> {            return when (text.isEmpty()) {                true -> emptyList()                false -> {                    if (text.trim().startsWith("{") || text.trim().startsWith("["))                        logger.warn("The input text '$text' seems to be JSON, but is processed as simple string, please change text font color to 0070C0 in order to process the input as JSON")                    val vertexInfos = text.split("(?<!\\\\)[;\\n]".toRegex())                        .map { it.replace("\\;", ";") }                    return vertexInfos.map { vertexLabelName ->                        val vertexNameMatch = requireNotNull("((\\w+):)?(.*)$".toRegex().find(vertexLabelName))                        { "Vertex name: '$vertexLabelName' doesn't adhere regexp '((\\w+):)?.*\\$'" }                        VertexData(                            label = vertexNameMatch.destructured.component2(), //Optional label                            name = vertexNameMatch.destructured.component3().replace("\\:", ":")                        )                    }                }            }        }        @Suppress("JAVA_CLASS_ON_COMPANION")        @JvmStatic        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)    }}