package ch.bok.maptiler.utils

import ch.bok.maptiler.models.Coords
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.log2

object OpenMapUtils {
    fun zoomLevelFromGSD(gsd: Double, at: Coords): Int {
        val c = 40075016.686
        val l = log2(c * cos(at.lat / 180 * PI) / gsd) - 8
        return floor(l).toInt()

    }
}