package ch.bok.maptiler.models

import ch.bok.maptiler.utils.GeoUtils
import org.geotools.geometry.DirectPosition2D
import org.geotools.geometry.jts.JTS
import org.geotools.referencing.CRS
import org.locationtech.jts.geom.Coordinate
import org.opengis.referencing.crs.CoordinateReferenceSystem

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

    fun distance(other: Coords)=
        if (crs == other.crs) {
            JTS.orthodromicDistance(Coordinate(lon, lat), Coordinate(other.lon, other.lat), crs)
        } else {
            throw IncoherentDistanceCRSException(this, other)
        }

    fun toCrs(target: CoordinateReferenceSystem): Coords{
        val transform = CRS.findMathTransform(crs, target, false)
        val to = transform.transform(DirectPosition2D(crs,lon, lat), DirectPosition2D())

        return Coords(to.coordinate[0],to.coordinate[1], target)
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
    override fun toString(): String {
        return "$nw - $se"
    }
}