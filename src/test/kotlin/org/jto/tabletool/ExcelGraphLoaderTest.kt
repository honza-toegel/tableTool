package org.jto.tabletool

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.VertexProperty
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExcelGraphLoaderTest {
    @Test
    fun testExcelGraphLoader() {
        val graph: Graph = TinkerGraph.open()
        val g: GraphTraversalSource = graph.traversal()

        val testFile = ExcelGraphLoaderTest::class.java.getResource("/test.xlsx").file
        ExcelGraphLoader(testFile, g).loadGraph()

        val allPersonResult = g.V().hasLabel("person").toSet()
        allPersonResult.forEach {
            println(it.toStr())
        }

        assertEquals(5, allPersonResult.size)

        val result = g.V().match<String>(
            As<String>("personA").out("hasFriend").`as`("personAFriend"),
            As<String>("personAFriend").out("marriedWith").`as`("personAFriendMarriedWith")
        ).select<String>("personA", "personAFriend", "personAFriendMarriedWith").by("name").toSet()

        result.forEach {
            println(it)
        }

        assertEquals(1, result.size)
        assertEquals(
            mapOf("personA" to "Leona", "personAFriend" to "Sarah", "personAFriendMarriedWith" to "Paul"),
            result.iterator().next()
        )

        /**
         * Select all persons pets
         */
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
    }
}