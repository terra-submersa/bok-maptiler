package ch.bok.maptiler.server.services

import ch.bok.maptiler.TestUtils
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class MBTileSetConnectorTest {
    private val filename = TestUtils.getTestFile("tilesets/test.mbtiles").absolutePath

    @Test
    fun `should get the correct tiles count`() {
        val connector = MBTileSetConnector(filename)

        val got = connector.countTiles()

        assertEquals(52, got)
    }

}