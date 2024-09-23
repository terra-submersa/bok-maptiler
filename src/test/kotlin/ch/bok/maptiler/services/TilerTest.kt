package ch.bok.maptiler.services

import ch.bok.maptiler.GeoImageFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TilerTest: GeoImageFixtures{
    private val orthoPhotoImage = anOrthoPhotoImage("EPSG:4326")

    @Test
    fun `fitImageToTiles with max zoom`(){
        val tiler = Tiler(orthoPhotoImage)
        println(orthoPhotoImage.getGSD())
        val zoomLevel = tiler.maxZoom()

        println(zoomLevel)
        val got = tiler.fitImageToTiles(zoomLevel)

        // ImageIO.write(got.image, "png", File("/Users/amasselot/tmp/scaled_image.png"))
        assertEquals(1536, got.image.width)
        assertEquals(1536, got.image.width)
    }
}