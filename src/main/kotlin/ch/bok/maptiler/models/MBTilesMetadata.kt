package ch.bok.maptiler.models

import ch.bok.maptiler.services.Tiler

data class MBTilesMetadata(
    val name: String,
    val bounds: BoundingBox,
    val minZoom: Int,
    val maxZoom: Int,
    val attribution: String,
    val attributes: Map<String, Any>? = null,
) {
    val format = "png"
    val type = "overlay"

    companion object {
        fun build(
            geoImage: GeoImage,
            minZoomLevel: Int,
            name: String,
            attributes: Map<String, Any>? = null
        ) = MBTilesMetadata(
            name = name,
            bounds = geoImage.boundingBox,
            minZoom = minZoomLevel,
            maxZoom = Tiler(geoImage).maxZoom(),
            attributes = attributes,
            attribution = "© Julien Beck - University of Geneva",
        )
    }
}
