package ch.bok.maptiler.models

import ch.bok.maptiler.utils.GeoUtils
import org.geotools.coverage.grid.io.GridCoverage2DReader
import org.geotools.coverage.grid.io.GridFormatFinder
import org.geotools.geometry.DirectPosition2D
import org.geotools.referencing.CRS
import java.io.File
import javax.imageio.ImageIO


data class Dimensions(
    val width: Int,
    val height: Int
) {
    override fun toString(): String {
        return "$width x $height"
    }
}

data class GeoImage(
    val boundingBox: BoundingBox,
    val dimensions: Dimensions
) {
    fun getNWCorner() = boundingBox.nw
    fun getNECorner() = Coords(boundingBox.se.lon, boundingBox.nw.lat, boundingBox.se.crs)
    fun getSECorner() = boundingBox.se

    fun getSWCorner() = Coords(boundingBox.nw.lon, boundingBox.se.lat, boundingBox.se.crs)
    fun getGSD(): Double {
        val xDist = getNWCorner().distance(getNECorner())
        val yDist = getNWCorner().distance(getSWCorner())
        val xGSD = xDist / dimensions.width.toDouble()
        val yGSD = yDist / dimensions.height.toDouble()
        return (xGSD + yGSD) / 2
    }

    companion object {
        fun getBoundingBox(imageFile: File): BoundingBox {
            val format = GridFormatFinder.findFormat(imageFile)
            val reader = format.getReader(imageFile) as GridCoverage2DReader
            val coverage = reader.read(*emptyArray())
            val envelope = coverage.envelope
            val transform = CRS.findMathTransform(envelope.coordinateReferenceSystem, GeoUtils.wgs84CRS, false)

            val upperWGS84 = transform.transform(envelope.upperCorner, DirectPosition2D())
            val lowerWGS84 = transform.transform(envelope.lowerCorner, DirectPosition2D())
            return BoundingBox(
                Coords(lowerWGS84.coordinate[1], upperWGS84.coordinate[0], GeoUtils.wgs84CRS),
                Coords(upperWGS84.coordinate[1], lowerWGS84.coordinate[0], GeoUtils.wgs84CRS)
            )
        }

        fun getDimensions(imageFile: File): Dimensions {
            val image = ImageIO.read(imageFile)
            return Dimensions(image.width, image.height)
        }

        fun fromFile(imageFile: File): GeoImage {

            return GeoImage(
                boundingBox = getBoundingBox(imageFile),
                dimensions = getDimensions(imageFile)
            )
        }
    }
}
