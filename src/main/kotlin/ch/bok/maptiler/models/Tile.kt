package ch.bok.maptiler.models

import ch.bok.maptiler.utils.GeoUtils
import org.geotools.referencing.crs.DefaultGeographicCRS
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
    fun getNWTileCorner(): Coords {
        val n = 1L shl zoom
        val lonDeg = x.toDouble() / n * 360.0 - 180.0
        val latRad = atan(sinh(PI * (1 - 2 * y.toDouble() / n)))
        val latDeg = Math.toDegrees(latRad)
        return Coords(lonDeg, latDeg, GeoUtils.wgs84CRS)
    }

    fun getSETileCorner(): Coords = TileCoords(x + 1, y + 1, zoom).getNWTileCorner()


    override fun toString() = "$zoom/$x/$y"

    companion object {
        val TILE_SIZE = 256
        fun getTileXY(coords: Coords, zoom: Int): TileCoords {
            if (coords.crs != GeoUtils.wgs84CRS) {
                throw UnsupportedCoordinateReferenceSystemToGetTileXY(coords.crs)
            }
            val latRad = Math.toRadians(coords.lat)
            var xtile = floor((coords.lon + 180) / 360 * (1L shl zoom)).toLong()
            var ytile = floor((1.0 - asinh(tan(latRad)) / PI) / 2 * (1L shl zoom)).toLong()

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