package ch.bok.maptiler.services

import ch.bok.maptiler.models.*
import ch.bok.maptiler.utils.GeoUtils
import ch.bok.maptiler.utils.OpenMapUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.lang.Long.max

class Tiler(private val geoImage: GeoImage) {
    fun maxZoom() = OpenMapUtils.zoomLevelFromGSD(geoImage.getGSD(), geoImage.getCenter())

    fun getNWTileCoords(zoomLevel: Int): TileCoords {
        val nwCorner = geoImage.getNWCorner(GeoUtils.wgs84CRS)
        return TileCoords.getTileXY(nwCorner, zoomLevel)
    }

    fun getSETileCoords(zoomLevel: Int): TileCoords {
        val seCorner = geoImage.getSECorner(GeoUtils.wgs84CRS)
        val c = TileCoords.getTileXY(seCorner, zoomLevel)
        // if the image ends exactly on a tile, then we don't want to extend
        val cInner = c.plus(-1, -1)
        val cInnerPosition = geoImage.coordsToPosition(cInner.getSETileCorner())
        if (cInnerPosition.x >= geoImage.dimensions.width && cInnerPosition.y >= geoImage.dimensions.height) {
            return cInner
        }
        return c
    }

    fun tileGenerator(minZoomLevel: Int): Flow<Tile> = flow {
        val maxZoomLevel = maxZoom()
        (minZoomLevel..maxZoom()).reversed().fold(fitImageToTiles(maxZoomLevel)) { gImg, zoomLevel ->
            val fittedImage = Tiler(gImg).fitImageToTiles(zoomLevel)
            val nwCorner = fittedImage.getNWCorner(GeoUtils.wgs84CRS)
            val nwTileCoords = TileCoords.getTileXY(nwCorner, zoomLevel)

            (0..<fittedImage.dimensions.height / TileCoords.TILE_SIZE).forEach { j ->
                (0..<fittedImage.dimensions.width / TileCoords.TILE_SIZE).forEach { i ->
                    val tileImage = fittedImage.image.getSubimage(
                        i * TileCoords.TILE_SIZE,
                        j * TileCoords.TILE_SIZE,
                        TileCoords.TILE_SIZE,
                        TileCoords.TILE_SIZE
                    )
                    emit(
                        Tile(
                            image = tileImage,
                            coords = TileCoords(nwTileCoords.x + i, nwTileCoords.y + j, zoomLevel)
                        )
                    )
                }
            }
            // divide the size by 2
            val nextWidth = fittedImage.dimensions.width / 2
            val nextHeight = fittedImage.dimensions.height / 2
            val nextImage = fittedImage.image.getScaledInstance(nextWidth, nextHeight, Image.SCALE_SMOOTH)
            val nextBufferedImage = BufferedImage(nextWidth, nextHeight, BufferedImage.TYPE_INT_ARGB)
            val g = nextBufferedImage.graphics
            g.drawImage(nextImage, 0, 0, null)
            g.dispose()
            val nextGeoImage = GeoImage(fittedImage.boundingBox, Dimensions(nextWidth, nextHeight), nextBufferedImage)
            nextGeoImage
        }

    }

    /**
     * to be splittable in tile, a image
     *  * properly zoomed
     *  * dimension must be a multiple ot Tile.TILE_SIZE
     *  * transparent bands shall be added to the outskirts
     *  NB: this method is suboptimal, as it first canvas with transparent surrounding before scaling it down.
     *      This can lead to pretty large image if the percision is high and zoom level low.
     *      However, our usage here does not go in this problem. At least, should not.
     */
    fun fitImageToTiles(zoomLevel: Int): GeoImage {
        val nwTileCoords = getNWTileCoords(zoomLevel)
        val seTileCoords = getSETileCoords(zoomLevel)
        val nwTiledCorner = nwTileCoords.getNWTileCorner()
        val seTiledCorner = seTileCoords.getSETileCorner()
        val nwPos = geoImage.coordsToPosition(nwTiledCorner)
        val sePos = geoImage.coordsToPosition(seTiledCorner)

        // add transparent bands on the outside of the image
        val canvasWidth = sePos.x - nwPos.x
        val canvasHeight = sePos.y - nwPos.y
        val canvasImage = BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB)

        val g = canvasImage.createGraphics()
        g.color = Color(0f, 0f, 0f, 0f)
        g.fillRect(0, 0, canvasWidth, canvasHeight)
        g.drawImage(geoImage.image, -nwPos.x, -nwPos.y, null)
        g.dispose()

        //scale the image
        val targetWidth = (max((seTileCoords.x - nwTileCoords.x),1) * TileCoords.TILE_SIZE).toInt()
        val targetHeight = (max((seTileCoords.y - nwTileCoords.y), 1) * TileCoords.TILE_SIZE).toInt()
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

    companion object {
    }
}