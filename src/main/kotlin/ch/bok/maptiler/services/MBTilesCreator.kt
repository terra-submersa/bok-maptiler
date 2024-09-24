package ch.bok.maptiler.services

import ch.bok.maptiler.models.GeoImage
import ch.bok.maptiler.models.MBTilesMetadata
import ch.bok.maptiler.models.Tile
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.sql.DriverManager
import java.sql.ResultSet
import javax.imageio.ImageIO

class MBTilesCreator(val filename: String) {
    private val connection by lazy {
        Class.forName("org.sqlite.JDBC")
        val url: String = "jdbc:sqlite:${filename}"
        DriverManager.getConnection(url)
    }

    fun createSchema() {
        val statement = connection.createStatement()
        CREATE_SCHEMA_STATEMENTS.forEach { statement.addBatch(it) }
        statement.executeBatch()
    }

    fun insertMetadata(metadata: MBTilesMetadata) {
        val statement = connection.prepareStatement("INSERT INTO metadata(name, value) VALUES(?, ?)")
        fun insertOne(key: String, value: Any) {
            statement.setString(1, key)
            statement.setString(2, value.toString())
            statement.addBatch()
        }
        insertOne("name", metadata.name)
        insertOne("format", metadata.format)
        val bounds =
            "${metadata.bounds.nw.lon},${metadata.bounds.se.lat},${metadata.bounds.se.lon},${metadata.bounds.nw.lat}"
        insertOne("bounds", bounds)
        val centerCoords = metadata.bounds.center()
        val center = "${centerCoords.lat},${centerCoords.lon}"
        insertOne("center", center)
        insertOne("minzom", metadata.minZoom)
        insertOne("maxzoom", metadata.maxZoom)
        insertOne("attribution", metadata.attribution)
        metadata.attributes?.let { att ->
            val sAtt = att.map { it.key to it.value.toString() }.toMap()
            insertOne("description", Json.encodeToString(sAtt))
        }
        insertOne("type", metadata.type)

        statement.executeBatch()
    }

    suspend fun insertTiles(flow: Flow<Tile>) = flow.collect {
        insertTile(tile = it)
    }

    fun insertTile(tile: Tile) {
        val statement = connection.prepareStatement(INSERT_TILE_STATEMENT)
        statement.setInt(1, tile.coords.zoom)
        statement.setLong(2, tile.coords.x)
        statement.setLong(3, tile.coords.y)
        val baos = ByteArrayOutputStream()
        ImageIO.write(tile.image, "png", baos)
        statement.setBytes(4, baos.toByteArray())
        statement?.executeUpdate()
    }

    fun execute(query: String): List<List<Any?>> {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(query)
        val nbColumns = resultSet.metaData.columnCount
        val rows = mutableListOf<List<Any?>>()
        while (resultSet.next()) {
            val row = (1..nbColumns).map { resultSet.getObject(it) }
            rows.add(row)
        }
        return listOf(*rows.toTypedArray())
    }

    companion object {
        val CREATE_SCHEMA_STATEMENTS = listOf(
            """
            CREATE TABLE tiles (
                zoom_level INTEGER,
                tile_column INTEGER,
                tile_row INTEGER,
                tile_data BLOB
            )
            """.trimIndent(),
            """
            CREATE TABLE metadata (
                name TEXT,
                value TEXT
            )
            """.trimIndent(),
            """            
            CREATE UNIQUE INDEX idx_tiles on tiles (zoom_level, tile_column, tile_row)
        """.trimIndent()
        )

        const val INSERT_TILE_STATEMENT = "INSERT INTO tiles(zoom_level,tile_column,tile_row,tile_data) VALUES(?,?,?,?)"
    }
}