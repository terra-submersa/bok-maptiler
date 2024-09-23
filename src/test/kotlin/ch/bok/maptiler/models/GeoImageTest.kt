package ch.bok.maptiler.models

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.Test

class GeoImageTest : GeoImageFixtures {

    val orthoPhotoImageUTM34N = anOrthoPhotoImage()
    val orthoPhotoImageWGS84 = anOrthoPhotoImage("EPSG:4326")

    @Test
    fun `should get GSD UTM34N`() {
        val got = orthoPhotoImageUTM34N.getGSD()

        assertEquals(0.008001110580721883, got, 1e-3)
    }

    fun `should get GSD WGS84`() {
        val got = orthoPhotoImageWGS84.getGSD()

        assertEquals(0.008001110580721883, got, 1e-3)
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
            val got = orthoPhotoImageUTM34N.dimensions

            assertEquals(1375, got.width)
            assertEquals(1407, got.height)
        }
    }

    @ParameterizedTest
    @MethodSource("positionToCoordsData")
    fun `positionToCoords`(
        pos: Position,
        coords: Coords,
        crsCode: String
    ) {
        val got = anOrthoPhotoImage(crsCode).positionToCoords(pos)
        assertEquals(coords.lon, got.lon, 1e-5)
        assertEquals(coords.lat, got.lat, 1e-5)

    }

    @ParameterizedTest
    @MethodSource("positionToCoordsData")
    fun `coordsToPosition`(
        pos: Position,
        coords: Coords,
        crsCode: String,
    ) {
        val got = anOrthoPhotoImage(crsCode).coordsToPosition(coords)
        assertEquals(pos.x, got.x)
        assertEquals(pos.y, got.y)

    }

    companion object : GeoImageFixtures {
        @JvmStatic
        fun positionToCoordsData() = listOf(
            Arguments.of(Position(0, 0), aNWCornerWGS84(), "EPSG:4326"),
            Arguments.of(Position(0, 0), aNWCornerUTM34M(), "EPSG:32634"),
            Arguments.of(Position(1374, 1407), aSECornerWGS84(), "EPSG:4326"),
            Arguments.of(Position(1374, 1407), aSECornerUTM34M(), "EPSG:32634"),
        )
    }
}