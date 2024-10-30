package ch.bok.maptiler.models

import ch.bok.maptiler.utils.GeoUtils
import org.opengis.referencing.crs.CoordinateReferenceSystem
import java.awt.image.BufferedImage
import kotlin.math.*

class UnsupportedCoordinateReferenceSystemToGetTileXY(crs: CoordinateReferenceSystem) :
    RuntimeException("Unsupported reference coordinate system ${crs.name} to get tile XY")

data class TileCoords(
    val x: Long,
    val y: Long,
    val zoom: Int
) {
    fun plus(dx: Int = 0, dy: Int = 0) = TileCoords(x + dx, y + dy, zoom)
    fun nwTileCorner(): Coords {
        val n = 1L shl zoom
        val x1 = x.toDouble() / n
        val y1 = (n - 1 - y).toDouble() / n
        val lonMerc = (x1 * 2 - 1) * PI
        val latMerc = -(y1 * 2 - 1) * PI

        val lambda = lonMerc
        val phi = 2 * atan(exp(latMerc)) - PI / 2

        val lonDeg = lambda / PI * 180
        val latDeg = phi / PI * 180
        return Coords(lonDeg, latDeg, GeoUtils.wgs84CRS)

//        val lonDeg = x.toDouble() / n * 360.0 - 180.0
//        val latRad = atan(sinh(PI * (1 - 2 * (n - 1 - y.toDouble()) / n)))
//        val latDeg = Math.toDegrees(latRad)
//        return Coords(lonDeg, latDeg, GeoUtils.wgs84CRS)
    }

    fun seTileCorner(): Coords = TileCoords(x + 1, y - 1, zoom).nwTileCorner()

    fun boundingBox() = BoundingBox(nwTileCorner(), seTileCorner())
    override fun toString() = "$zoom/$x/$y"

    companion object {
        val TILE_SIZE = 256
        fun getTileXY(coords: Coords, zoom: Int): TileCoords {
            if (coords.crs != GeoUtils.wgs84CRS) {
                return getTileXY(coords.toCrs(GeoUtils.wgs84CRS), zoom)
            }
            val latRad = Math.toRadians(coords.lat)
            var xtile = floor((coords.lon + 180) / 360 * (1L shl zoom)).toLong()
            var ytile = floor((1.0 - asinh(tan(latRad)) / PI) / 2 * (1L shl zoom)).toLong()

            ytile = (1L shl zoom) - 1 - ytile

            if (xtile < 0) {
                xtile = 0
            }
            if (xtile >= (1L shl zoom)) {
                xtile = (1L shl zoom) - 1
            }
            if (ytile < 0) {
                ytile = 0
            }
            if (ytile >= (1 shl zoom)) {
                ytile = (1L shl zoom) - 1
            }
            return TileCoords(xtile, ytile, zoom)
        }


    }
}

data class Tile(val image: BufferedImage, val coords: TileCoords)