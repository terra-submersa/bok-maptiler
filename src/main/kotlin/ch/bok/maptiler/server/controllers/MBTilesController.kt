package ch.bok.maptiler.server.controllers

import ch.bok.maptiler.models.TileCoords
import ch.bok.maptiler.models.mbtileserver.DBName
import ch.bok.maptiler.models.mbtileserver.TileSetDetails
import ch.bok.maptiler.server.services.MBTileSetsService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/services")
@CrossOrigin(origins = ["*"])
class MBTilesController(
    val service: MBTileSetsService
) {
    private fun getUrlPrefix(request: HttpServletRequest): String {
        val protocol = request.scheme
        val domain = request.serverName
        val port = request.serverPort
        return "$protocol://$domain:$port"
    }

    @GetMapping("")
    fun listTileSets(request: HttpServletRequest) =
        service.allTileSets
            .map { service.metadata(it.key, getUrlPrefix(request)) }
            .map {
                mapOf(
                    "imageType" to it.format,
                    "url" to it.map.replace("/map", ""),
                    "name" to it.name
                )
            }


    @GetMapping("/{dbName}")
    fun getTileSetDetails(
        @PathVariable dbName: String,
        request: HttpServletRequest
    ): TileSetDetails = service.metadata(DBName(dbName), getUrlPrefix(request))

    @GetMapping("/{dbName}/tiles/{z}/{x}/{y}.png")
    fun getTile(
        @PathVariable dbName: String,
        @PathVariable z: Int,
        @PathVariable x: Long,
        @PathVariable y: Long
    ): ResponseEntity<ByteArray> {
        val bytes = service.getTile(DBName(dbName), TileCoords(x, y, z))
        val headers = HttpHeaders()
        headers.contentType = MediaType.IMAGE_PNG

        return ResponseEntity(bytes, headers, HttpStatus.OK)
    }
}