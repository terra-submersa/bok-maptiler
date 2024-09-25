package ch.bok.maptiler.server.controllers

import ch.bok.maptiler.server.services.MBTileSetsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class MBTilesController(
    service: MBTileSetsService
) {
    @GetMapping("/tiles/{z}/{x}/{y}.png")
    fun getTile(@PathVariable z: Int, @PathVariable x: Int, @PathVariable y: Int): ResponseEntity<ByteArray> {
        TODO()
    }
}