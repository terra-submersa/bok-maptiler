package ch.bok.maptiler.models

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.utils.GeoUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.Test

class GeoImageTest : GeoImageFixtures {

    val orthoPhotoImageUTM34N = anOrthoPhotoImage()
    val orthoPhotoImageWGS84 = anOrthoPhotoImage("EPSG:4326")

    @Test
    fun `should get GSD UTM34N`() {
        val got = orthoPhotoImageUTM34N.getGSD()

        assertEquals(0.008001110580721883, got, 1e-3)
    }

    fun `should get GSD WGS84`() {
        val got = orthoPhotoImageWGS84.getGSD()

        assertEquals(0.008001110580721883, got, 1e-3)
    }

    @Nested
    inner class CompanionObj {
        @Test
        fun `should read bounding box from tiff in WGS84`() {
            val file = anOrthoPhotoTifFile()
            val got = GeoImage.getBoundingBox(file, "EPSG:4326")

            val expectedNW = aNWCornerWGS84()
            val expectedSE = aSECornerWGS84()

            // latitude position is within 10cm
            assertEquals(expectedNW.lon, got.nw.lon, 1e-6)
            assertEquals(expectedNW.lat, got.nw.lat, 1e-6)
            assertEquals(expectedSE.lon, got.se.lon, 1e-6)
            assertEquals(expectedSE.lat, got.se.lat, 1e-6)
        }

        @Test

        fun `should read bounding box from tiff in UTM 34M`() {
            val file = anOrthoPhotoTifFile()
            val got = GeoImage.getBoundingBox(file, "EPSG:32634")

            val expectedNW = aNWCornerUTM34M()
            val expectedSE = aSECornerUTM34M()

            // latitude position is within 10cm
            assertEquals(expectedNW.lon, got.nw.lon, 1e-1)
            assertEquals(expectedNW.lat, got.nw.lat, 1e-1)
            assertEquals(expectedSE.lon, got.se.lon, 1e-1)
            assertEquals(expectedSE.lat, got.se.lat, 1e-1)
        }

        @Test
        fun `should read dimensions from tiff`() {
            val got = orthoPhotoImageUTM34N.dimensions

            assertEquals(1375, got.width)
            assertEquals(1407, got.height)
        }
    }

    @Test
    fun `image ratio is coherent between dimension and measures`() {
        /*
        gdalinfo src/test/resources/odm_orthophoto_384.tif
        Size is 375, 384
        Upper Left  (  688745.970, 4144537.752) ( 23d 7'59.56"E, 37d25'42.36"N)
        Lower Left  (  688745.970, 4144526.494) ( 23d 7'59.55"E, 37d25'42.00"N)
        Upper Right (  688756.965, 4144537.752) ( 23d 8' 0.01"E, 37d25'42.36"N)
        Lower Right (  688756.965, 4144526.494) ( 23d 8' 0.00"E, 37d25'41.99"N)
        Pixel Size = (0.029317708333110,-0.029317708333110)
         */
        val imageUTM = anOrthoPhotoImage("EPSG:32634", file = "odm_orthophoto_384.tif")

        val expectedWidth=375
        val expectedHeight=384
        val expectedXDist = 688756.965 - 688745.970
        val expectedYDist = 4144537.752 - 4144526.494

        val bbUTM = imageUTM.boundingBox

        assertEquals(expectedXDist, bbUTM.sw().distance(bbUTM.se), 2e-3)
        assertEquals(expectedXDist, bbUTM.nw.distance(bbUTM.ne()), 2e-3)
        assertEquals(expectedYDist, bbUTM.nw.distance(bbUTM.sw()), 2e-3)
        assertEquals(expectedYDist, bbUTM.ne().distance(bbUTM.se), 2e-3)

        assertEquals(expectedWidth/expectedXDist, expectedHeight/expectedYDist, 3e-3)


    }

    @Test
    fun `a tile pixel should be square`() {
        // expected from https://www.netzwolf.info/geo/math/tilebrowser.html?lat=37.42838242616285&lon=23.13327147498925&zoom=17#tile

        val image = anOrthoPhotoImage( file = "odm_orthophoto_384.tif")
        val imageCenter = image.getCenter()
        val tile = TileCoords.getTileXY(imageCenter, 22)

        val posNE = image.coordsToPosition(tile.getNWTileCorner())
        val posSW = image.coordsToPosition(tile.getSETileCorner())

        assertEquals(posSW.x - posNE.x, posSW.y - posNE.y)

    }


    @ParameterizedTest
    @MethodSource("positionToCoordsData")
    fun `positionToCoords`(
        pos: Position,
        coords: Coords,
        crsCode: String
    ) {
        val got = anOrthoPhotoImage(crsCode).positionToCoords(pos)
        assertEquals(coords.lon, got.lon, 1e-5)
        assertEquals(coords.lat, got.lat, 1e-5)

    }

    @ParameterizedTest
    @MethodSource("positionToCoordsData")
    fun `coordsToPosition`(
        pos: Position,
        coords: Coords,
        crsCode: String,
    ) {
        val got = anOrthoPhotoImage(crsCode).coordsToPosition(coords)
        assertEquals(pos.x, got.x)
        assertEquals(pos.y, got.y)

    }

    companion object : GeoImageFixtures {
        @JvmStatic
        fun positionToCoordsData() = listOf(
            Arguments.of(Position(0, 0), aNWCornerWGS84(), "EPSG:4326"),
            Arguments.of(Position(0, 0), aNWCornerUTM34M(), "EPSG:32634"),
            Arguments.of(Position(1375, 1407), aSECornerWGS84(), "EPSG:4326"),
            Arguments.of(Position(1375, 1407), aSECornerUTM34M(), "EPSG:32634"),
        )
    }
}