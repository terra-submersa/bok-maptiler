package ch.bok.maptiler.services

import ch.bok.maptiler.models.*
import ch.bok.maptiler.models.TileCoords.Companion.TILE_SIZE
import ch.bok.maptiler.utils.GeoUtils
import ch.bok.maptiler.utils.OpenMapUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.time.Instant
import javax.imageio.ImageIO

data class GeoImageTilePositioning(
    val tcwfNW: TileCoordsPosition,
    val tcwfSE: TileCoordsPosition,
    val xBandLeft: Int,
    val xBandRight: Int,
    val yBandTop: Int,
    val yBandBottom: Int,
    val imageTileWith: Int,
    val imageTileHeight: Int,
) {
    fun tilingWidth() = ((tcwfSE.coords.x - tcwfNW.coords.x + 1) * TILE_SIZE).toInt()
    fun tilingHeight() = ((tcwfNW.coords.y - tcwfSE.coords.y + 1) * TILE_SIZE).toInt()

}

class Tiler(val geoImage: GeoImage) {
    fun maxZoom() = OpenMapUtils.zoomLevelFromGSD(geoImage.getGSD(), geoImage.getCenter())

    fun getNWTileCoords(zoomLevel: Int): TileCoords {
        val nwCorner = geoImage.getNWCorner(GeoUtils.wgs84CRS)
        return TileCoords.getTileXY(nwCorner, zoomLevel)
    }

    fun getSETileCoords(zoomLevel: Int): TileCoords {
        val seCorner = geoImage.getSECorner(GeoUtils.wgs84CRS)
        val c = TileCoords.getTileXY(seCorner, zoomLevel)
        // if the image ends exactly on a tile, then we don't want to extend
        val cInner = c.plus(-1, +1)
        val cInnerPosition = geoImage.coordsToPosition(cInner.seTileCorner())
        if (cInnerPosition.x >= geoImage.dimensions.width && cInnerPosition.y <= geoImage.dimensions.height) {
            return cInner
        }
        return c
    }

    fun tileCoordsPosition(imgPos: Position, zoomLevel: Int): TileCoordsPosition {
        val pointCoords = geoImage.positionToCoords(imgPos)
        val tcwf = TileCoords.getTileXYWithFrac(pointCoords, zoomLevel)
        val posNW = geoImage.coordsToPosition(tcwf.coords.nwTileCorner())
        val posSE = geoImage.coordsToPosition(tcwf.coords.seTileCorner())
        val x0 = posNW.x
        val x1 = posSE.x
        val y0 = posNW.y
        val y1 = posSE.y
        return TileCoordsPosition(
            tcwf.coords,
            Position((tcwf.frac.first * TILE_SIZE).toInt(), (tcwf.frac.second * TILE_SIZE).toInt())
        )
    }

    fun tilePositioning(zoomLevel: Int): GeoImageTilePositioning {
        val tcwfNW = tileCoordsPosition(Position(0, 0), zoomLevel)
        val tcwfSE = tileCoordsPosition(
            Position(geoImage.dimensions.width - 1, geoImage.dimensions.height - 1),
            zoomLevel
        )

        val xBandLeft = tcwfNW.position.x
        val xBandRight = TILE_SIZE - tcwfSE.position.x
        val yBandTop = tcwfNW.position.y
        val yBandBottom = TILE_SIZE - tcwfSE.position.y
        val imageTileWith = (tcwfSE.coords.x - tcwfNW.coords.x + 1) * TILE_SIZE - xBandLeft - xBandRight
        val imageTileHeight = (tcwfNW.coords.y - tcwfSE.coords.y + 1) * TILE_SIZE - yBandTop - yBandBottom

        return GeoImageTilePositioning(
            tcwfNW = tcwfNW,
            tcwfSE = tcwfSE,
            xBandLeft = xBandLeft,
            xBandRight = xBandRight,
            yBandTop = yBandTop,
            yBandBottom = yBandBottom,
            imageTileWith = imageTileWith.toInt(),
            imageTileHeight = imageTileHeight.toInt()
        )

    }

    fun tileGenerator(minZoomLevel: Int): Flow<Tile> = flow {
        (minZoomLevel..maxZoom()).forEach { zoomLevel ->
            val fittedImage = fitImageToTiles(zoomLevel)
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
                    val coords = TileCoords(nwTileCoords.x + i, nwTileCoords.y - j, zoomLevel)
                    println("emit $coords")
                    emit(
                        Tile(
                            image = tileImage,
                            coords = coords
                        )
                    )
                }
            }
        }

    }

    /**
     * to be splittable in tile, a image
     *  * properly zoomed
     *  * dimension must be a multiple ot Tile.TILE_SIZE
     *  * transparent bands shall be added to the outskirts
     */
    fun fitImageToTiles(zoomLevel: Int): GeoImage {
        val tag = "$zoomLevel-${Instant.now().toEpochMilli()}"

        val tilePos = tilePositioning(zoomLevel)

        //scale the image
        val scaledImage =
            geoImage.image.getScaledInstance(tilePos.imageTileWith, tilePos.imageTileHeight, Image.SCALE_DEFAULT)
        val bufferedScaledImage =
            BufferedImage(tilePos.imageTileWith, tilePos.imageTileHeight, BufferedImage.TYPE_INT_ARGB)
        val g2 = bufferedScaledImage.createGraphics()
        g2.drawImage(scaledImage, 0, 0, null)
        g2.dispose()

        val canvasImage = BufferedImage(tilePos.tilingWidth(), tilePos.tilingHeight(), BufferedImage.TYPE_INT_ARGB)

        val g = canvasImage.createGraphics()
        g.color = Color(0f, 0f, 0f, 0f)
        g.fillRect(0, 0, tilePos.tilingWidth(), tilePos.tilingHeight())
        g.drawImage(scaledImage, tilePos.xBandLeft, tilePos.yBandTop, null)
        g.dispose()

        return GeoImage(
            boundingBox = BoundingBox(tilePos.tcwfNW.coords.nwTileCorner(), tilePos.tcwfSE.coords.seTileCorner()).toCrs(
                geoImage.boundingBox.crs
            ),
            dimensions = Dimensions(tilePos.tilingWidth(), tilePos.tilingHeight()),
            image = canvasImage
        )
    }

    companion object {
    }
}