package ch.bok.maptiler.models

import ch.bok.maptiler.services.Tiler
import ch.bok.maptiler.utils.GeoUtils
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString


data class MBTilesMetadata(
    val name: String,
    val bounds: BoundingBox,
    val minZoom: Int,
    val maxZoom: Int,
    val attribution: String,
    val attributes: Map<String, String>? = null,
) {
    val format = "png"
    val type = "overlay"

    /**
     * convert into an MBTiles format
     * https://github.com/mapbox/mbtiles-spec/blob/master/1.3/spec.md
     */
    fun toMBTilesDescriptor(): Map<String, Any?> {
        val boundsStr =
            "${bounds.nw.lon},${bounds.se.lat},${bounds.se.lon},${bounds.nw.lat}"
        val centerCoords = bounds.center()
        val center = "${centerCoords.lat},${centerCoords.lon}"
        val attributesJson = attributes?.let {
            Json.encodeToString(it)
        }
        return mapOf<String, Any?>(
            "name" to name,
            "format" to format,
            "bounds" to boundsStr,
            "center" to center,
            "minzoom" to minZoom,
            "maxzoom" to maxZoom,
            "attribution" to attribution,
            "description" to attributesJson,
            "type" to type,
        )
    }

    companion object {
        fun build(
            geoImage: GeoImage,
            minZoom: Int,
            name: String,
            attributes: Map<String, String>? = null
        ) = MBTilesMetadata(
            name = name,
            bounds = geoImage.boundingBox,
            minZoom = minZoom,
            maxZoom = Tiler(geoImage).maxZoom(),
            attributes = attributes,
            attribution = "© Julien Beck - University of Geneva",
        )
        fun fromMBTilesDescriptor(descriptor: Map<String, Any?>):MBTilesMetadata{
            val cBounds = descriptor["bounds"].toString().split(",").map{it.toDouble()}
            val bounds = BoundingBox(
                nw=Coords(cBounds[0], cBounds[3], GeoUtils.wgs84CRS),
                se = Coords(cBounds[2], cBounds[1], GeoUtils.wgs84CRS)
            )
            val attributes = descriptor["description"]?.let{Json.decodeFromString<Map<String, String>>(it.toString())}
            return MBTilesMetadata(
                name = descriptor["name"].toString(),
                bounds=bounds,
                minZoom = descriptor["minzoom"].toString().toInt(),
                maxZoom = descriptor["maxzoom"].toString().toInt(),
                attribution = descriptor["attribution"].toString(),
                attributes = attributes
            )
        }
    }
}
