package ch.bok.maptiler.models

import ch.bok.maptiler.GeoImageFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MBTilesMetadataTest : GeoImageFixtures {
    private val metadata = anMBTilesMetadata()
    private val descriptor = mapOf(
        "name" to "paf le chien",
        "format" to "png",
        "bounds" to "23.1332108258033,37.42833060029414,23.133332212431817,37.428434250438066",
        "center" to "37.4283824253661,23.133271519117557",
        "minzoom" to 16,
        "maxzoom" to 24,
        "attribution" to "© Julien Beck - University of Geneva",
        "description" to """{"pouet":"42","flapflap":"la girafe"}""",
        "type" to "overlay",
    )

    @Test
    fun `toMBTilesDescriptor should serialize to Map`() {
        val got = metadata.toMBTilesDescriptor()

        assertEquals(descriptor, got)
    }

    @Test
    fun `fromMBTilesDescriptor should deserialize a Map`() {
        val got = MBTilesMetadata.fromMBTilesDescriptor(descriptor)

        assertEquals(metadata, got)
    }
}