package ch.bok.maptiler.utils

import ch.bok.maptiler.models.Coords
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource


class OpenMapUtilsTest {
    @ParameterizedTest
    @MethodSource("zoomLevelGSDLat")
    fun `zoom Level should be deduced from GSD an latitude`(
        givenGSD: Double,
        givenLatitude: Double,
        expectedZoomLevel: Int
    ) {
        val c = Coords(0.0, givenLatitude, GeoUtils.wgs84CRS)
        val got = OpenMapUtils.zoomLevelFromGSD(givenGSD, c)
        assertEquals(expectedZoomLevel, got)
    }


    companion object {
        @JvmStatic
        fun zoomLevelGSDLat() = listOf(
            Arguments.of(9.55, 0.0, 14),
            Arguments.of(0.148, 0.0, 20),
            Arguments.of(0.0033, 80.0, 23),
            Arguments.of(0.00094, 37.4283743, 27),
        )
    }
}