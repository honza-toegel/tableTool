package org.jto.tabletool

import org.jgrapht.Graph
import org.jgrapht.graph.DirectedPseudograph
import org.jto.tabletool.graph.Edge
import org.jto.tabletool.graph.Vertex
import org.jto.tabletool.graph.findVerticesByEdge
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class ExcelGraphLoaderTest {
    @Test
    fun testExcelGraphLoader() {

        val g: Graph<Vertex, Edge> = DirectedPseudograph(Edge::class.java)

        val testFile = ExcelGraphLoaderTest::class.java.getResource("/test.xlsx").file
        ExcelGraphLoader(testFile, g).loadGraph()

        val allPersonResult = g.vertexSet().filter { it.label == "person" }.toSet()
        allPersonResult.forEach {
            println(it.toString())
        }

        assertEquals(6, allPersonResult.size)


        val hasFriend = g.findVerticesByEdge("personA", "personAFriend") { edge -> edge.label == "hasFriend" }
        val marriedWith =
            g.findVerticesByEdge("personAFriend", "personAFriendMarriedWith") { edge -> edge.label == "marriedWith" }

        val hasFriendWhoIsMarriedWith = hasFriend.join(marriedWith).data.map{ r -> r.map { v -> v.key to v.value.name }.toMap() }.toSet()

        println(hasFriendWhoIsMarriedWith)

        assertEquals(1, hasFriendWhoIsMarriedWith.size)
        Assert.assertEquals(
            setOf(mapOf("personA" to "Leona", "personAFriend" to "Sarah", "personAFriendMarriedWith" to "Paul")),
            hasFriendWhoIsMarriedWith
        )
    }
}

        /**
         * Select all persons pets
         */
        /**
        val personPetResult = g.V().match<Vertex>(
            As<Vertex>("person").out("hasPet").`as`("personsPet")
        ).select<Vertex>("person", "personsPet").toSet()

        personPetResult.forEach { resultRow ->
            with(resultRow["person"]!!) {
                println(this.toStr())
            }
            with(resultRow["personsPet"]!!) {
                println(this.toStr())
            }
        }

        assertEquals(5, personPetResult.size)
        assertTrue(
            personPetResult.map { it["person"]!!.toStr() + "\n" + it["personsPet"]!!.toStr() }.containsAll(
                setOf(
                    "Label: 'person' Properties:name:='Sarah'\n" +
                            "Label: 'dog' Properties:height:='58';legs:='4';name:='Sari'",
                    "Label: 'person' Properties:name:='Simone'\n" +
                            "Label: 'cat' Properties:height:='21';legs:='4';name:='Lisa'",
                    "Label: 'person' Properties:name:='Leona'\n" +
                            "Label: 'cat' Properties:name:='Catalina'",
                    "Label: 'person' Properties:name:='Leona'\n" +
                            "Label: 'dog' Properties:name:='Leo'",
                    "Label: 'person' Properties:name:='Paul'\n" +
                            "Label: 'cat' Properties:name:='Pao'"
                )
            )
        )
    }**/
