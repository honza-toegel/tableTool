package org.jto.tabletool

import org.apache.tinkerpop.gremlin.process.traversal.P
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.*
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.junit.Test

class MatchServerGroupTest {
    @Test
    fun testMatchServerGroup() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val notAvailable: Vertex = g.addV("notAvailable").property("name", "N/A").next()

        val manEnv001P0: Vertex = g.addV("manEnvAppl").property("name", "001/P0").next()
        val manEnv019P7: Vertex = g.addV("manEnvAppl").property("name", "019/P7").next()
        val manEnv890P8111: Vertex = g.addV("manEnvAppl").property("name", "890/P8").property("desan", "111").next()
        val manEnv891P7019: Vertex = g.addV("manEnvAppl").property("name", "891/P7").property("desan", "019").next()

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

        g.addE("deployedOn").from(swComponentGNZ).to(notAvailable).iterate()
        g.addE("deployedOn").from(swComponentPTK).to(notAvailable).iterate()
        g.addE("restrictedServesManEnv").from(notAvailable).to(notAvailable).iterate()


        g.V().match<String>(
            As<String>("mftType").out("hasManTransfer").`as`("manTransfer"),
            As<String>("manTransfer").out("sender").`as`("senderManEnvAppl"),
            As<String>("manTransfer").out("receiver").`as`("receiverManEnvAppl"),
            As<String>("mftType").out("senderComponent").`as`("senderComponent"),
            As<String>("mftType").out("receiverComponent").`as`("receiverComponent")
        )
            .matchServerGroup<Vertex, Map<String, String>, String>("receiver")
            //.matchServerGroup<Vertex, Map<String, String>, String>("sender")
            //.matchServerGroup<Vertex, String, String>("sender")

            .select<String>(
                "mftType",
                "manTransfer",
                "senderComponent",
                "receiverManEnvAppl",
                //"senderMandator",
                //"senderEnvironment",
                //"senderServerGroup",
                //"senderRestrictedServesManEnv",
                "receiverManEnvAppl",
                "receiverComponent"
                //"receiverMandator",
                //"receiverEnvironment"
                //"receiverServerGroup",
                //"receiverRestrictedServesManEnv"
            ).by("name").toSet()

            .forEach {
                println(it)
            }

        //TODO: IllegalStateException, final state from tinkerpop => usually caused by missing next() or iterate()

    }
}
