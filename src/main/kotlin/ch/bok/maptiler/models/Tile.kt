package ch.bok.maptiler.models

import kotlin.math.PI
import kotlin.math.asinh
import kotlin.math.floor
import kotlin.math.tan

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
    }
}
