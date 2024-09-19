package ch.bok.maptiler.models

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class GeoImageTest : GeoImageFixtures {
    @Nested
    inner class CompanionObject {
        @Test
        fun `should read bounding box from tiff`() {
            val file = anOrthoPhotoTifFile()
            val got = GeoImage.getBoundingBox(file)

            val expectedNW = aNWCornerWGS84()
            val expectedSE = aSECornerWGS84()

            assertEquals(expectedNW.lon, got.nw.lon, 1e-4)
            assertEquals(expectedNW.lat, got.nw.lat, 1e-4)
            assertEquals(expectedSE.lon, got.se.lon, 1e-4)
            assertEquals(expectedSE.lat, got.se.lat, 1e-4)
        }

        @Test
        fun `should read dimensions from tiff`() {
            val file = TestUtils.getTestFile("odm_1/code/odm_orthophoto/odm_orthophoto.tif")
            val got = GeoImage.getDimensions(file)

            assertEquals(10999, got.width)
            assertEquals(11258, got.height)
        }
    }
}