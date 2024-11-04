package ch.bok.maptiler.services

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.models.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import javax.imageio.ImageIO

class TilerTest : GeoImageFixtures {
    private val orthoPhotoImage = anOrthoPhotoImage()
    private val kouvertaTiler = Tiler(anOrthoPhotoImage(file = "kouverta-500.tif"))


    @Test
    fun `fitImageToTiles with max zoom`() {
        // expected size is taken from
        // gdal2tiles -v  -z 16-24 src/test/resources/odm_orthophoto.tif tmp/odm
        // find tmp/odm/24 -type f | wc -l
        val tiler = Tiler(orthoPhotoImage)
        val zoomLevel = tiler.maxZoom()

        val got = tiler.fitImageToTiles(zoomLevel)

        assertEquals(1792, got.image.width)
        assertEquals(1792, got.image.width)
        assertEquals(tiler.geoImage.boundingBox.crs, got.boundingBox.crs)
    }


    @Test
    fun `fitImageToTiles(fitImageToTile(i)) should be the same twice than fitImageToTile(i)`() {
        val tiler = Tiler(orthoPhotoImage)
        val zoomLevel = tiler.maxZoom()

        val got = Tiler(tiler.fitImageToTiles(zoomLevel)).fitImageToTiles(zoomLevel)

        assertEquals(1792, got.image.width)
        assertEquals(1792, got.image.width)
    }


    @Test
    fun `fitImageToTiles 384 with zoom 22`() {
        // expected size is taken from
        // gdal2tiles -v  -z 16-24 src/test/resources/odm_orthophoto_384.tif tmp/odm_384
        // find tmp/odm_384/22 -type f | wc -l
        val tiler = Tiler(
            anOrthoPhotoImage(file = "odm_orthophoto_384.tif")
        )

        val got = tiler.fitImageToTiles(22)

        assertEquals(512, got.image.width)
        assertEquals(512, got.image.width)
    }

    @Test
    fun `getNWTileCoords of raw image`() {
        val tiler = Tiler(orthoPhotoImage)
        val got = tiler.getNWTileCoords(24)
        assertEquals(TileCoords(9466693, 10272090, 24), got)
    }

    @Test
    fun `getSETileCoords of raw image`() {
        val tiler = Tiler(orthoPhotoImage)
        val got = tiler.getSETileCoords(24)
        assertEquals(TileCoords(9466699, 10272084, 24), got)
    }

    @Test
    fun `getSETileCoords of fitted image should be the same image`() {
        val tiler = Tiler(Tiler(orthoPhotoImage).fitImageToTiles(24))
        val got = tiler.getSETileCoords(24)
        assertEquals(TileCoords(9466699, 10272084, 24), got)
    }

    @Nested
    inner class TilePositioning {
        /*
            Based on gdal2tile generation
         */
        private val tilePos = kouvertaTiler.tilePositioning(zoomLevel = 17)

        @Test
        fun `xBandLeft`() {
            assertEquals(21, tilePos.xBandLeft)
        }

        @Test
        fun `xBandRight`() {
            assertEquals(67, tilePos.xBandRight)
        }

        @Test
        fun `yBandTop`() {
            assertEquals(30, tilePos.yBandTop)
        }

        @Test
        fun `yBandBottom`() {
            assertEquals(64, tilePos.yBandBottom)
        }
        @Test
        fun `imageTileWith`() {
            assertEquals(168, tilePos.imageTileWith)
        }
        @Test
        fun `imageTileHeight`() {
            assertEquals(162, tilePos.imageTileHeight)
        }


        @Test
        fun `tilePositioning should have the same proportion as the original image with image in one tile`() {
            assertEquals(
                1.0 * tilePos.imageTileWith / kouvertaTiler.geoImage.dimensions.width,
                1.0 * tilePos.imageTileHeight / kouvertaTiler.geoImage.dimensions.height,
                0.02
            )
        }
    }

    @Test
    fun `kouverta 500 fitted images `() {
        val got = kouvertaTiler.fitImageToTiles(19)
        assertEquals(
            Dimensions(256 * 3, 256 * 4),
            got.dimensions
        )
    }

    @Test
    fun `kouverta 500 at zoom level 19 should produce a 4x3 tileset`() {
        /**
         * find tmp/kouverta-500/19
         * tmp/kouverta-500/19
         * tmp/kouverta-500/19/296006
         * tmp/kouverta-500/19/296006/320867.png
         * tmp/kouverta-500/19/296006/320866.png
         * tmp/kouverta-500/19/296006/320864.png
         * tmp/kouverta-500/19/296006/320865.png
         * tmp/kouverta-500/19/296007
         * tmp/kouverta-500/19/296007/320867.png
         * tmp/kouverta-500/19/296007/320866.png
         * tmp/kouverta-500/19/296007/320864.png
         * tmp/kouverta-500/19/296007/320865.png
         * tmp/kouverta-500/19/296005
         * tmp/kouverta-500/19/296005/320867.png
         * tmp/kouverta-500/19/296005/320866.png
         * tmp/kouverta-500/19/296005/320864.png
         * tmp/kouverta-500/19/296005/320865.png
         * tmp/kouverta-500/19/296004
         * tmp/kouverta-500/19/296004/320867.png
         * tmp/kouverta-500/19/296004/320866.png
         * tmp/kouverta-500/19/296004/320864.png
         * tmp/kouverta-500/19/296004/320865.png
         *
         */
        val got = kouvertaTiler.fitImageToTiles(19)
        assertEquals(
            Dimensions(256 * 3, 256 * 4),
            got.dimensions
        )
    }

    @Test
    fun `Kouverta 500 flow produces a list of elements`() = runTest {
        val flow = kouvertaTiler.tileGenerator(17)

        val accTiles = mutableListOf<Tile>()
        flow.collect {
            accTiles.add(it)
        }
        val gotTiles = accTiles.sortedBy { it.coords.y }
            .sortedBy { it.coords.x }
            .sortedBy { it.coords.zoom }

        val expected = aKouverta500TileList()
        assertEquals(expected.size, gotTiles.size, "The list does not contain the expected number of elements")
        assertEquals(expected, gotTiles.map { it.coords })
    }

    @ParameterizedTest
    @MethodSource("mapToTileCoords")
    fun `get tileCoordsPosition for kouverta-500`(imagePos: Position, tilePoint: TileCoordsPosition) {
        val got = kouvertaTiler.tileCoordsPosition(imagePos, tilePoint.coords.zoom)

        assertEquals(tilePoint, got)


    }

    companion object {
        @JvmStatic
        fun mapToTileCoords() = listOf(
            Arguments.of(Position(146, 116), TileCoordsPosition(TileCoords(148002, 160433, 18), Position(134, 136))),
            Arguments.of(Position(266, 107), TileCoordsPosition(TileCoords(148002, 160433, 18), Position(210, 132))),
            Arguments.of(Position(0, 0), TileCoordsPosition(TileCoords(148002, 160433, 18), Position(43, 60))),
            Arguments.of(Position(543, 499), TileCoordsPosition(TileCoords(148003, 160432, 18), Position(123, 129))),
        )
    }
}