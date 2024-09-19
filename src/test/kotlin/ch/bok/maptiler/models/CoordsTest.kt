package ch.bok.maptiler.models

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.utils.GeoUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.sqrt

class CoordsTest : GeoImageFixtures {
    @Test
    fun `distance between the extreme of the example ortho phot with WGS84`() {
        val nwCorner = aNWCornerWGS84()
        val seCorner = aSECornerWGS84()

        val distance = nwCorner.distance(seCorner)

        val dx = 688746.758 - 688756.697
        val dy = 4144537.265 - 4144525.192
        val expected = sqrt(dx * dx + dy * dy)

        assertEquals(expected, distance, 1.0)
    }

    @Test
    fun `distance between the extreme of the example ortho phot with EPSG-32634 `() {
        val nwCorner = aNWCornerUTM34M()
        val seCorner = aSECornerUTM34M()

        val distance = nwCorner.distance(seCorner)

        val dx = 688746.758 - 688756.697
        val dy = 4144537.265 - 4144525.192
        val expected = sqrt(dx * dx + dy * dy)

        assertEquals(expected, distance, 1e-3)
    }

    @Test
    fun `distance should throw ehn incompatible CRS `() {
        val nwCorner = aNWCornerUTM34M()
        val seCorner = aSECornerWGS84()

        assertThrows<IncoherentMidCRSException> { nwCorner.distance(seCorner) }
    }
}