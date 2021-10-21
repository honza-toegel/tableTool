package org.jto.tabletool

import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.*
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
class TestGraphCreation {
    fun testGraphCreation() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

    val notAvailable: Vertex = g.addV("notAvailable").property("name", "N/A").next()

        val mandator001: Vertex = g.addV("mandator").property("name", "001").next()
        val mandator019: Vertex = g.addV("mandator").property("name", "019").next()
        val mandator891: Vertex = g.addV("mandator").property("name", "891").next()
        val mandator899: Vertex = g.addV("mandator").property("name", "898").next()
        val mandator890: Vertex = g.addV("mandator").property("name", "890").next()
        val mandator895: Vertex = g.addV("mandator").property("name", "895").next()

        val envP0: Vertex = g.addV("environment").property("name", "P0").next()
        val envR0: Vertex = g.addV("environment").property("name", "R0").next()
        val envP7: Vertex = g.addV("environment").property("name", "P7").next()
        val envR7: Vertex = g.addV("environment").property("name", "R7").next()

        val manEnv001P0: Vertex = g.addV("manEnvAppl").property("name", "001/P0").next()
        val manEnv019P7: Vertex = g.addV("manEnvAppl").property("name", "019/P7").next()
        val manEnv890P8111: Vertex = g.addV("manEnvAppl").property("name", "890/P8").property("desan", "111").next()
        val manEnv891P7019: Vertex = g.addV("manEnvAppl").property("name", "891/P7").property("desan", "019").next()

        g.addE("mandator").from(manEnv001P0).to(mandator001).iterate()
        g.addE("environment").from(manEnv001P0).to(envP0).iterate()

        g.addE("mandator").from(manEnv019P7).to(mandator019).iterate()
        g.addE("environment").from(manEnv019P7).to(envP7).iterate()

        g.addE("mandator").from(manEnv891P7019).to(mandator891).iterate()
        g.addE("environment").from(manEnv891P7019).to(envP7).iterate()

        val mandatorTransfer001P0_001P0: Vertex = g.addV("manTransfer").property("name", "001/P0->001/P0").next()
        val mandatorTransfer001P0_019P7: Vertex = g.addV("manTransfer").property("name", "001/P0->019/P7").next()

        g.addE("sender").from(mandatorTransfer001P0_001P0).to(manEnv001P0).iterate()
        g.addE("receiver").from(mandatorTransfer001P0_001P0).to(manEnv001P0).iterate()

        g.addE("sender").from(mandatorTransfer001P0_019P7).to(manEnv001P0).iterate()
        g.addE("receiver").from(mandatorTransfer001P0_019P7).to(manEnv019P7).iterate()

        val mftTypeL3YPTK01: Vertex = g.addV("mftType").property("name", "L3YPTK01").next()
        val swComponentL3Y: Vertex = g.addV("swComponent").property("name", "L3Y").next()
        val swComponentPTK: Vertex = g.addV("swComponent").property("name", "PTK").next()

        val mftTypeGNZL3X01: Vertex = g.addV("mftType").property("name", "GNZL3X01").next()
        val swComponentGNZ: Vertex = g.addV("swComponent").property("name", "GNZ").next()


        g.addE("senderComponent").from(mftTypeL3YPTK01).to(swComponentL3Y).iterate()
        g.addE("receiverComponent").from(mftTypeL3YPTK01).to(swComponentPTK).iterate()
        g.addE("hasManTransfer").from(mftTypeL3YPTK01).to(mandatorTransfer001P0_019P7).iterate()
        g.addE("hasManTransfer").from(mftTypeL3YPTK01).to(mandatorTransfer001P0_001P0).iterate()

        g.addE("senderComponent").from(mftTypeGNZL3X01).to(swComponentGNZ).iterate()
        g.addE("receiverComponent").from(mftTypeGNZL3X01).to(swComponentL3Y).iterate()
        g.addE("hasManTransfer").from(mftTypeGNZL3X01).to(mandatorTransfer001P0_001P0).iterate()

        //fun<S> traversalTest():GraphTraversal<S,Vertex> = hasLabel<S>("mftType").out("senderComponent")
        fun <S> GraphTraversal<S, Vertex>.traversalTest1(): GraphTraversal<S, Vertex> =
            this.hasLabel("mftType").out("senderComponent")
        println(g.V().hasLabel("mftType").out("senderComponent").valueMap<String>("name").toSet())
        //println(g.V().flatMap { traversalTest<String>() }.valueMap<String>("name").toSet())
        println(g.V().traversalTest1().valueMap<String>("name").toSet())

        val servers_ch_L3Y =
            g.addV("server").property("name", "l3y-l3x-tcu-ch-001-890.p0.0.com,l3y-l3x-tcu-ch-001-890.p0.1.com").next()
        val servers_emac_L3Y =
            g.addV("server").property("name", "l3y-l3x-tcu-ch-898-891.p2.0.com,l3y-l3x-tcu-ch-898-891.p2.1.com").next()

        g.addE("deployedOn").from(swComponentL3Y).to(servers_ch_L3Y).iterate()
        g.addE("deployedOn").from(swComponentL3Y).to(servers_emac_L3Y).iterate()
        g.addE("restrictedServesManEnv").from(servers_ch_L3Y).to(manEnv001P0).iterate()
        g.addE("restrictedServesManEnv").from(servers_ch_L3Y).to(manEnv890P8111).iterate()
        g.addE("restrictedServesManEnv").from(servers_emac_L3Y).to(manEnv891P7019).iterate()
        g.addE("restrictedServesManEnv").from(servers_emac_L3Y).to(manEnv019P7).iterate()

        val postscriptL3X: Vertex =
            g.addV("postScript").property("name", "mv var/tmp && op asipost.sh; mv var/tmp/11 && op asipost22.sh")
                .next()
        val postscriptParams_CH_nodesan: Vertex = g.addV("postScript").property("name", "$1 $2 $3 \$desan").next()
        val postscriptParams_OTHR_desan: Vertex =
            g.addV("postScript").property("name", "$1 $2 $3 \$desan $1031; $1 $2 $3 $1031").next()
        val receiverDirectoryL3X: Vertex = g.addV("receiverDirectory").property("name", "/var/tmp/$1003_$1005").next()

        g.addE("hasPostScript").from(swComponentL3Y).to(postscriptL3X).iterate()
        g.addE("restrictMandator").from(postscriptL3X).to(mandator001).to(mandator890).iterate()
        g.addE("hasParams").from(postscriptL3X).to(postscriptParams_CH_nodesan).to(postscriptParams_OTHR_desan)
            .iterate()
        g.addE("hasReceiverDirectory").from(postscriptL3X).to(receiverDirectoryL3X).iterate()


        //The result value (reference for Traversal<V,V>) can't be given twice to one same traversal
        // -> therefore must be handed over with function reference, which is called on each place where this is needed
        fun naVertex(): GraphTraversal<Vertex, Vertex> = g.V(notAvailable.id())


        g.V().match<String>(
            As<String>("mftType").out("hasManTransfer").`as`("manTransfer"),
            As<String>("manTransfer").out("sender").`as`("senderManEnvAppl"),
            As<String>("manTransfer").out("receiver").`as`("receiverManEnvAppl"),
            As<String>("senderManEnvAppl").out("mandator").`as`("senderMandator"),
            As<String>("senderManEnvAppl").out("environment").`as`("senderEnvironment"),
            As<String>("receiverManEnvAppl").out("mandator").`as`("receiverMandator"),
            As<String>("receiverManEnvAppl").out("environment").`as`("receiverEnvironment"),
            As<String>("mftType").out("senderComponent").`as`("senderComponent"),
            As<String>("mftType").out("receiverComponent").`as`("receiverComponent")
        )
            .matchServerGroup<Vertex, Map<String, String>, String>("receiver") {naVertex()}
            .matchServerGroup<Vertex, String, String>("sender") {naVertex()}


            //Select a postScript by priority, restrict by given restrictions
            .select<String>("receiverComponent").coalesce(
                //First priority is postscript related to mftType
                select<String, Vertex>("mftType").out("hasPostScript"),
                //Second priority is postscript related to receiverComponent which can be restricted to mandator/environment/serverGroup
                match<String, Vertex>(
                    As<String>("receiverComponent").`out`("hasPostScript").`as`("postScript"),
                    As<String>("postScript").coalesce(out("restrictMandator"), g.V(notAvailable.id()))
                        .`as`("restrictedMandator"),
                    As<String>("restrictedMandator").or(
                        where<String>(P.eq("receiverMandator")),
                        hasLabel<String>("notAvailable")
                    ),
                    As<String>("postScript").coalesce(out("restrictEnvironment"), g.V(notAvailable.id()))
                        .`as`("restrictedEnvironment"),
                    As<String>("restrictedEnvironment").or(
                        where<String>(P.eq("receiverEnvironment")),
                        hasLabel<String>("notAvailable")
                    ),
                    As<String>("postScript").coalesce(out("restrictServerGroup"), g.V(notAvailable.id()))
                        .`as`("restrictedServerGroup"),
                    As<String>("restrictedServerGroup").or(
                        where<String>(P.eq("receiverServerGroup")),
                        hasLabel<String>("notAvailable")
                    )
                ).select("postScript"),
                //No postscript defined
                g.V(notAvailable.id())
            ).`as`("receiverPostScript")
            .select<String>(
                "mftType",
                "manTransfer",
                "senderComponent",
                "senderMandator",
                "senderEnvironment",
                "senderServerGroup",
                "senderRestrictedServesManEnv",
                "receiverComponent",
                "receiverMandator",
                "receiverEnvironment",
                "receiverServerGroup",
                "receiverRestrictedServesManEnv",
                "receiverPostScript"
            ).by("name").toSet()

            .forEach {
                println(it)
                //it.forEach{ print((it.value as Vertex).value<String>("name")) }
                //println((it["mftType"] as Vertex).value<String>("name"))
            }

        //TODO: IllegalStateException, final state from tinkerpop => usually caused by missing next() or iterate()

    }
}
