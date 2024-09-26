package ch.bok.maptiler.models.mbtileserver

import ch.bok.maptiler.models.MBTilesMetadata
import ch.bok.maptiler.models.TileCoords.Companion.TILE_SIZE
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JvmInline
value class DBName(val value: String) {
    override fun toString() = "$value"
}

/*
{
   "attribution": "© Julien Beck - University of Geneva",
   "bounds": [23.1332108258033, 37.4283306002941, 23.1333322124318, 37.4284342504381],
   "center": [37.4283824253661, 23.1332715191176],
   "description": "{\"pouet\":\"42\",\"flapflap\":\"la girafe\"}",
   "format": "png",
   "map": "http://localhost:9080/services/test/map",
   "maxzoom": 24,
   "minzoom": 16,
   "name": "paf",
   "scheme": "xyz",
   "tilejson": "2.1.0",
   "tiles": [
    "http://localhost:9080/services/test/tiles/{z}/{x}/{y}.png"
  ],
   "tilesize": 256,
   "type": "overlay"
}
 */
@Serializable
data class TileSetDetails(
    @Transient val dbName: DBName = DBName("???"),
    val name: String,
    val minzoom: Int,
    val maxzoom: Int,
    val bounds: List<Double>,
    val center: List<Double>,
    val description: String?,
    val attribution: String,
    val format: String,
    val type: String,
    @Transient val urlPrefix: String = ""
) {
    @EncodeDefault
    val tilesize: Int = TILE_SIZE

    @EncodeDefault
    val map: String = "$urlPrefix/services/$dbName/map"

    @EncodeDefault
    val schema: String = "xyz"

    @EncodeDefault
    val tilejson: String = "2.1.0"

    @EncodeDefault
    val tiles: List<String> = listOf(
        "$urlPrefix/services/$dbName/tiles/{z}/{x}/{y}.png"
    )

    fun addUrlPrefix(p: String) = copy(urlPrefix = p)
    companion object {
        fun from(dbName: DBName, m: MBTilesMetadata) =
            TileSetDetails(
                dbName = dbName,
                name = m.name,
                minzoom = m.minZoom,
                maxzoom = m.maxZoom,
                bounds = listOf(m.bounds.nw.lon, m.bounds.se.lat, m.bounds.se.lon, m.bounds.nw.lat),
                center = m.bounds.center().let { listOf(it.lat, it.lon) },
                description = Json.encodeToString(m.attributes),
                attribution = m.attribution,
                format = m.format,
                type = m.type
            )
    }
}
