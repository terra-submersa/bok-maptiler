package ch.bok.maptiler

import ch.bok.maptiler.models.Coords

interface GeoImageFixtures {
    fun anOrthoPhotoTifFile() = TestUtils.getTestFile("odm_1/code/odm_orthophoto/odm_orthophoto.tif")
    fun aNWCornerWGS84() = Coords.build(23.1332197, 37.4284297, "EPSG:4326")
    fun aSECornerWGS84() = Coords.build(23.1333288, 37.4283189, "EPSG:4326")
    fun aNWCornerUTM34M() = Coords.build(688746.758, 4144537.265,"EPSG:32634")
    fun aSECornerUTM34M() = Coords.build(688756.697, 4144525.192, "EPSG:32634")
}