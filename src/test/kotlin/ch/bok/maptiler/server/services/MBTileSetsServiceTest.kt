package ch.bok.maptiler.server.services

import ch.bok.maptiler.TestUtils
import ch.bok.maptiler.server.configuration.MBTilesProperties
import ch.bok.maptiler.server.services.MBTileSetsService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MBTileSetsServiceTest {
    private val properties = MBTilesProperties(folder = TestUtils.getTestFile("tilesets").absolutePath)

    @Test
    fun `should get the mbtiles files`(){
        val service = MBTileSetsService(properties)

        val got = service.listTileSetFiles()

        assertEquals(1, got.size)
        assertTrue(got.first().endsWith(".mbtiles"), "listed files ends with .mbtiles")
    }
}