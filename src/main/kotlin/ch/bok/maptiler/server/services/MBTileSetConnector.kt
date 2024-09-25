package ch.bok.maptiler.server.services

import java.sql.DriverManager

class MBTileSetConnector(private val filename: String) {
    val path = filename.split("/").last().replace(".mbtiles", "")
    private val connection by lazy {
        DriverManager.getConnection("jdbc:sqlite:${filename}")
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
}