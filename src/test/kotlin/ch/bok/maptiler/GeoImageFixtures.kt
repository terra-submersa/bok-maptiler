package ch.bok.maptiler

import ch.bok.maptiler.models.Coords
import ch.bok.maptiler.models.GeoImage

interface GeoImageFixtures {
    fun anOrthoPhotoTifFile() = TestUtils.getTestFile("odm_1/code/odm_orthophoto/odm_orthophoto.tif")
    fun anOrthoPhotoImage(crsCode: String? = null) = GeoImage.fromFile(anOrthoPhotoTifFile(), crsCode = crsCode)

    fun aNWCornerWGS84() = Coords.build(23.1332108, 37.4284343, "EPSG:4326")
    fun aSECornerWGS84() = Coords.build(23.1333322, 37.4283306, "EPSG:4326")
    fun aNWCornerUTM34M() = Coords.build(688745.970408, 4144537.75204, "EPSG:32634")
    fun aSECornerUTM34M() = Coords.build(688756.969408, 4144526.4940400003, "EPSG:32634")
}