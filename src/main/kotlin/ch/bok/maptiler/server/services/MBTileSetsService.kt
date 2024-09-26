package ch.bok.maptiler.server.services

import ch.bok.maptiler.models.TileCoords
import ch.bok.maptiler.models.mbtileserver.DBName
import ch.bok.maptiler.server.configuration.MBTilesProperties
import org.springframework.stereotype.Service
import java.io.File
import java.io.FilenameFilter

class TileSetFolderNotFoundException(folder: String) :
    RuntimeException("Cannot find directory $folder for tilesets")

class TileSetConnectionNotFoundException(db: DBName) :
    RuntimeException("Cannot find connector $db for tilesets")

@Service
class MBTileSetsService(
    val configuration: MBTilesProperties
) {
    val folder by lazy { configuration.folder }
    val allTileSets by lazy {
        listTileSetFiles().map { (db, path) ->
            db to MBTileSetConnector(path)
        }.toMap()
    }

    fun getTileSetConnection(db: DBName) = connector(db)

    private fun connector(db: DBName) =
        allTileSets[db] ?: throw TileSetConnectionNotFoundException(db)

    fun listTileSetFiles(): Map<DBName, String> {
        val directory = File(folder)
        if (!directory.exists() || !directory.isDirectory) {
            throw TileSetFolderNotFoundException(folder)
        }
        val filter = FilenameFilter { _, name ->
            reName.matches(name)

        }

        return directory.listFiles(filter).map {
            val m = reName.matchEntire(it.absolutePath)!!

            val dbName = DBName(m.groupValues[1])
            dbName to it.absolutePath
        }.toMap()
    }

    fun metadata(db: DBName, urlPrefix: String) =
        getTileSetConnection(db).metadata().addUrlPrefix(urlPrefix)

    fun getTile(db: DBName, coords: TileCoords):ByteArray =
        connector(db).getTile(coords)

    companion object {
        val reName = Regex("""(?:.*/)?(.+)\.mbtiles""")
    }
}