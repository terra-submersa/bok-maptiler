package ch.bok.maptiler.server.services

import ch.bok.maptiler.models.MBTilesMetadata
import ch.bok.maptiler.models.TileCoords
import ch.bok.maptiler.models.TileCoords.Companion.TILE_SIZE
import ch.bok.maptiler.models.mbtileserver.DBName
import ch.bok.maptiler.models.mbtileserver.TileSetDetails
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.sql.DriverManager
import javax.imageio.ImageIO

class NoTileFoundException(coords: TileCoords) :
    RuntimeException("no tile found $coords")

class MBTileSetConnector(private val filename: String) {
    private val dbName = DBName(filename.split("/").last().replace(".mbtiles", ""))
    private val connection by lazy {
        DriverManager.getConnection("jdbc:sqlite:${filename}")
    }

    fun metadata(): TileSetDetails {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT name, value FROM metadata")
        val descriptor = mutableMapOf<String, Any?>()
        while (resultSet?.next() == true) {
            val name = resultSet.getString("name")
            val value = resultSet.getString("value")
            descriptor[name] = value
        }
        val metadata = MBTilesMetadata.fromMBTilesDescriptor(descriptor.toMap())
        return TileSetDetails.from(dbName, metadata)
    }

    fun countTiles(): Int {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT COUNT(*) FROM tiles")
        resultSet.next()
        val count = resultSet.getInt(1)
        resultSet.close()
        statement.close()
        return count
    }

    fun getTile(coords: TileCoords): ByteArray {
        val stmt = connection.createStatement()

        val resultSet =
            stmt.executeQuery("SELECT tile_data FROM tiles WHERE zoom_level=${coords.zoom} and tile_column=${coords.x} and tile_row=${coords.y}")
        if (resultSet.next()) {
            val bytes: ByteArray = resultSet.getBytes(1)
            resultSet.close()
            stmt.close()
            return bytes
        }
        return emptyTile
    }

    companion object {
        val emptyTile: ByteArray by lazy {
            val image = BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()
            g.color = Color(0, 0, 0, 0)
            g.fillRect(0, 0, TILE_SIZE, TILE_SIZE)
            g.dispose()
            val baos = ByteArrayOutputStream()
            ImageIO.write(image, "png", baos)
            baos.toByteArray()
        }
    }
}