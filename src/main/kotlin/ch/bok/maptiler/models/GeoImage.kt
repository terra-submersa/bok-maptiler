package ch.bok.maptiler.models

import ch.bok.maptiler.utils.GeoUtils
import org.geotools.coverage.grid.io.GridCoverage2DReader
import org.geotools.coverage.grid.io.GridFormatFinder
import org.geotools.geometry.DirectPosition2D
import org.geotools.referencing.CRS
import org.opengis.referencing.crs.CoordinateReferenceSystem
import java.awt.image.BufferedImage
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
    val dimensions: Dimensions,
    val image: BufferedImage
) {

    fun positionToCoords(p: Position) =
        Coords(
            boundingBox.nw.lon + (boundingBox.se.lon - boundingBox.nw.lon) * p.x / dimensions.width,
            boundingBox.nw.lat + (boundingBox.se.lat - boundingBox.nw.lat) * p.y / dimensions.height,
            boundingBox.crs
        )

    fun coordsToPosition(c: Coords): Position =
        if (c.crs != GeoUtils.utm34NCRS) {
            coordsToPosition(c.toCrs(GeoUtils.utm34NCRS))
        } else {
            Position(
                (dimensions.width * (c.lon - boundingBox.nw.lon) / (boundingBox.se.lon - boundingBox.nw.lon)).toInt(),
                (dimensions.height * (c.lat - boundingBox.nw.lat) / (boundingBox.se.lat - boundingBox.nw.lat)).toInt(),
            )
        }

    fun getNWCorner(crs: CoordinateReferenceSystem? = null) = crs?.let { boundingBox.nw.toCrs(it) } ?: boundingBox.nw
    fun getNECorner(crs: CoordinateReferenceSystem? = null) =
        crs?.let { Coords(boundingBox.se.toCrs(it).lon, boundingBox.nw.toCrs(it).lat, it) }
            ?: Coords(boundingBox.se.lon, boundingBox.nw.lat, boundingBox.se.crs)

    fun getSECorner(crs: CoordinateReferenceSystem? = null) = crs?.let { boundingBox.se.toCrs(it) } ?: boundingBox.se
    fun getSWCorner(crs: CoordinateReferenceSystem? = null) =
        crs?.let { Coords(boundingBox.nw.toCrs(it).lon, boundingBox.se.toCrs(it).lat, it) }
            ?: Coords(boundingBox.nw.lon, boundingBox.se.lat, boundingBox.se.crs)

    fun getCenter() = boundingBox.nw.mid(boundingBox.se)
    fun getXGSD(): Double {
        val xDist = getNWCorner().distance(getNECorner())
        return xDist / dimensions.width.toDouble()
    }

    fun getYGSD(): Double {
        val yDist = getNWCorner().distance(getSWCorner())
        return yDist / dimensions.height.toDouble()
    }

    fun getGSD(): Double {
        return (getXGSD() + getYGSD()) / 2
    }

    companion object {
        fun getBoundingBox(imageFile: File): BoundingBox {
            val format = GridFormatFinder.findFormat(imageFile)
            val reader = format.getReader(imageFile) as GridCoverage2DReader
            val coverage = reader.read(*emptyArray())
            val envelope = coverage.envelope

            val nw = Coords(envelope.getMinimum(0), envelope.getMaximum(1), envelope.coordinateReferenceSystem)
            val se = Coords(envelope.getMaximum(0), envelope.getMinimum(1), envelope.coordinateReferenceSystem)

            return BoundingBox(nw, se)
        }

        fun fromFile(imageFile: File): GeoImage {
            val image = ImageIO.read(imageFile)

            return GeoImage(
                boundingBox = getBoundingBox(imageFile),
                dimensions = Dimensions(image.width, image.height),
                image = image
            )
        }
    }
}
