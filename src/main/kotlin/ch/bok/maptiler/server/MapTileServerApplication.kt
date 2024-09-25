package ch.bok.maptiler.server

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("ch.bok.maptiler.server")
@OpenAPIDefinition()
class MBTilesServerApplication{

}

fun main(args: Array<String>) {
    runApplication<MBTilesServerApplication>(*args)
}
