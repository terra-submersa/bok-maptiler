package ch.bok.maptiler.models

import ch.bok.maptiler.utils.GeoUtils
import org.geotools.coverage.grid.io.GridCoverage2DReader
import org.geotools.coverage.grid.io.GridFormatFinder
import org.geotools.geometry.DirectPosition2D
import org.geotools.referencing.CRS
import java.io.File
import javax.imageio.ImageIO

data class Position(
    val x: Int,
    val y: Int
)

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

    fun positionToCoords(p: Position) =
        Coords(
            boundingBox.nw.lon + (boundingBox.se.lon - boundingBox.nw.lon) * p.x / dimensions.width,
            boundingBox.nw.lat + (boundingBox.se.lat - boundingBox.nw.lat) * p.y / dimensions.height,
            boundingBox.crs
        )

    fun coordsToPosition(c: Coords) =
        Position(
            (dimensions.width * (c.lon - boundingBox.nw.lon) / (boundingBox.se.lon - boundingBox.nw.lon)).toInt(),
            (dimensions.height * (c.lat - boundingBox.nw.lat) / (boundingBox.se.lat - boundingBox.nw.lat)).toInt(),
        )

    fun getNWCorner() = boundingBox.nw
    fun getNECorner() = Coords(boundingBox.se.lon, boundingBox.nw.lat, boundingBox.se.crs)
    fun getSECorner() = boundingBox.se
    fun getSWCorner() = Coords(boundingBox.nw.lon, boundingBox.se.lat, boundingBox.se.crs)

    fun getCenter() = boundingBox.nw.mid(boundingBox.se)
    fun getGSD(): Double {
        val xDist = getNWCorner().distance(getNECorner())
        val yDist = getNWCorner().distance(getSWCorner())
        val xGSD = xDist / dimensions.width.toDouble()
        val yGSD = yDist / dimensions.height.toDouble()
        return (xGSD + yGSD) / 2
    }

    companion object {
        fun getBoundingBox(imageFile: File, crsCode: String? = null): BoundingBox {
            val format = GridFormatFinder.findFormat(imageFile)
            val reader = format.getReader(imageFile) as GridCoverage2DReader
            val coverage = reader.read(*emptyArray())
            val envelope = coverage.envelope

            val nw = Coords(envelope.getMinimum(0), envelope.getMaximum(1), envelope.coordinateReferenceSystem)
            val se = Coords(envelope.getMaximum(0), envelope.getMinimum(1), envelope.coordinateReferenceSystem)

            return crsCode?.let {
                val crs = GeoUtils.getCRS(it)
                BoundingBox(nw.toCrs(crs), se.toCrs(crs))
            } ?: BoundingBox(nw, se)
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
