package ch.bok.maptiler.services

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.models.Dimensions
import ch.bok.maptiler.models.Tile
import ch.bok.maptiler.models.TileCoords
import ch.bok.maptiler.utils.GeoUtils
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

class TilerTest : GeoImageFixtures {
    private val orthoPhotoImage = anOrthoPhotoImage()

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


    @Test
    fun `kouverta 500 fitted images `() {
        val tiler = Tiler(anOrthoPhotoImage(file = "kouverta-500.tif"))
        val got = tiler.fitImageToTiles(19)
        assertEquals(
            Dimensions(256 * 3, 256 * 3),
            got.dimensions
        )
    }

    @Test
    fun `kouverta 500 at zoom level 19 should produce a 3x3 tileset`() {
        val tiler = Tiler(anOrthoPhotoImage(file = "kouverta-500.tif"))
        val got = tiler.fitImageToTiles(19)
        ImageIO.write(got.image, "png", File("/Users/amasselot/tmp/kouverta-fitted.png"))
        assertEquals(
            Dimensions(256 * 3, 256 * 3),
            got.dimensions
        )
    }

    @Test
    fun `Kouverta 500 flow produces a list of elements`() = runTest {
        val tiler = Tiler(anOrthoPhotoImage(file = "kouverta-500.tif"))
        val flow = tiler.tileGenerator(17)

        val accTiles = mutableListOf<Tile>()
        flow.collect {
            accTiles.add(it)
        }
        val gotTiles = accTiles.sortedBy { it.coords.y }
            .sortedBy { it.coords.x }
            .sortedBy { it.coords.zoom }

        val expected = aKouverta500TileList()
        assertEquals(expected.size, gotTiles.size, "The list does not contain the expected number of elements")
        assertEquals(expected, gotTiles.map{it.coords})
    }
}