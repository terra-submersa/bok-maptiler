package ch.bok.maptiler.models

import ch.bok.maptiler.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class GeoImageTest {
    @Nested
    inner class Companion {
        @Test
        fun `should read bounding box from tiff`() {
            val file = TestUtils.getTestFile("odm_orthophoto.tif")
            val got = GeoImage.getBoundingBox(file)

            assertEquals(23.133219, got.nw.lon, 1e-4)
            assertEquals(37.428431, got.nw.lat, 1e-4)
            assertEquals(23.133328, got.se.lon, 1e-4)
            assertEquals(37.428319, got.se.lat, 1e-4)
        }
    }

    @Test
    fun `should read dimensions from tiff`() {
        val file = TestUtils.getTestFile("odm_orthophoto.tif")
        val got = GeoImage.getDimensions(file)

        assertEquals(10999, got.width)
        assertEquals(11258, got.height)
    }
}