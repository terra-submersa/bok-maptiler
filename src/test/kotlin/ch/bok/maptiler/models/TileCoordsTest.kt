package ch.bok.maptiler.models

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.utils.GeoUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class TileCoordsTest {
    // test from https://www.netzwolf.info/geo/math/tilebrowser.html?lat=51.157800&lon=6.865500&zoom=14
    @ParameterizedTest
    @MethodSource("coordsToTile")
    fun `get the tile from center`(zoomLevel: Int, coords: Coords, expected: TileCoords) {
        val got = TileCoords.getTileXY(coords, zoomLevel)
        assertEquals(expected, got)
    }

    @Test
    fun `get the tile NW coords coords from TileCoords`() {
        val got = tileCoords.getNWTileCorner()
        assertEquals(coordsNW.lon, got.lon, 1e-5)
        assertEquals(coordsNW.lat, got.lat, 1e-5)
    }

    companion object : GeoImageFixtures {
        private val coordsNW = Coords.build(6.855469, 51.165567, "EPSG:4326")
        private val coordsSE = Coords.build(6.877442, 51.151786, "EPSG:4326")
        private val tileCoords = TileCoords(8504L, 5473L, 14)

        @JvmStatic
        fun coordsToTile() = listOf(
            Arguments.of(14, coordsNW.mid(coordsSE), TileCoords(8504L, 5473L, 14)),
            Arguments.of(
                17,
                Coords(23.1331729888916, 37.42845602452845, GeoUtils.wgs84CRS),
                TileCoords(73958L, 50821L, 17)
            ),
            Arguments.of(
                17,
                Coords(23.1332108258033, 37.428434250438066, GeoUtils.wgs84CRS),
                TileCoords(73958L, 50821L, 17)
            ),
            Arguments.of(
                13,
                Coords(23.1331729888916, 37.42845602452845, GeoUtils.wgs84CRS),
                TileCoords(4622L, 3176L, 13)
            ),

            )
    }
}