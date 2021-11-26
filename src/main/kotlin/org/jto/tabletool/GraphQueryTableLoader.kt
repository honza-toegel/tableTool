package org.jto.tabletool

import org.jgrapht.Graph
import org.jto.tabletool.graph.*
import org.slf4j.LoggerFactory

class GraphQueryTableLoader(val g: Graph<Vertex, Edge>) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    private fun getBaseTable() = joinAll(
        g.findVerticesByEdgeLabel("mftType", "manTransfer", "hasManTransfer"),
        g.findVerticesByEdgeLabel("manTransfer", "senderManEnv", "hasSender"),
        g.findVerticesByEdgeLabel("manTransfer", "receiverManEnv", "hasReceiver"),
        g.findVerticesByEdgeLabel("senderManEnv", "senderMandator", "hasMandator"),
        g.findVerticesByEdgeLabel("senderManEnv", "senderEnvironment", "hasEnvironment"),
        g.findVerticesByEdgeLabel("receiverManEnv", "receiverMandator", "hasMandator"),
        g.findVerticesByEdgeLabel("receiverManEnv", "receiverEnvironment", "hasEnvironment"),
        g.findVerticesByEdgeLabel("mftType", "senderComponent", "hasSenderComponent"),
        g.findVerticesByEdgeLabel("mftType", "receiverComponent", "hasReceiverComponent"),
        g.findVerticesByEdgeLabel("mftType", "mftService", "hasMftService"),
        g.findVerticesByEdgeLabel("mftType", "transferType", "hasTransferType")
    ).join(g.findVerticesByEdgeLabel("mftType", "comment", "hasComment"), JoinType.LeftOuter)

    private fun getServers(prefix: String) = joinAll(
        g.findVerticesByEdgeLabel("${prefix}Component", "${prefix}ServerGroup", "deployedOn"),
        g.findVerticesByEdgeLabel("${prefix}ServerGroup", "${prefix}RestrictedManEnv", "restrictedManEnv"),
        g.findVerticesByEdgeLabel("${prefix}ServerGroup", "${prefix}Server", "hasServer")
    )

    private fun Table<Vertex>.filterOutByRestrictedManEnv(prefix: String): Table<Vertex> =
        filterNonEqualNames(
            "${prefix}ManEnv",
            "${prefix}RestrictedManEnv"
        )

    /**
     * @param sourceLabel can be 'mftType' or 'receiverComponent'
     * @param sourceVertexLabel in case of sourceLabel='receiverComponent' is 'swComponent'
     */
    private fun getPostscript(sourceLabel: String, sourceVertexLabel: String = sourceLabel) = joinAll(
        JoinType.LeftOuter,
        g.findVerticesByEdgeLabel(sourceLabel, "postScript", "hasPostScript")
            .filterByLabel(sourceLabel, sourceVertexLabel),
        g.findVerticesByEdgeLabel("postScript", "postScriptRestrictReceiverMandator", "restrictMandator")
            .filterByLabel("postScript"),
        g.findVerticesByEdgeLabel("postScript", "postScriptRestrictReceiverEnvironment", "restrictEnvironment")
            .filterByLabel("postScript"),
        g.findVerticesByEdgeLabel("postScript", "postScriptRestrictReceiverServerGroup", "restrictServerGroup")
            .filterByLabel("postScript")
    )

    /**
     * @param sourceLabel can be 'mftType' or 'receiverComponent'
     * @param sourceVertexLabel in case of sourceLabel='receiverComponent' is 'swComponent'
     */
    private fun getPostscriptParams(sourceLabel: String, sourceVertexLabel: String = sourceLabel) = joinAll(
        JoinType.LeftOuter,
        g.findVerticesByEdgeLabel(sourceLabel, "postScriptParams", "hasPostScriptParams")
            .filterByLabel(sourceLabel, sourceVertexLabel),
        g.findVerticesByEdgeLabel("postScriptParams", "postScriptParamsRestrictReceiverMandator", "restrictMandator")
            .filterByLabel("postScriptParams"),
        g.findVerticesByEdgeLabel("postScriptParams", "postScriptParamsRestrictReceiverEnvironment", "restrictEnvironment")
            .filterByLabel("postScriptParams"),
        g.findVerticesByEdgeLabel("postScriptParams", "postScriptParamsRestrictReceiverServerGroup", "restrictServerGroup")
            .filterByLabel("postScriptParams")
    )

    /**
     * @param sourceLabel can be 'mftType' or 'receiverComponent'
     * @param sourceVertexLabel in case of sourceLabel='receiverComponent' is 'swComponent'
     */
    private fun getReceiverDirectory(sourceLabel: String, sourceVertexLabel: String = sourceLabel) = joinAll(
        JoinType.LeftOuter,
        g.findVerticesByEdgeLabel(sourceLabel, "receiverDirectory", "hasReceiverDirectory")
            .filterByLabel(sourceLabel, sourceVertexLabel),
        g.findVerticesByEdgeLabel("receiverDirectory", "receiverDirectoryRestrictReceiverMandator", "restrictMandator")
            .filterByLabel("receiverDirectory"),
        g.findVerticesByEdgeLabel("receiverDirectory", "receiverDirectoryRestrictReceiverEnvironment", "restrictEnvironment")
            .filterByLabel("receiverDirectory"),
        g.findVerticesByEdgeLabel("receiverDirectory", "receiverDirectoryRestrictReceiverServerGroup", "restrictServerGroup")
            .filterByLabel("receiverDirectory")
    )

    private fun Table<Vertex>.filterOutByManAndEnvAndServerGroup(prefix: String): Table<Vertex> =
        filterNonEqualNames(
            "receiverMandator",
            "${prefix}RestrictReceiverMandator"
        ).filterNonEqualNames(
            "receiverEnvironment",
            "${prefix}RestrictReceiverEnvironment"
        ).filterNonEqualNames(
            "receiverServerGroup",
            "${prefix}RestrictReceiverServerGroup"
        )

    /**
     * @param uidPrefix 'sender' or 'receiver'
     */
    private fun getUID(uidPrefix: String) = joinAll(
        JoinType.LeftOuter,
        g.findVerticesByEdgeLabel("${uidPrefix}Component", "${uidPrefix}UID", "hasUID")
            .filterByLabel("${uidPrefix}Component", "swComponent"),
        g.findVerticesByEdgeLabel("${uidPrefix}UID", "${uidPrefix}UIDRestrictReceiverMandator", "restrictMandator")
            .filterByLabel("${uidPrefix}UID", "UID"),
        g.findVerticesByEdgeLabel("${uidPrefix}UID", "${uidPrefix}UIDRestrictReceiverEnvironment", "restrictEnvironment")
            .filterByLabel("${uidPrefix}UID", "UID"),
        g.findVerticesByEdgeLabel("${uidPrefix}UID", "${uidPrefix}UIDRestrictReceiverServerGroup", "restrictServerGroup")
            .filterByLabel("${uidPrefix}UID", "UID")
    )

    fun extractData(): Set<Map<String, TableValue>> {

        logger.info("Execute graph query to extract data..")

        val result = getBaseTable()
            .join(getServers("receiver"), JoinType.LeftOuter).filterOutByRestrictedManEnv("receiver")
            .join(getServers("sender"), JoinType.LeftOuter).filterOutByRestrictedManEnv("sender")
            //Join postscript on mftType as PRIO-1
            .join("mftType", getPostscript("mftType"), JoinType.LeftOuter)
            //Join postscript on mftType as PRIO-2
            .join(
                "receiverComponent",
                getPostscript("receiverComponent", "swComponent"),
                JoinType.LeftOuter,
                ignoreRightOnMultipleJoinColumns = true
            )
            .filterOutByManAndEnvAndServerGroup("postScript")
            //Join postscript params on mftType as PRIO-1
            .join("mftType", getPostscriptParams("mftType"), JoinType.LeftOuter)
            //Join postscript params on mftType as PRIO-2
            .join(
                "receiverComponent",
                getPostscriptParams("receiverComponent", "swComponent"),
                JoinType.LeftOuter,
                ignoreRightOnMultipleJoinColumns = true
            )
            .filterOutByManAndEnvAndServerGroup("postScriptParams")
            //Join receiver directory on mftType as PRIO-1
            .join("mftType", getReceiverDirectory("mftType"), JoinType.LeftOuter)
            //Join receiver directory on mftType as PRIO-2
            .join(
                "receiverComponent",
                getReceiverDirectory("receiverComponent", "swComponent"),
                JoinType.LeftOuter,
                ignoreRightOnMultipleJoinColumns = true
            )
            .filterOutByManAndEnvAndServerGroup("receiverDirectory")
            //Add receiver UID
            .join("receiverComponent", getUID("receiver"), JoinType.LeftOuter)
            .filterOutByManAndEnvAndServerGroup("receiverUID")
            //Add sender UID
            .join("senderComponent", getUID("sender"), JoinType.LeftOuter)
            .filterOutByManAndEnvAndServerGroup("senderUID")

        return result.data.map { row ->
            row.map { valEntry ->
                valEntry.key to valEntry.value.toTableValue()
            }.toMap()
        }.toSet()
    }

    private fun Vertex.toTableValue(): TableValue =
        when (properties.contains("defaultValue")) {
            false -> StringTableValue(name)
            true -> RegExpTableValue(properties["defaultValue"]!!, name.toRegex())
        }
}