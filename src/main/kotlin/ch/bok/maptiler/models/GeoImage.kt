package ch.bok.maptiler.models

import org.geotools.coverage.grid.io.GridFormatFinder
import org.geotools.geometry.DirectPosition2D
import org.geotools.referencing.CRS
import org.opengis.referencing.crs.CoordinateReferenceSystem
import java.io.File
import javax.imageio.ImageIO

data class Coords(val lon: Double, val lat: Double) {

    fun mid(other: Coords) = Coords((lon + other.lon) / 2, (lat + other.lat) / 2)
    override fun toString(): String {
        return "($lon, $lat)"
    }
}

data class BoundingBox(val nw: Coords, val se: Coords) {
    override fun toString(): String {
        return "$nw - $se"
    }
}

data class Dimensions(
    val width: Int,
    val height: Int
) {
    override fun toString(): String {
        return "$width x $height"
    }
}

data class GeoImage(
    val boundingBox: BoundingBox
) {
    companion object {
        fun getBoundingBox(imageFile: File): BoundingBox {
            val format = GridFormatFinder.findFormat(imageFile)
            val reader = format.getReader(imageFile)
            val coverage = reader.read()
            val envelope = coverage.envelope
            val transform = CRS.findMathTransform(envelope.coordinateReferenceSystem, targetCrs, false)

            val upperWGS84 = transform.transform(envelope.upperCorner, DirectPosition2D())
            val lowerWGS84 = transform.transform(envelope.lowerCorner, DirectPosition2D())
            return BoundingBox(
                Coords(lowerWGS84.coordinate[1], upperWGS84.coordinate[0]),
                Coords(upperWGS84.coordinate[1], lowerWGS84.coordinate[0])
            )
        }

        fun getDimensions(imageFile: File): Dimensions {
            val image = ImageIO.read(imageFile)
            return Dimensions(image.width, image.height)
        }

        val targetCrs: CoordinateReferenceSystem = CRS.decode("EPSG:4326");
        fun fromFile(imageFile: File): GeoImage {

            return GeoImage(
                boundingBox = getBoundingBox(imageFile)
            )
        }
    }
}
