package ch.bok.maptiler.services

import ch.bok.maptiler.models.BoundingBox
import ch.bok.maptiler.models.Dimensions
import ch.bok.maptiler.models.GeoImage
import ch.bok.maptiler.models.TileCoords
import ch.bok.maptiler.utils.GeoUtils
import ch.bok.maptiler.utils.OpenMapUtils
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage

class Tiler(private val geoImage: GeoImage) {
    fun maxZoom() = OpenMapUtils.zoomLevelFromGSD(geoImage.getGSD(), geoImage.getCenter())

    /**
     * to be splittable in tile, a image
     *  * properly zoomed
     *  * dimension must be a multiple ot Tile.TILE_SIZE
     *  * transparent bands shall be added to the outskirts
     */
    fun fitImageToTiles( zoomLevel: Int): GeoImage {
        val nwCorner= geoImage.getNWCorner(GeoUtils.wgs84CRS)
        val seCorner = geoImage.getSECorner(GeoUtils.wgs84CRS)
        val nwTileCoords = TileCoords.getTileXY(nwCorner, zoomLevel)
        val seTileCoords = TileCoords.getTileXY(seCorner, zoomLevel)
        val nwTiledCorner = nwTileCoords.getNWTileCorner()
        val seTiledCorner = seTileCoords.getSETileCorner()
        val nwPos = geoImage.coordsToPosition(nwTiledCorner)
        val sePos = geoImage.coordsToPosition(seTiledCorner)

        // add transparent bands on the outside of the image
        val canvasWidth = sePos.x - nwPos.x
        val canvasHeight = nwPos.y - sePos.y
        val canvasImage = BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB)

        val g = canvasImage.createGraphics()
        g.color = Color(0f, 0f, 0f, 0f)
        g.fillRect(0, 0, canvasWidth, canvasHeight)
        g.drawImage(geoImage.image, -nwPos.x, -nwPos.y, null)
        g.dispose()

        //scale the image
        val targetWidth = ((seTileCoords.x - nwTileCoords.x) * TileCoords.TILE_SIZE).toInt()
        val targetHeight = ((nwTileCoords.y - seTileCoords.y) * TileCoords.TILE_SIZE).toInt()
        val scaledImage = canvasImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT)
        val bufferedScaledImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        val g2 = bufferedScaledImage.createGraphics()
        g2.drawImage(scaledImage, 0, 0, null)
        g2.dispose()


        return GeoImage(
            boundingBox = BoundingBox(nwTiledCorner, seTiledCorner),
            dimensions = Dimensions(targetWidth, targetHeight),
            image = bufferedScaledImage
        )
    }

}