package ch.bok.maptiler.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TileCoordsTest {
    @Nested
    inner class CompanionObject() {

        @Nested
        inner class GetTileCoords {
            // test from https://www.netzwolf.info/geo/math/tilebrowser.html?lat=51.157800&lon=6.865500&zoom=14
            private val coordsNW = Coords.build (6.855469, 51.165567, "EPSG:4326")
            private val coordsSE = Coords.build (6.877442, 51.151786, "EPSG:4326")
            private val tileCoords = TileCoords(8504L, 5473L, 14)
            @Test
            fun `get the tile from center`() {
                val got = TileCoords.getTileXY(coordsNW.mid(coordsSE), tileCoords.zoom)
                assertEquals(tileCoords, got)
            }
            @Test
            fun `get the tile NW coords coords from TileCoords`(){
                val got = TileCoords.getNWTileCorner(tileCoords)
                assertEquals(coordsNW.lon, got.lon, 1e-5)
                assertEquals(coordsNW.lat, got.lat, 1e-5)
            }
        }

    }
}