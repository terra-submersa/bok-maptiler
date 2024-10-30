package ch.bok.maptiler.models

import ch.bok.maptiler.utils.GeoUtils
import org.geotools.geometry.DirectPosition2D
import org.geotools.geometry.jts.JTS
import org.geotools.referencing.CRS
import org.locationtech.jts.geom.Coordinate
import org.opengis.referencing.crs.CoordinateReferenceSystem
import kotlin.math.sqrt

class IncoherentMidCRSException(c1: Coords, c2: Coords) :
    RuntimeException("Incoherent CRSException with mid() $c1 and $c2")

class IncoherentDistanceCRSException(c1: Coords, c2: Coords) :
    RuntimeException("Incoherent CRSException with distance() $c1 and $c2")

data class Coords(val lon: Double, val lat: Double, val crs: CoordinateReferenceSystem) {
    fun mid(other: Coords) =
        if (crs == other.crs) {
            Coords((lon + other.lon) / 2, (lat + other.lat) / 2, crs)
        } else {
            throw IncoherentMidCRSException(this, other)
        }

    /**
     * distance in meters
     */
    fun distance(other: Coords): Double {
        if (crs != GeoUtils.utm34NCRS || other.crs != GeoUtils.utm34NCRS) {
            return toCrs(GeoUtils.utm34NCRS).distance(other.toCrs(GeoUtils.utm34NCRS))
        }
//        if (crs == other.crs) {
//            return JTS.orthodromicDistance(Coordinate(lon, lat), Coordinate(other.lon, other.lat), crs)
//        }
        val dx = lon - other.lon
        val dy = lat - other.lat
        return sqrt(dx * dx + dy * dy)
        throw IncoherentDistanceCRSException(this, other)
    }

    fun toCrs(target: CoordinateReferenceSystem): Coords {
        val transform = CRS.findMathTransform(crs, target, false)
        val to = transform.transform(DirectPosition2D(crs, lon, lat), DirectPosition2D())

        return Coords(to.coordinate[0], to.coordinate[1], target)
    }

    override fun toString(): String {
        return "($lon, $lat) [${crs.name}]"
    }

    companion object {
        fun build(lon: Double, lat: Double, crsCode: String) =
            Coords(lon, lat, GeoUtils.getCRS(crsCode))
    }
}

data class BoundingBox(val nw: Coords, val se: Coords) {
    val crs = nw.crs

    fun center() = nw.mid(se)

    fun ne() = nw.copy(lon = se.lon)
    fun sw() = se.copy(lon = nw.lon)

    fun toCrs(target: CoordinateReferenceSystem) =
        BoundingBox(nw.toCrs(target), se.toCrs(target))

    fun size() = width() to height()

    fun height(): Double = if (crs != GeoUtils.utm34NCRS) {
        toCrs(GeoUtils.utm34NCRS).height()
    } else {
        nw.distance(sw())
    }

    fun widthBottom() = sw().distance(se)
    fun widthTop() = nw.distance(ne())
    fun width() = nw.mid(ne()).distance(sw().mid(se))


    override fun toString(): String {
        return "$nw - $se"
    }
}