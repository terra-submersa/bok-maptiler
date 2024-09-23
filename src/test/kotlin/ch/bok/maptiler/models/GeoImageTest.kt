package ch.bok.maptiler.models

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.TestUtils
import org.geotools.referencing.CRS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.Test

class GeoImageTest : GeoImageFixtures {

    val orthoPhotoImage = GeoImage.fromFile(TestUtils.getTestFile("odm_1/code/odm_orthophoto/odm_orthophoto.tif"))
    @Test
    fun `should get GSD`(){
        val file = anOrthoPhotoTifFile()
        val image = GeoImage.fromFile(file)
        val got = image.getGSD()

        assertEquals(0.00094, got, 1e-3)
    }

    @Nested
    inner class CompanionObj {
        @Test
        fun `should read bounding box from tiff in WGS84`() {
            val file = anOrthoPhotoTifFile()
            val got = GeoImage.getBoundingBox(file, "EPSG:4326")

            val expectedNW = aNWCornerWGS84()
            val expectedSE = aSECornerWGS84()

            // latitude position is within 10cm
            assertEquals(expectedNW.lon, got.nw.lon, 1e-6)
            assertEquals(expectedNW.lat, got.nw.lat, 1e-6)
            assertEquals(expectedSE.lon, got.se.lon, 1e-6)
            assertEquals(expectedSE.lat, got.se.lat, 1e-6)
        }
        @Test

        fun `should read bounding box from tiff in UTM 34M`() {
            val file = anOrthoPhotoTifFile()
            val got = GeoImage.getBoundingBox(file, "EPSG:32634")

            val expectedNW = aNWCornerUTM34M()
            val expectedSE = aSECornerUTM34M()

            // latitude position is within 10cm
            assertEquals(expectedNW.lon, got.nw.lon, 1e-1)
            assertEquals(expectedNW.lat, got.nw.lat, 1e-1)
            assertEquals(expectedSE.lon, got.se.lon, 1e-1)
            assertEquals(expectedSE.lat, got.se.lat, 1e-1)
        }

        @Test
        fun `should read dimensions from tiff`() {
            val file = TestUtils.getTestFile("odm_1/code/odm_orthophoto/odm_orthophoto.tif")
            val got = GeoImage.getDimensions(file)

            assertEquals(10999, got.width)
            assertEquals(11258, got.height)
        }
    }

    @ParameterizedTest
    @MethodSource("positionToCoords")
    fun `positionToCoords`(
        pos: Position,
        coords: Coords
    ) {
        val got = orthoPhotoImage.positionToCoords(pos)
        assertEquals(coords.lon, got.lon, 1e-5)
        assertEquals(coords.lat, got.lat, 1e-5)

    }

    @ParameterizedTest
    @MethodSource("positionToCoords")
    fun `coordsToPosition`(
        pos: Position,
        coords: Coords
    ) {
        val got = orthoPhotoImage.coordsToPosition(coords)
        assertEquals(pos.x, got.x)
        assertEquals(pos.y, got.y)

    }

    companion object:GeoImageFixtures {
        @JvmStatic
        fun positionToCoords() = listOf(
            Arguments.of(Position(0, 0), aNWCornerUTM34M()),
            Arguments.of(Position(10999, 11258), aSECornerUTM34M()),
        )
    }
}