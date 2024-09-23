package ch.bok.maptiler.utils

import org.geotools.referencing.ReferencingFactoryFinder
import org.geotools.util.factory.Hints
import org.opengis.referencing.crs.CoordinateReferenceSystem
import java.lang.Boolean
import kotlin.String


object GeoUtils {
    /**
     * We must use this Hint, not to invert lon/lat for certain coordinate systems
     * https://docs.geotools.org/latest/userguide/library/referencing/order.html
     */
    private val hints = Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE)
    private val factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints)

    fun getCRS(crsCode: String) = factory.createCoordinateReferenceSystem(crsCode);

    val wgs84CRS: CoordinateReferenceSystem = getCRS("EPSG:4326")
}