package ch.bok.maptiler.utils

import ch.bok.maptiler.models.Coords
import org.geotools.geometry.jts.JTS
import org.geotools.referencing.CRS
import org.locationtech.jts.geom.Coordinate
import org.opengis.referencing.crs.CoordinateReferenceSystem

object GeoUtils {
    val wgs84CRS: CoordinateReferenceSystem = CRS.decode("EPSG:4326");
}