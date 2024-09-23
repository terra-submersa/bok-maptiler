package ch.bok.maptiler.utils

import ch.bok.maptiler.models.Coords
import ch.bok.maptiler.models.GeoImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.log2

object OpenMapUtils {
    fun maxZoomLevel(geoImage: GeoImage) =
        zoomLevelFromGSD(geoImage.getGSD(), geoImage.boundingBox.center())
    fun zoomLevelFromGSD(gsd: Double, at: Coords): Int {
        val atWGS84 = at.toCrs(GeoUtils.wgs84CRS)
        val c = 40075016.686
        val l = log2(c * cos(atWGS84.lat / 180 * PI) / gsd) - 8
        return floor(l).toInt()

    }
}