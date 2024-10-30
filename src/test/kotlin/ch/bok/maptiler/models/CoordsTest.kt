package ch.bok.maptiler.models

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.utils.GeoUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opengis.referencing.crs.CoordinateReferenceSystem
import kotlin.math.sqrt

class CannotAssertCloseForCRS(crs: CoordinateReferenceSystem) :
    RuntimeException("Cannot assert close with CRS ${crs.alias}")

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

    @Nested
    inner class DistanceFixtureWGS84vsUTM {
        private fun assertClose(expected: Double, actual: Double, tolerance: Double) =
            assertEquals(expected, actual, tolerance)

        private fun assertCloseUTM(expected: Double, actual: Double) = assertClose(expected, actual, 1e-3)
        private fun assertCloseWGS84(expected: Double, actual: Double) = assertClose(expected, actual, 1e-10)
        fun assertClose(expected: Coords, actual: Coords) =
            if (expected.crs == GeoUtils.utm34NCRS) {
                assertCloseUTM(expected.lon, actual.lon)
                assertCloseUTM(expected.lat, actual.lat)
            } else if (expected.crs == GeoUtils.wgs84CRS) {
                assertCloseWGS84(expected.lon, actual.lon)
                assertCloseWGS84(expected.lat, actual.lat)
            } else {
                throw CannotAssertCloseForCRS(expected.crs)

            }

        private fun assertClose(expected: BoundingBox, actual: BoundingBox) {
            assertClose(expected.nw, actual.nw)
            assertClose(expected.ne(), actual.ne())
            assertClose(expected.se, actual.se)
            assertClose(expected.sw(), actual.sw())
        }

        @Test
        fun `both bounding boxes should be coherent WGS84 to UTM`() {
            val toUTM = aBoundingBoxWGS84().toCrs(GeoUtils.utm34NCRS)
            assertClose(toUTM, aBoundingBoxUTM34M())
        }

        @Test
        fun `both bounding boxes should be coherent UTM to WGS84`() {
            assertClose(aBoundingBoxUTM34M().toCrs(GeoUtils.wgs84CRS), aBoundingBoxWGS84())
        }

        @Test
        fun `height should be the same`() {
            val gotWGS84 = aBoundingBoxWGS84().height()
            val gotUTM34N = aBoundingBoxUTM34M().height()
            assertEquals(gotWGS84, gotUTM34N, 1e-5)
        }
    }

    @Nested
    inner class DistanceSameLatLonWGS84 {
        //expected: https://www.netzwolf.info/geo/math/tilebrowser.html?lat=37.42838242616285&lon=23.13327147498925&zoom=17#tile
        private val nw = Coords(23.1317138671875, 37.42906945530332, GeoUtils.wgs84CRS)
        private val se = Coords(23.13446044921875, 37.42688834526727, GeoUtils.wgs84CRS)
        private val ne = Coords(23.13446044921875, 37.42906945530332, GeoUtils.wgs84CRS)
        private val sw = Coords(23.1317138671875, 37.42688834526727, GeoUtils.wgs84CRS)

        @Test
        fun `left side`() {
            val distance: Double = nw.distance(sw)

            assertEquals(242.07198198406286, distance, 2e-2)
        }

        @Test
        fun `right side`() {
            val distance: Double = ne.distance(se)

            assertEquals(242.07198198370864, distance, 2e-2)
        }

        @Test
        fun `top side`() {
            val distance: Double = nw.distance(ne)

            assertEquals(243.09728182315826, distance, 2e-2)
        }

        @Test
        fun `bottom side`() {
            val distance: Double = se.distance(sw)

            assertEquals(243.1043344227021, distance, 2e-2)
        }

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