package ch.bok.maptiler.server.services

import ch.bok.maptiler.server.configuration.MBTilesProperties
import org.springframework.stereotype.Service
import java.io.File
import java.io.FilenameFilter

class TileSetFolderNotFoundException(folder: String) :
    RuntimeException("Cannot find directory $folder for tilesets")


@Service
class MBTileSetsService(
    val configuration: MBTilesProperties
) {
    fun folder() = configuration.folder

    fun listTileSetFiles(): List<String> {
        val directory = File(folder())
        if (!directory.exists() || !directory.isDirectory) {
            throw TileSetFolderNotFoundException(folder())
        }
        val filter = FilenameFilter { _, name -> name.endsWith(".mbtiles") }

        return directory.listFiles(filter).map { it.absolutePath }
    }

}