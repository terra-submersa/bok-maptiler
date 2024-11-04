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
    // WARNING, the url gives the coordinates for the googlmap/leaflet call. The mbtiles format is x,y 0,0 on the bottom left
    @ParameterizedTest
    @MethodSource("coordsToTile")
    fun `get the tile from center`(zoomLevel: Int, coords: Coords, expected: TileCoords) {
        val got = TileCoords.getTileXY(coords, zoomLevel)
        assertEquals(expected, got)
    }

    @Test
    fun `get the tile NW coords coords from TileCoords`() {
        val got = tileCoords.nwTileCorner()
        assertEquals(coordsNW.lon, got.lon, 1e-5)
        assertEquals(coordsNW.lat, got.lat, 1e-5)
    }

    @Test
    fun `get the tile SE coords coords from TileCoords`() {
        val got = tileCoords.seTileCorner()
        assertEquals(coordsSE.lon, got.lon, 1e-5)
        assertEquals(coordsSE.lat, got.lat, 1e-5)
    }

    @Nested
    inner class Tile_73958_50821_17{
        // https://www.netzwolf.info/geo/math/tilebrowser.html?lat=37.42838242616285&lon=23.13327147498925&zoom=17#tile
        val tile = TileCoords(73958, (1 shl 17) - 1 - 50821, 17)

        @Test
        fun nw(){
            val corner = tile.boundingBox().nw
            assertEquals(23.131714, corner.lon, 1e-6)
            assertEquals(37.429070, corner.lat, 1e-6)
        }
        @Test
        fun se(){
            val corner = tile.boundingBox().se
            assertEquals(23.134461, corner.lon, 1e-6)
            assertEquals(37.426888, corner.lat, 1e-6)
        }
        @Test
        fun `height`() {
            assertEquals(242.54 , tile.nwTileCorner().distance(tile.plus(0, -1).nwTileCorner()), 0.5)
        }

        @Test
        fun `width`() {

            assertEquals(242.54 , tile.boundingBox().width(), 0.5)
        }
    }

    companion object : GeoImageFixtures {
        private val coordsNW = Coords.build(6.855469, 51.165567, "EPSG:4326")
        private val coordsSE = Coords.build(6.877442, 51.151786, "EPSG:4326")
        private val tileCoords = TileCoords(8504L, 10910L, 14)

        @JvmStatic
        fun coordsToTile() = listOf(
            Arguments.of(
                14,
                coordsNW.mid(coordsSE),
                TileCoords(8504L, 10910L, 14)
            ),
            Arguments.of(
                17,
                Coords(23.1331729888916, 37.42845602452845, GeoUtils.wgs84CRS),
                TileCoords(73958L, 80250L, 17)
            ),
            Arguments.of(
                17,
                Coords(23.1332108258033, 37.428434250438066, GeoUtils.wgs84CRS),
                TileCoords(73958L, 80250L, 17)
            ),
            Arguments.of(
                13,
                Coords(23.1331729888916, 37.42845602452845, GeoUtils.wgs84CRS),
                TileCoords(4622L, 5015L, 13)
            ),
            Arguments.of(
                17,
                Coords(23.1331729888916, 37.42845602452845, GeoUtils.wgs84CRS).toCrs(GeoUtils.utm34NCRS),
                TileCoords(73958L, 80250L, 17)
            ),
            Arguments.of(
                17,
                Coords(23.1332108258033, 37.428434250438066, GeoUtils.wgs84CRS).toCrs(GeoUtils.utm34NCRS),
                TileCoords(73958L, 80250L, 17)
            ),
            Arguments.of(
                13,
                Coords(23.1331729888916, 37.42845602452845, GeoUtils.wgs84CRS).toCrs(GeoUtils.utm34NCRS),
                TileCoords(4622L, 5015L, 13)
            ),

            )
    }
}