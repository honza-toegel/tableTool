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
        g.findVerticesByEdgeLabel("mftType", "receiverComponent", "hasReceiverComponent")
    )

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
    /*
    val query = g.V().match<String>(
        As<String>("mftType").out("hasManTransfer").`as`("manTransfer"),
        //As<String>("mftType").out("restrictedDesan").`as`("restrictedDesan"),
        As<String>("manTransfer").out("hasSender").`as`("senderManEnv"),
        As<String>("manTransfer").out("hasReceiver").`as`("receiverManEnv"),
        As<String>("senderManEnv").out("hasMandator").`as`("senderMandator"),
        As<String>("senderManEnv").out("hasEnvironment").`as`("senderEnvironment"),
        As<String>("receiverManEnv").out("hasMandator").`as`("receiverMandator"),
        As<String>("receiverManEnv").out("hasEnvironment").`as`("receiverEnvironment"),
        As<String>("mftType").out("hasSenderComponent").`as`("senderComponent"),
        As<String>("mftType").out("hasReceiverComponent").`as`("receiverComponent")
    )
        //Receiver server group
        .match<String>(
            As<String>("receiverComponent").coalesce(out("deployedOn"), g.V(notAvailable.id()))
                .`as`("receiverServerGroup"),
            As<String>("receiverServerGroup").coalesce(out("restrictedManEnv"), g.V(notAvailable.id()))
                .or(where<String>(P.eq("receiverManEnv")), hasLabel<String>("notAvailable"))
        )
        .select<String>("receiverServerGroup").`as`("receiverServerGroup")

        //Receiver server
        .coalesce(As<String>("receiverServerGroup").out("hasServer"), g.V(notAvailable.id())).`as`("receiverServer")

        //Sender server group
        .match<String>(
            As<String>("senderComponent").coalesce(out("deployedOn"), g.V(notAvailable.id()))
                .`as`("senderServerGroup"),
            As<String>("senderServerGroup").coalesce(out("restrictedManEnv"), g.V(notAvailable.id()))
                .or(where<String>(P.eq("senderManEnv")), hasLabel<String>("notAvailable"))
        ).select<String>("senderServerGroup").`as`("senderServerGroup")

        //Sender server
        .coalesce(As<String>("senderServerGroup").out("hasServer"), g.V(notAvailable.id())).`as`("senderServer")

        //Select a postScript by priority, restrict by given restrictions
        .coalesce(
            //First priority is postscript related to mftType
            match<String, Vertex>(
                As<String>("mftType").`out`("hasPostScript").`as`("postScript"),
                As<String>("postScript").coalesce(out("restrictMandator"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverMandator")), hasLabel<String>("notAvailable")),
                As<String>("postScript").coalesce(out("restrictEnvironment"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverEnvironment")), hasLabel<String>("notAvailable")),
                As<String>("postScript").coalesce(out("restrictServerGroup"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverServerGroup")), hasLabel<String>("notAvailable"))
            ).select("postScript"),
            //Second priority is postscript related to receiverComponent which can be restricted to mandator/environment/serverGroup
            match<String, Vertex>(
                As<String>("receiverComponent").`out`("hasPostScript").`as`("postScript"),
                As<String>("postScript").coalesce(out("restrictMandator"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverMandator")), hasLabel<String>("notAvailable")),
                As<String>("postScript").coalesce(out("restrictEnvironment"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverEnvironment")), hasLabel<String>("notAvailable")),
                As<String>("postScript").coalesce(out("restrictServerGroup"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverServerGroup")), hasLabel<String>("notAvailable"))
            ).select("postScript"),
            //No postscript defined
            g.V(notAvailable.id())
        ).`as`("postScript")

        //Select a postScriptParams by priority, restrict by given restrictions
        .coalesce(
            //First priority is postScriptParams related to mftType
            match<String, Vertex>(
                As<String>("mftType").`out`("hasPostScriptParams").`as`("postScriptParams"),
                As<String>("postScriptParams").coalesce(out("restrictMandator"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverMandator")), hasLabel<String>("notAvailable")),
                As<String>("postScriptParams").coalesce(out("restrictEnvironment"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverEnvironment")), hasLabel<String>("notAvailable")),
                As<String>("postScriptParams").coalesce(out("restrictServerGroup"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverServerGroup")), hasLabel<String>("notAvailable"))
            ).select("postScriptParams"),
            //Second priority is postScriptParams related to receiverComponent which can be restricted to mandator/environment/serverGroup
            match<String, Vertex>(
                As<String>("receiverComponent").`out`("hasPostScriptParams").`as`("postScriptParams"),
                As<String>("postScriptParams").coalesce(out("restrictMandator"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverMandator")), hasLabel<String>("notAvailable")),
                As<String>("postScriptParams").coalesce(out("restrictEnvironment"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverEnvironment")), hasLabel<String>("notAvailable")),
                As<String>("postScriptParams").coalesce(out("restrictServerGroup"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverServerGroup")), hasLabel<String>("notAvailable"))
            ).select("postScriptParams"),
            //No postscript defined
            g.V(notAvailable.id())
        ).`as`("postScriptParams")

        //Select a receiverDirectory by priority, restrict by given restrictions
        .coalesce(
            //First priority is receiverDirectory related to mftType
            match<String, Vertex>(
                As<String>("mftType").`out`("hasReceiverDirectory").`as`("receiverDirectory"),
                As<String>("receiverDirectory").coalesce(out("restrictMandator"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverMandator")), hasLabel<String>("notAvailable")),
                As<String>("receiverDirectory").coalesce(out("restrictEnvironment"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverEnvironment")), hasLabel<String>("notAvailable")),
                As<String>("receiverDirectory").coalesce(out("restrictServerGroup"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverServerGroup")), hasLabel<String>("notAvailable"))
            ).select("receiverDirectory"),
            //Second priority is receiverDirectory related to receiverComponent which can be restricted to mandator/environment/serverGroup
            match<String, Vertex>(
                As<String>("receiverComponent").`out`("hasReceiverDirectory").`as`("receiverDirectory"),
                As<String>("receiverDirectory").coalesce(out("restrictMandator"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverMandator")), hasLabel<String>("notAvailable")),
                As<String>("receiverDirectory").coalesce(out("restrictEnvironment"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverEnvironment")), hasLabel<String>("notAvailable")),
                As<String>("receiverDirectory").coalesce(out("restrictServerGroup"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverServerGroup")), hasLabel<String>("notAvailable"))
            ).select("receiverDirectory"),
            //No postscript defined
            g.V(notAvailable.id())
        ).`as`("receiverDirectory")

        //Select a receiverUID by priority, restrict by given restrictions
        .coalesce(
            //receiverUID related to receiverComponent which can be restricted to mandator/environment/serverGroup
            match<String, Vertex>(
                As<String>("receiverComponent").`out`("hasUID").`as`("receiverUID"),
                As<String>("receiverUID").coalesce(out("restrictMandator"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverMandator")), hasLabel<String>("notAvailable")),
                As<String>("receiverUID").coalesce(out("restrictEnvironment"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverEnvironment")), hasLabel<String>("notAvailable")),
                As<String>("receiverUID").coalesce(out("restrictServerGroup"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverServerGroup")), hasLabel<String>("notAvailable"))
            ).select("receiverUID"),
            //No postscript defined
            g.V(notAvailable.id())
        ).`as`("receiverUID")

        //Select a senderUID by priority, restrict by given restrictions
        .coalesce(
            //senderUID related to receiverComponent which can be restricted to mandator/environment/serverGroup
            match<String, Vertex>(
                As<String>("senderComponent").`out`("hasUID").`as`("senderUID"),
                As<String>("senderUID").coalesce(out("restrictMandator"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverMandator")), hasLabel<String>("notAvailable")),
                As<String>("senderUID").coalesce(out("restrictEnvironment"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverEnvironment")), hasLabel<String>("notAvailable")),
                As<String>("senderUID").coalesce(out("restrictServerGroup"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("receiverServerGroup")), hasLabel<String>("notAvailable"))
            ).select("senderUID"),
            //No postscript defined
            g.V(notAvailable.id())
        ).`as`("senderUID")

        //Result columns
        .select<Vertex>(
            "mftType",
            "manTransfer",
            "senderComponent",
            "senderMandator",
            "senderEnvironment",
            "senderServerGroup",
            "senderServer",
            "senderUID",
            "receiverComponent",
            "receiverMandator",
            "receiverEnvironment",
            "receiverServerGroup",
            "receiverServer",
            "postScript",
            "postScriptParams",
            "receiverDirectory",
            "receiverUID"
        )

    val queryResult = query.toSet()

    logger.info("Query returned ${queryResult.size} results")

    queryResult.forEach {
        logger.debug(it.map { it.value.toStr() }.joinToString (" , "))
    }

    //Remove "N/A" cells
    return queryResult.map { row -> row.mapNotNull { cell -> when (cell.value.propertyOrEmpty("name") == "N/A") {
        true -> null
        false -> cell.key to toTableValue(cell.value)
    } }.toMap() }.toSet()
    */


}