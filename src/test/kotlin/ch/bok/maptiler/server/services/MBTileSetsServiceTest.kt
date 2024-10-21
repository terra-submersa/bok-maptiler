package ch.bok.maptiler.server.services

import ch.bok.maptiler.TestUtils
import ch.bok.maptiler.models.mbtileserver.DBName
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

        val expectedDBName = DBName("test")
        assertEquals(2, got.size, "directory should contain two files")
        assertTrue(got.containsKey(expectedDBName), "file list should contain db")
        assertTrue(got[expectedDBName]!!.endsWith(".mbtiles"), "File ends with .mbtiles")
    }
}