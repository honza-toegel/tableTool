package org.jto.tabletool

import com.beust.klaxon.Klaxon
import org.junit.Test
import kotlin.test.assertEquals

class RelatedVertexTest {

    @Test
    fun testEmptyJSONObject () {
        val resultList = VertexData.parseFromJSONString("")
        println(resultList)
        assertEquals(0, resultList.size)
    }

    @Test
    fun testJSONObject () {
        val resultList = VertexData.parseFromJSONString("""
        {
            "label" : "mftType",
            "name": "John Smith",
        }
        """)
        println(resultList)
        assertEquals(1, resultList.size)
        assertEquals("John Smith", resultList[0].name)
        assertEquals("mftType", resultList[0].label)
    }

    @Test
    fun testJSONList () {
        val resultList = VertexData.parseFromJSONString("""
        [
            {"name": "John Smith",}, 
            {"name": "John Paul",}
        ]
        """)
        println(resultList)
        assertEquals(2, resultList.size)
        assertEquals("John Smith", resultList[0].name)
        assertEquals("John Paul", resultList[1].name)
        assertEquals("", resultList[0].label)
    }

    @Test
    fun testSimpleEmptyString () {
        val resultList = VertexData.parseFromSimpleString("")
        println(resultList)
        assertEquals(0, resultList.size)
    }

    @Test
    fun testSimpleString () {
        val resultList = VertexData.parseFromSimpleString("""John Smith;Paul \;Smith
            |Olga
        """.trimMargin())
        println(resultList)
        assertEquals(3, resultList.size)
        assertEquals("John Smith", resultList[0].name)
        assertEquals("Paul ;Smith", resultList[1].name)
        assertEquals("Olga", resultList[2].name)
        assertEquals("", resultList[0].label)
    }
}