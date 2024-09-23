package ch.bok.maptiler.models

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.utils.GeoUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.sqrt

class CoordsTest : GeoImageFixtures {
    private val nwCorner = aNWCornerUTM34M()
    private val seCorner = aSECornerUTM34M()

    private val expectedDiagonalDist = run {
        val dx = nwCorner.lon - seCorner.lon
        val dy = nwCorner.lat - seCorner.lat
        sqrt(dx * dx + dy * dy)
    }

    @Test
    fun `mid`() {
        val got = aNWCornerWGS84().mid(aSECornerWGS84())

        assertEquals((aNWCornerWGS84().lon + aSECornerWGS84().lon) / 2, got.lon)
        assertEquals((aNWCornerWGS84().lat + aSECornerWGS84().lat) / 2, got.lat)
    }

    @Test
    fun `mid throw Exception if incompatible CRS`() {
        assertThrows<IncoherentMidCRSException>() { aNWCornerWGS84().mid(aSECornerUTM34M()) }
    }

    @Test
    fun `distance between the extreme of the example ortho phot with WGS84`() {
        val distance = aNWCornerWGS84().distance(aSECornerWGS84())

        assertEquals(expectedDiagonalDist, distance, 1e-2)
    }

    @Test
    fun `distance between the extreme of the example ortho phot with EPSG-32634 `() {
        val distance = nwCorner.distance(seCorner)

        assertEquals(expectedDiagonalDist, distance, 1e-3)
    }

    @Test
    fun `distance should throw ehn incompatible CRS `() {
        val nwCorner = aNWCornerUTM34M()
        val seCorner = aSECornerWGS84()

        assertThrows<IncoherentDistanceCRSException> { nwCorner.distance(seCorner) }
    }

    @Test
    fun `should transform UTM coordinates to WGS 84`() {
        // precision is cm
        val got = aNWCornerUTM34M().toCrs(GeoUtils.wgs84CRS)
        assertEquals(aNWCornerWGS84().lat, got.lat, 1e-6)
        assertEquals(aNWCornerWGS84().lon, got.lon, 1e-6)
    }

    @Test
    fun `should transform WGS 84 coordinates to EPSG 32634`() {
        // precision is cm
        val got = aNWCornerWGS84().toCrs(GeoUtils.getCRS("EPSG:32634"))
        assertEquals(aNWCornerUTM34M().lat, got.lat, 1e-2)
        assertEquals(aNWCornerUTM34M().lon, got.lon, 1e-2)
    }
}