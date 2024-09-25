package ch.bok.maptiler

import ch.bok.maptiler.models.BoundingBox
import ch.bok.maptiler.models.Coords
import ch.bok.maptiler.models.GeoImage
import ch.bok.maptiler.models.MBTilesMetadata

interface GeoImageFixtures {
    fun anOrthoPhotoTifFile() = TestUtils.getTestFile("odm_orthophoto.tif")
    fun anOrthoPhotoImage(crsCode: String? = null, file: String = "odm_orthophoto.tif") =
        GeoImage.fromFile(TestUtils.getTestFile(file), crsCode = crsCode)

    fun aNWCornerWGS84() = Coords.build(23.1332108258033, 37.428434250438066, "EPSG:4326")
    fun aSECornerWGS84() = Coords.build(23.133332212431817, 37.42833060029414, "EPSG:4326")
    fun aNWCornerUTM34M() = Coords.build(688745.970408, 4144537.75204, "EPSG:32634")
    fun aSECornerUTM34M() = Coords.build(688756.9643610917, 4144526.4940400003, "EPSG:32634")

    fun anMBTilesMetadata() = MBTilesMetadata(
        name = "paf le chien",
        bounds = BoundingBox(nw = aNWCornerWGS84(), se = aSECornerWGS84()),
        minZoom = 16,
        maxZoom = 24,
        attribution = "© Julien Beck - University of Geneva",
        attributes = mapOf(
            "pouet" to "42",
            "flapflap" to "la girafe"
        )
    )
}