package ch.bok.maptiler.server.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mbtiles")
data class MBTilesProperties(
    val folder: String
)