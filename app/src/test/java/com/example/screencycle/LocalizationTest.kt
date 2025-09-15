package com.example.screencycle

import org.junit.Test
import org.junit.Assert.assertTrue
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class LocalizationTest {
    @Test
    fun ruLocaleHasAllStrings() {
        val baseFile = File("src/main/res/values/strings.xml")
        val ruFile = File("src/main/res/values-ru/strings.xml")
        val factory = DocumentBuilderFactory.newInstance()
        val baseDoc = factory.newDocumentBuilder().parse(baseFile)
        val ruDoc = factory.newDocumentBuilder().parse(ruFile)
        val baseNames = baseDoc.getElementsByTagName("string")
        val baseSet = (0 until baseNames.length)
            .map { baseNames.item(it).attributes.getNamedItem("name").nodeValue }
            .toSet()
        val ruNames = ruDoc.getElementsByTagName("string")
        val ruSet = (0 until ruNames.length)
            .map { ruNames.item(it).attributes.getNamedItem("name").nodeValue }
            .toSet()
        val missingInRu = baseSet - ruSet
        val missingInBase = ruSet - baseSet
        assertTrue(
            "Missing in ru: $missingInRu, missing in base: $missingInBase",
            missingInRu.isEmpty() && missingInBase.isEmpty()
        )
    }
}
