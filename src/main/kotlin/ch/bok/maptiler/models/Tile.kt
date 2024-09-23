package ch.bok.maptiler.models

import ch.bok.maptiler.utils.GeoUtils
import org.geotools.referencing.crs.DefaultGeographicCRS
import kotlin.math.*

data class TileCoords(
    val x: Long,
    val y: Long,
    val zoom: Int
){
    override fun toString() = "$zoom/$x/$y"
    companion object{
        fun getTileXY(coords: Coords, zoom: Int):TileCoords{
            val latRad = Math.toRadians(coords.lat)
            var xtile = floor( (coords.lon + 180) / 360 * (1L shl zoom) ).toLong()
            var ytile = floor( (1.0 - asinh(tan(latRad)) / PI) / 2 * (1L shl zoom) ).toLong()

            if (xtile < 0) {
                xtile = 0
            }
            if (xtile >= (1L shl zoom)) {
                xtile= (1L shl zoom) - 1
            }
            if (ytile < 0) {
                ytile = 0
            }
            if (ytile >= (1 shl zoom)) {
                ytile = (1L shl zoom) - 1
            }
            return TileCoords(xtile, ytile, zoom)
        }

        fun getNWTileCorner(tileCoords: TileCoords): Coords{
            val n = 1L shl tileCoords.zoom
            val lonDeg = tileCoords.x.toDouble() / n * 360.0 - 180.0
            val latRad = atan(sinh(PI * (1 - 2 * tileCoords.y.toDouble() / n)))
            val latDeg = Math.toDegrees(latRad)
            return Coords(lonDeg, latDeg, GeoUtils.wgs84CRS)
        }
    }
}
