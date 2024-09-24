package ch.bok.maptiler.services

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.TestUtils
import ch.bok.maptiler.models.MBTilesMetadata
import ch.bok.maptiler.utils.GeoUtils
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class MBTilesCreatorTest : GeoImageFixtures {
    private val orthoPhotoImage = anOrthoPhotoImage("EPSG:4326")

    @BeforeEach
    fun setUp() {
        TestUtils.mkdir("tmp")
    }

    @Test
    fun insertMetadata() {
        val file = "tmp/test-metadata.db"
        TestUtils.rm(file)
        val metadata = MBTilesMetadata.build(
            geoImage = orthoPhotoImage,
            minZoomLevel = 16,
            name = "paf le chien",
            attributes = mapOf(
                "pouet" to 42,
                "flapflap" to "la girafe"
            )
        )
        val creator = MBTilesCreator(file)
        creator.createSchema()
        creator.insertMetadata(metadata)

        val got = creator.execute("select name,value from metadata")
        fun gotValue(name: String) = got.firstOrNull { it[0] == name }?.let { it[1] }

        assertNotNull(gotValue("name"))
        assertEquals("paf le chien", gotValue("name"))
        assertNotNull(gotValue("bounds"))
        assertEquals("23.1332108258033,37.42833060029414,23.133332212431817,37.428434250438066", gotValue("bounds"))
    }

    @Test
    fun insertTiles() {
        val file = "tmp/test-tiles.db"
        TestUtils.rm(file)
        val creator = MBTilesCreator(file)
        creator.createSchema()
        val tiler = Tiler(orthoPhotoImage)
        runBlocking {
            creator.insertTiles(tiler.tileGenerator(16))
        }
        val got = creator.execute("select * from tiles")

        assertEquals(52, got.size, "check the number of lines in the table")

    }
}