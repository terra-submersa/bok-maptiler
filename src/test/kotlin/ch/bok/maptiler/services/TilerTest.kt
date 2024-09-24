package ch.bok.maptiler.services

import ch.bok.maptiler.GeoImageFixtures
import ch.bok.maptiler.models.Tile
import ch.bok.maptiler.models.TileCoords
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

class TilerTest : GeoImageFixtures {
    private val orthoPhotoImage = anOrthoPhotoImage("EPSG:4326")

    @Test
    fun `fitImageToTiles with max zoom`() {
        val tiler = Tiler(orthoPhotoImage)
        val zoomLevel = tiler.maxZoom()

        val got = tiler.fitImageToTiles(zoomLevel)

        // ImageIO.write(got.image, "png", File("/Users/amasselot/tmp/scaled_image.png"))
        assertEquals(1536, got.image.width)
        assertEquals(1536, got.image.width)
    }

    @Test
    fun `fitImageToTiles 384 with zoom 22`() {
        val tiler = Tiler(
            anOrthoPhotoImage("EPSG:4326", file = "odm_orthophoto_384.tif")
        )

        val got = tiler.fitImageToTiles(22)

        ImageIO.write(got.image, "png", File("/Users/amasselot/tmp/scaled_image.png"))
        assertEquals(256, got.image.width)
        assertEquals(256, got.image.width)
    }

    @Test
    fun `getNWTileCoords of raw image`() {
        val tiler = Tiler(orthoPhotoImage)
        val got = tiler.getNWTileCoords(24)
        assertEquals(TileCoords(9466693, 6505125, 24), got)
    }

    @Test
    fun `getSETileCoords of raw image`() {
        val tiler = Tiler(orthoPhotoImage)
        val got = tiler.getSETileCoords(24)
        assertEquals(TileCoords(9466699, 6505131, 24), got)
    }

    @Test
    fun `getSETileCoords of fitted image should be the same image`() {
        val tiler = Tiler(Tiler(orthoPhotoImage).fitImageToTiles(24))
        val got = tiler.getSETileCoords(24)
        assertEquals(TileCoords(9466699, 6505131, 24), got)
    }

    @Test
    fun `fitImageToTiles(fitImageToTile(i)) should be the same twice than fitImageToTile(i)`() {
        val tiler = Tiler(orthoPhotoImage)
        val zoomLevel = tiler.maxZoom()

        val got = Tiler(tiler.fitImageToTiles(zoomLevel)).fitImageToTiles(zoomLevel)

        // ImageIO.write(got.image, "png", File("/Users/amasselot/tmp/scaled_image.png"))
        assertEquals(1536, got.image.width)
        assertEquals(1536, got.image.width)
    }

    @Test
    fun `Flow produces a list of element`() = runTest {

        val tiler = Tiler(orthoPhotoImage)

        val flow = tiler.tileGenerator(16)

        val list = mutableListOf<Tile>()
        flow.collect {
            list.add(it)
        }

        assertEquals(52, list.size, "The list does not contain the expected number of elements")
        assertEquals(TileCoords(9466693, 6505125, 24), list[0].coords, "tile for the first")
        assertEquals(TileCoords(9466698, 6505130, 24), list[35].coords, "tile for the 36th")
        assertEquals(TileCoords(36979, 25410, 16), list[51].coords, "tile for the last")
    }
}