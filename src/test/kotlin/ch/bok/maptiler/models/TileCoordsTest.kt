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
            val givenNW = Coords (6.855469, 51.165567)
            val givenSE = Coords (6.877442, 51.151786)
            val givenZoom = 14

            @Test
            fun `get the tile from center`() {
                val got = TileCoords.getTileXY(givenNW.mid(givenSE), givenZoom)
                assertEquals(TileCoords(8504, 5473, 14), got)
            }
        }
    }
}