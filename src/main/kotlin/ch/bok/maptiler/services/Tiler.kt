package ch.bok.maptiler.services

import ch.bok.maptiler.models.GeoImage
import ch.bok.maptiler.utils.OpenMapUtils

class Tiler(private val image: GeoImage) {
    fun maxZoom() = OpenMapUtils.zoomLevelFromGSD(image.getGSD(), image.getCenter())

}