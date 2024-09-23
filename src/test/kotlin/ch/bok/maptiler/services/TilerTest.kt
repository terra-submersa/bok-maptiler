package ch.bok.maptiler.services

import ch.bok.maptiler.GeoImageFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

class TilerTest: GeoImageFixtures{
    private val orthoPhotoImage = anOrthoPhotoImage("EPSG:4326")

    @Test
    fun `fitImageToTiles with max zoom`(){
        val tiler = Tiler(orthoPhotoImage)
        println(orthoPhotoImage.getGSD())
        val zoomLevel = tiler.maxZoom()

        val got = tiler.fitImageToTiles(17)

        ImageIO.write(got.image, "png", File("/Users/amasselot/tmp/scaled_image.png"))
    }
}