package org.jto.tabletool

import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.*
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.slf4j.LoggerFactory

class GraphQueryTableLoader(val g: GraphTraversalSource) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    fun extractData(): Set<Map<String, String>> {

        logger.info("Execute graph query to extract data..")

        val notAvailable: Vertex = g.addV("notAvailable").property("name", "N/A").next()

        val query = g.V().match<String>(
            As<String>("mftType").out("hasManTransfer").`as`("manTransfer"),
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

            //Sender server group
            .match<String>(
                As<String>("senderComponent").coalesce(out("deployedOn"), g.V(notAvailable.id()))
                    .`as`("senderServerGroup"),
                As<String>("senderServerGroup").coalesce(out("restrictedManEnv"), g.V(notAvailable.id()))
                    .or(where<String>(P.eq("senderManEnv")), hasLabel<String>("notAvailable"))
            ).select<String>("senderServerGroup").`as`("senderServerGroup")

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

            //Select a postScriptParams by priority, restrict by given restrictions
            .coalesce(
                //First priority is postScriptParams related to mftType
                match<String, Vertex>(
                    As<String>("mftType").`out`("hasReceiverDirectory").`as`("receiverDirectory"),
                    As<String>("receiverDirectory").coalesce(out("restrictMandator"), g.V(notAvailable.id()))
                        .or(where<String>(P.eq("receiverMandator")), hasLabel<String>("notAvailable")),
                    As<String>("receiverDirectory").coalesce(out("restrictEnvironment"), g.V(notAvailable.id()))
                        .or(where<String>(P.eq("receiverEnvironment")), hasLabel<String>("notAvailable")),
                    As<String>("receiverDirectory").coalesce(out("restrictServerGroup"), g.V(notAvailable.id()))
                        .or(where<String>(P.eq("receiverServerGroup")), hasLabel<String>("notAvailable"))
                ).select("receiverDirectory"),
                //Second priority is postScriptParams related to receiverComponent which can be restricted to mandator/environment/serverGroup
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

            //Result columns
            .select<String>(
                "mftType",
                "manTransfer",
                "senderComponent",
                "senderMandator",
                "senderEnvironment",
                "senderServerGroup",
                "receiverComponent",
                "receiverMandator",
                "receiverEnvironment",
                "receiverServerGroup",
                "postScript",
                "postScriptParams",
                "receiverDirectory"
            ).by("name")

        val queryResult = query.toSet()

        logger.info("Query returned ${queryResult.size} results")

        queryResult.forEach {
            logger.debug(it.toString())
            //it.forEach{ print((it.value as Vertex).value<String>("name")) }
            //println((it["mftType"] as Vertex).value<String>("name"))
        }

        /*
        val query1 = g.V().match<String>(
            As<String>("mftType").out("hasManTransfer").`as`("manTransfer")).select<String>("mftType", "manTransfer").by("name")
        val queryResult1 = query1.toSet()
        logger.info("Query1 returned ${queryResult1.size} results")
        queryResult1.forEach {
            logger.debug(it.toString())
        }*/

        return queryResult

    }

}