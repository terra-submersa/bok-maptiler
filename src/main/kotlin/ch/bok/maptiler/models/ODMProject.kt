package ch.bok.maptiler.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.FileInputStream

@Serializable
data class ODMProcessStats(
    @SerialName("average_gsd")
    val averageGSD: Double
)

@Serializable
data class ODMStats(
    @SerialName("odm_processing_statistics")
    val processing: ODMProcessStats
)

data class ODMProject(
    val path: String,
    val averageGSD: Double
) {
    companion object {
        fun fromPath(path: String): ODMProject {
            val statsFile = "$path/code/odm_report/stats.json"
            val stats = Json.decodeFromStream<ODMStats>(FileInputStream(statsFile))
            return ODMProject(
                path = path,
                averageGSD = stats.processing.averageGSD
            )
        }
    }
}